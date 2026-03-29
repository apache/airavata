/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.execution.util.common;

import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

/**
 * Starts the singleton MariaDB container before any tests are discovered or executed,
 * but only when integration tests are actually being run (i.e. the "integration" group
 * is included via {@code -Dgroups=integration}).
 *
 * <p>This avoids starting Docker when running only unit tests ({@code mvn test}).
 *
 * <p>Registered via META-INF/services/org.junit.platform.launcher.LauncherSessionListener.
 */
public class SharedMariaDBLauncherSessionListener implements LauncherSessionListener {

    @Override
    public void launcherSessionOpened(LauncherSession session) {
        String groups = System.getProperty("groups", "");
        String surefireGroups = System.getProperty("surefire.groups", "");
        if (groups.contains("integration") || surefireGroups.contains("integration")) {
            SharedMariaDB.getInstance();
        }
    }
}
