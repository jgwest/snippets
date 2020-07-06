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

package com.odoeventconsumer.jsonevents;

public class MachineEventWrapper {

    DevFileCommandExecutionBegin devFileCommandExecutionBegin;
    DevFileCommandExecutionComplete devFileCommandExecutionComplete;
    ReportError reportError;

    LogText logText;
    SupervisordStatus supervisordStatus;
    ContainerStatus containerStatus;
    URLReachable urlReachable;
    KubernetesPodStatus kubernetesPodStatus;

    public LogText getLogText() {
        return logText;
    }

    public void setLogText(LogText logText) {
        this.logText = logText;
    }

    public SupervisordStatus getSupervisordStatus() {
        return supervisordStatus;
    }

    public void setSupervisordStatus(SupervisordStatus supervisordStatus) {
        this.supervisordStatus = supervisordStatus;
    }

    public ContainerStatus getContainerStatus() {
        return containerStatus;
    }

    public void setContainerStatus(ContainerStatus containerStatus) {
        this.containerStatus = containerStatus;
    }

    public URLReachable getUrlReachable() {
        return urlReachable;
    }

    public void setUrlReachable(URLReachable urlReachable) {
        this.urlReachable = urlReachable;
    }

    public KubernetesPodStatus getKubernetesPodStatus() {
        return kubernetesPodStatus;
    }

    public void setKubernetesPodStatus(KubernetesPodStatus kubernetesPodStatus) {
        this.kubernetesPodStatus = kubernetesPodStatus;
    }

    public DevFileCommandExecutionBegin getDevFileCommandExecutionBegin() {
        return devFileCommandExecutionBegin;
    }

    public void setDevFileCommandExecutionBegin(DevFileCommandExecutionBegin devFileCommandExecutionBegin) {
        this.devFileCommandExecutionBegin = devFileCommandExecutionBegin;
    }

    public DevFileCommandExecutionComplete getDevFileCommandExecutionComplete() {
        return devFileCommandExecutionComplete;
    }

    public void setDevFileCommandExecutionComplete(DevFileCommandExecutionComplete devFileCommandExecutionComplete) {
        this.devFileCommandExecutionComplete = devFileCommandExecutionComplete;
    }

    public ReportError getReportError() {
        return reportError;
    }

    public void setReportError(ReportError reportError) {
        this.reportError = reportError;
    }

    public AbstractEvent getContents() {

        if (devFileCommandExecutionBegin != null) {
            return devFileCommandExecutionBegin;
        }

        if (devFileCommandExecutionComplete != null) {
            return devFileCommandExecutionComplete;
        }

        if (reportError != null) {
            return reportError;
        }

        if (logText != null) {
            return logText;
        }
        if (supervisordStatus != null) {
            return supervisordStatus;
        }
        if (containerStatus != null) {
            return containerStatus;
        }
        if (urlReachable != null) {
            return urlReachable;
        }

        if (kubernetesPodStatus != null) {
            return kubernetesPodStatus;
        }

        throw new RuntimeException("");
    }
}
