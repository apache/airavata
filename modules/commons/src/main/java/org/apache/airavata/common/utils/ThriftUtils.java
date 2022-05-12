/**
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
 */
package org.apache.airavata.common.utils;

import org.apache.airavata.model.task.*;
import org.apache.thrift.*;

public class ThriftUtils {
	public static byte[] serializeThriftObject(TBase object) throws TException {
		return new TSerializer().serialize(object);
	}

	public static void createThriftFromBytes(byte[] bytes, TBase object) throws TException {
		new TDeserializer().deserialize(object, bytes);
	}

	public static Object getSubTaskModel(TaskModel taskModel) throws TException {
		switch (taskModel.getTaskType()) {
			case DATA_STAGING:
			case OUTPUT_FETCHING:
				DataStagingTaskModel dataStagingTaskModel = new DataStagingTaskModel();
				ThriftUtils.createThriftFromBytes(taskModel.getSubTaskModel(), dataStagingTaskModel);
				return dataStagingTaskModel;
			case ENV_SETUP:
                EnvironmentSetupTaskModel environmentSetupTaskModel = new EnvironmentSetupTaskModel();
                ThriftUtils.createThriftFromBytes(taskModel.getSubTaskModel(), environmentSetupTaskModel);
                return environmentSetupTaskModel;
			case JOB_SUBMISSION:
                JobSubmissionTaskModel jobSubmissionTaskModel = new JobSubmissionTaskModel();
                ThriftUtils.createThriftFromBytes(taskModel.getSubTaskModel(), jobSubmissionTaskModel);
                return jobSubmissionTaskModel;
            case MONITORING:
                MonitorTaskModel monitorTaskModel = new MonitorTaskModel();
                ThriftUtils.createThriftFromBytes(taskModel.getSubTaskModel(), monitorTaskModel);
                return monitorTaskModel;
			case ENV_CLEANUP:
				// TODO return Environment Clean up task  model
			default:
				return null;
		}
	}

	public static void close(TServiceClient client) {
		if (client.getOutputProtocol().getTransport().isOpen()) {
			client.getOutputProtocol().getTransport().close();
		}
		if (client.getInputProtocol().getTransport().isOpen()) {
			client.getInputProtocol().getTransport().close();
		}
	}
}
