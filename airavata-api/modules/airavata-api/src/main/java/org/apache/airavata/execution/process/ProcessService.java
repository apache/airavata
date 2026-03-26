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
package org.apache.airavata.execution.process;

import java.util.List;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.core.model.ProcessState;

public interface ProcessService {

    String addProcess(ProcessModel processModel, String experimentId) throws RegistryException;

    ProcessModel getProcess(String processId) throws RegistryException;

    void updateProcess(ProcessModel processModel, String processId) throws RegistryException;

    List<ProcessModel> getProcessList(String experimentId) throws RegistryException;

    List<String> getProcessIds(String experimentId) throws RegistryException;

    List<ProcessModel> getProcessListInState(ProcessState state) throws RegistryException;

    void removeProcess(String processId) throws RegistryException;
}
