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
package org.apache.airavata.gfac.core.config;

import org.apache.airavata.model.data.movement.DataMovementProtocol;

import java.util.HashMap;
import java.util.Map;

public class DataTransferTaskConfig {
	private DataMovementProtocol transferProtocol;
	private String taskClass;
	private Map<String,String> properties = new HashMap<>();


	public DataMovementProtocol getTransferProtocol() {
		return transferProtocol;
	}

	public void setTransferProtocol(DataMovementProtocol transferProtocol) {
		this.transferProtocol = transferProtocol;
	}

	public String getTaskClass() {
		return taskClass;
	}

	public void setTaskClass(String taskClass) {
		this.taskClass = taskClass;
	}

	public void addProperty(String key, String value) {
		properties.put(key, value);
	}

	public void addProperties(Map<String, String> propMap) {
		propMap.forEach(properties::put);
	}

	public Map<String,String> getProperties(){
		return properties;
	}
}
