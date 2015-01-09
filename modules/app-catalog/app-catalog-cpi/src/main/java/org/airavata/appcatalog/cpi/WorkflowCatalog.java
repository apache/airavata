/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

package org.airavata.appcatalog.cpi;

import java.util.List;

public interface WorkflowCatalog {

    public List<String> getAllWorkflows() throws AppCatalogException;

    public org.apache.airavata.model.Workflow getWorkflow(String workflowTemplateId) throws AppCatalogException;

    public void deleteWorkflow(String workflowTemplateId) throws AppCatalogException;

    public String registerWorkflow(org.apache.airavata.model.Workflow workflow) throws AppCatalogException;

    public void updateWorkflow(String workflowTemplateId, org.apache.airavata.model.Workflow workflow) throws AppCatalogException;

    public String getWorkflowTemplateId(String workflowName) throws AppCatalogException;

    public boolean isWorkflowExistWithName(String workflowName) throws AppCatalogException;
}
