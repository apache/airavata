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
package org.apache.airavata.task.mock;

import org.apache.airavata.config.conditional.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.task.TaskDef;
import org.apache.airavata.task.TaskHelper;
import org.apache.airavata.task.TaskResult;
import org.apache.airavata.task.TaskUtil;
import org.apache.airavata.task.base.AbstractTask;
import org.springframework.stereotype.Component;

@TaskDef(name = "Mock Task")
@Component
@ConditionalOnParticipant
public class MockTask extends AbstractTask {

    public MockTask(TaskUtil taskUtil) {
        super(taskUtil);
    }

    @Override
    public TaskResult onRun(TaskHelper helper) {
        return onSuccess("Successfully executed Mock Task");
    }

    @Override
    public void onCancel() {}
}
