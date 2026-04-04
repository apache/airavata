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
package org.apache.airavata.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.airavata.model.task.proto.*;

public class ThriftUtils {

    public static Object getSubTaskModel(TaskModel taskModel) throws InvalidProtocolBufferException {
        ByteString subTaskBytes = taskModel.getSubTaskModel();
        if (subTaskBytes.isEmpty()) {
            return null;
        }
        switch (taskModel.getTaskType()) {
            case DATA_STAGING:
            case OUTPUT_FETCHING:
                return DataStagingTaskModel.parseFrom(subTaskBytes);
            case ENV_SETUP:
                return EnvironmentSetupTaskModel.parseFrom(subTaskBytes);
            case JOB_SUBMISSION:
                return JobSubmissionTaskModel.parseFrom(subTaskBytes);
            case MONITORING:
                return MonitorTaskModel.parseFrom(subTaskBytes);
            case ENV_CLEANUP: // TODO return Environment Clean up task model
            default:
                return null;
        }
    }
}
