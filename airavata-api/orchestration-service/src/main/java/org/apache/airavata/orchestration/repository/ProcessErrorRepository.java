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
import org.apache.airavata.model.commons.proto.ErrorModel;
import org.springframework.stereotype.Component;

/**
 * Thin facade over ExecErrorRepository for backward compatibility.
 */
@Component
public class ProcessErrorRepository {

    private final ExecErrorRepository delegate = new ExecErrorRepository();

    public String addProcessError(ErrorModel processError, String processId) throws RegistryException {
        return delegate.addProcessError(processError, processId);
    }

    public String updateProcessError(ErrorModel processError, String processId) throws RegistryException {
        return delegate.updateProcessError(processError, processId);
    }

    public List<ErrorModel> getProcessError(String processId) throws RegistryException {
        return delegate.getProcessError(processId);
    }
}
