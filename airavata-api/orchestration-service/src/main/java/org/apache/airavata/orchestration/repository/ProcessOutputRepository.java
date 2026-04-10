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
package org.apache.airavata.orchestration.repository;

import java.util.List;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.springframework.stereotype.Component;

/**
 * Thin facade over ExecIoParamRepository for backward compatibility.
 */
@Component
public class ProcessOutputRepository {

    private final ExecIoParamRepository delegate = new ExecIoParamRepository();

    public String addProcessOutputs(List<OutputDataObjectType> processOutputs, String processId)
            throws RegistryException {
        return delegate.addProcessOutputs(processOutputs, processId);
    }

    public void updateProcessOutputs(List<OutputDataObjectType> processOutputs, String processId)
            throws RegistryException {
        delegate.updateProcessOutputs(processOutputs, processId);
    }

    public List<OutputDataObjectType> getProcessOutputs(String processId) throws RegistryException {
        return delegate.getProcessOutputs(processId);
    }
}
