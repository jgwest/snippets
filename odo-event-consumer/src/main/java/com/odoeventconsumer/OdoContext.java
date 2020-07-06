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

import java.nio.file.Path;
import java.util.Optional;

/**
 * Various global paths that are shared throughout the code base via this class. All values are immutable.
 */
public class OdoContext {

    final Path odoPath;

    final Path contextPath;

    final Path odoBinaryPath;

    final Path kubernetesContext;

    public OdoContext(Path odoPath, Path contextPath, Path kubernetesContext) {
        this.odoPath = odoPath;
        this.contextPath = contextPath;
        this.odoBinaryPath = this.odoPath.resolve("odo");
        this.kubernetesContext = kubernetesContext;
    }

    public Path getOdoPath() {
        return odoPath;
    }

    public Path getContextPath() {
        return contextPath;
    }

    public Path getOdoBinaryPath() {
        return odoBinaryPath;
    }

    public Optional<Path> getKubernetesContext() {
        return Optional.ofNullable(kubernetesContext);
    }
}
