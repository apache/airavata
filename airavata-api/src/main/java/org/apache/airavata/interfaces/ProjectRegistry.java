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
package org.apache.airavata.interfaces;

import java.util.List;
import java.util.Map;
import org.apache.airavata.model.experiment.proto.ProjectSearchFields;
import org.apache.airavata.model.workspace.proto.Notification;
import org.apache.airavata.model.workspace.proto.Project;

/**
 * Registry operations for projects and notifications.
 */
public interface ProjectRegistry {

    // --- Project operations ---
    String createProject(String gatewayId, Project project) throws Exception;

    List<Project> getUserProjects(String gatewayId, String userName, int limit, int offset) throws Exception;

    Project getProject(String projectId) throws Exception;

    boolean deleteProject(String projectId) throws Exception;

    void updateProject(String projectId, Project updatedProject) throws Exception;

    List<Project> searchProjects(
            String gatewayId,
            String userName,
            List<String> accessibleProjIds,
            Map<ProjectSearchFields, String> filters,
            int limit,
            int offset)
            throws Exception;

    // --- Notification operations ---
    boolean deleteNotification(String gatewayId, String notificationId) throws Exception;

    Notification getNotification(String gatewayId, String notificationId) throws Exception;

    List<Notification> getAllNotifications(String gatewayId) throws Exception;

    boolean updateNotification(Notification notification) throws Exception;

    String createNotification(Notification notification) throws Exception;
}
