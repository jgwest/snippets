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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KubernetesPodStatusEntry {
    String name;
    String uid;
    String phase;
    Map<String, String> labels = new HashMap<>();
    String startTime;
    List<PodContainerStatus> containers = new ArrayList<>();
    List<PodContainerStatus> initContainers = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public List<PodContainerStatus> getContainers() {
        return containers;
    }

    public void setContainers(List<PodContainerStatus> containers) {
        this.containers = containers;
    }

    public List<PodContainerStatus> getInitContainers() {
        return initContainers;
    }

    public void setInitContainers(List<PodContainerStatus> initContainers) {
        this.initContainers = initContainers;
    }

}
