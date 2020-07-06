/*
 * Copyright 2020 Jonathan West, IBM Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
*/

package com.odoeventconsumer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.odoeventconsumer.jsonevents.KubernetesPodStatus;
import com.odoeventconsumer.jsonevents.KubernetesPodStatusEntry;
import com.odoeventconsumer.jsonevents.MachineEventWrapper;
import com.odoeventconsumer.jsonevents.PodContainerStatus;
import com.odoeventconsumer.jsonevents.ReportError;
import com.odoeventconsumer.jsonevents.SupervisordStatus;

/**
 * An example implementation of how to consume the 'odo component status -o json' logs and 'odo push -o json' logs.
 */
public class OdoEventConsumer {

    /** Set this to true to see the output from odo */
    private static final boolean DEBUG = false;

    public static void main(String[] args)
            throws JsonSyntaxException, JsonIOException, IOException, InterruptedException {

        if (args.length < 2) {
            System.out.println(
                    "Arguments: \"path to odo executable\" \"path to existing, pushed odo component\"  \"(optional) path to kubernetes context file\"`");
            return;
        }

        Path odoPath = Paths.get(args[0]);
        Path componentPath = Paths.get(args[1]);

        Path kubernetesContext = null;
        if (args.length > 2) {
            kubernetesContext = Paths.get(args[2]);
        }

        OdoContext context = new OdoContext(odoPath, componentPath, kubernetesContext);

        // Start following the component status and report the status
        status(context);
    }

    /**
     * Start following the component status and use that data to determine whether the component is running or not.
     */
    private static void status(OdoContext context) throws IOException, InterruptedException {

        // Start the 'odo component status' process using the paths in context
        Process p;
        {
            ProcessBuilder pb = new ProcessBuilder();

            pb = pb.directory(context.getContextPath().toFile());

            List<String> command = Arrays.asList(context.getOdoBinaryPath().toString(), "component", "status",
                    "--follow", "-o", "json");

            System.out.println(command);

            if (context.getKubernetesContext().isPresent()) {
                pb.environment().put("KUBECONFIG", context.getKubernetesContext().get().toString());
            }

            pb = pb.command(command);

            pb.redirectErrorStream(true);
            p = pb.start();
        }

        Gson gson = new Gson();

        ComponentStatus componentStatus = new ComponentStatus();

        // Read the JSON events from the 'component status' odo process, and unmarshal
        // them to Java objects.
        new InputStreamReaderStream(p.getInputStream()).getStream().map(json -> {
            try {
                MachineEventWrapper parsedJson = gson.fromJson(json, MachineEventWrapper.class);
                return parsedJson;
            } catch (JsonSyntaxException e) {
                System.out.println("> " + json); // For unexpected strings, just output them and ignore.
                return null;
            }
        }).filter(event -> event != null).map(event -> event.getContents()).forEach(event -> {
            if (DEBUG) {
                System.out.println(event.debugString());
            }

            if (event instanceof ReportError) {
                // If we receive an error, set the component status to error
                ComponentStatus oldStatus = componentStatus.clone();

                ReportError re = (ReportError) event;
                componentStatus.clear();
                componentStatus.setErrorOccurred(re.getError());

                reportStatusIfChanged(componentStatus, oldStatus);
            }

            if (event instanceof KubernetesPodStatus) {
                // If we receive a Kubernetes pod status:
                ComponentStatus oldStatus = componentStatus.clone();

                KubernetesPodStatus kps = (KubernetesPodStatus) event;

                // 1) Does there exist a running Pod (phase=Running)
                KubernetesPodStatusEntry containerPod = kps.getPods().stream()
                        .filter(pod -> pod.getPhase().equals("Running")).findFirst().orElse(null);
                if (containerPod == null) {
                    componentStatus.clear();
                    reportStatusIfChanged(componentStatus, oldStatus);
                    return;
                }

                // 2) Does that pod contain a running container
                PodContainerStatus containerStatus = containerPod.getContainers().stream().filter(
                        container -> container.getStarted() == true && container.getState().getRunning() != null)
                        .findFirst().orElse(null);

                if (containerStatus != null) {
                    // Container is running, all is good
                    componentStatus.setErrorOccurred(null);
                    componentStatus.setPodRunning(true);
                    componentStatus.setContainerRunning(true);
                } else {
                    // Pod is running but container is not
                    componentStatus.clear();
                    componentStatus.setPodRunning(true);
                    componentStatus.setContainerRunning(false);
                }

                reportStatusIfChanged(componentStatus, oldStatus);

            }

            if (event instanceof SupervisordStatus) {
                // If we receive a supervisord status upate
                ComponentStatus oldStatus = componentStatus.clone();

                SupervisordStatus supervisordStatus = (SupervisordStatus) event;

                // If the last k8s status we received was all good
                if (componentStatus.isPodRunning() && componentStatus.isContainerRunning()) {

                    // We are all god if there if the 'devrun' program is in state running
                    boolean programRunning = supervisordStatus.getProgramStatus().stream()
                            .anyMatch(e -> e.getProgram().equals("devrun") && e.getStatus().equals("RUNNING"));

                    componentStatus.setSupervisordProgramRunning(programRunning);

                    reportStatusIfChanged(componentStatus, oldStatus);
                }

            }

        });

    }

    private static void reportStatusIfChanged(ComponentStatus newStatus, ComponentStatus oldStatus) {
        if (!newStatus.equals(oldStatus)) {
            String statusText = newStatus.calculateStatus();
            if (statusText != null) {
                System.out.println("- Status: " + statusText);
            }

        }
    }

    /** The current observed status of the odo-managed component */
    private static class ComponentStatus {

        String errorOccurred = null;

        boolean podRunning = false;
        boolean containerRunning = false;

        /**
         * null if we have not yet seen a result for supervisord, otherwise true/false based on program name
         */
        Boolean supervisordProgramRunning;

        public boolean isPodRunning() {
            return podRunning;
        }

        public void setPodRunning(boolean podRunning) {
            this.podRunning = podRunning;
        }

        public boolean isContainerRunning() {
            return containerRunning;
        }

        public void setContainerRunning(boolean containerRunning) {
            this.containerRunning = containerRunning;
        }

        public Boolean getSupervisordProgramRunning() {
            return supervisordProgramRunning;
        }

        public void setSupervisordProgramRunning(Boolean supervisordProgramRunning) {
            this.supervisordProgramRunning = supervisordProgramRunning;
        }

        public void setErrorOccurred(String errorOccurred) {
            this.errorOccurred = errorOccurred;
        }

        public String getErrorOccurred() {
            return errorOccurred;
        }

        public void clear() {
            errorOccurred = null;
            podRunning = false;
            containerRunning = false;
            supervisordProgramRunning = false;
        }

        public String calculateStatus() {
            boolean statusAvailable = true;
            String notRunningReason = null;

            if (errorOccurred != null) {
                return "Not running - error occurred: " + errorOccurred;
            }

            if (podRunning == false) {
                notRunningReason = "Component pod not started.";
            } else if (containerRunning == false) {
                notRunningReason = "Container not running";
            } else if (supervisordProgramRunning == null) {
                statusAvailable = false;
            } else if (supervisordProgramRunning == false) {
                notRunningReason = "Supervisord program not running";
            }

            // Return null if we have not seen a supervisord status yet
            if (!statusAvailable) {
                return null;
            }

            String result = "";

            if (notRunningReason == null) {
                result = "Running  " + this.toString();
            } else {
                result = "Not running - " + notRunningReason + " " + this.toString();
            }

            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ComponentStatus other = (ComponentStatus) obj;
            return containerRunning == other.containerRunning && Objects.equals(errorOccurred, other.errorOccurred)
                    && podRunning == other.podRunning
                    && Objects.equals(supervisordProgramRunning, other.supervisordProgramRunning);
        }

        @Override
        public String toString() {
            return "{ ComponentStatus: errorOccurred=" + errorOccurred + ", podRunning=" + podRunning
                    + ", containerRunning=" + containerRunning + ", supervisordProgramRunning="
                    + supervisordProgramRunning + " }";
        }

        public ComponentStatus clone() {
            ComponentStatus cs = new ComponentStatus();
            cs.setContainerRunning(this.isContainerRunning());
            cs.setPodRunning(this.isPodRunning());
            cs.setSupervisordProgramRunning(this.getSupervisordProgramRunning());
            cs.setErrorOccurred(this.getErrorOccurred());
            return cs;
        }
    }

    /**
     * A simple example of how to consume the JSON events from 'odo push -o json'
     */
    private static void push(OdoContext context) throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder();

        pb = pb.directory(context.getContextPath().toFile());

        List<String> command = Arrays.asList(context.getOdoBinaryPath().toString() + ".exe", "push", "-f", "-o",
                "json");

        System.out.println(command);

        if (context.getKubernetesContext().isPresent()) {
            pb.environment().put("KUBECONFIG", context.getKubernetesContext().get().toString());
        }

        pb = pb.command(command);

        pb.redirectErrorStream(true);
        Process p = pb.start();

        Gson gson = new Gson();

        // Read the lines from the console and unmarshal them to Java objects
        new InputStreamReaderStream(p.getInputStream()).getStream()
                .map(json -> gson.fromJson(json, MachineEventWrapper.class)).map(event -> event.getContents())
                .forEach(event -> {

                    if (DEBUG) {
                        System.out.println(event.debugString());
                    }

                });

    }

}
