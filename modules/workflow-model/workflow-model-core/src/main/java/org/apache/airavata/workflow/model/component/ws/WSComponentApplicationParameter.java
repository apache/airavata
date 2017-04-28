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
package org.apache.airavata.workflow.model.component.ws;


import org.apache.airavata.model.application.io.DataType;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="Parameter")
@XmlType(propOrder = {"name", "type", "description", "defaultValue", "applicationArgument", "inputOrder"})
public class WSComponentApplicationParameter {
	private String name;
//	private QName type;
	private String description;
	private String defaultValue;
	private String applicationArgument;
	private int inputOrder;
	private DataType type;

	public WSComponentApplicationParameter() {
	}

	public WSComponentApplicationParameter(String name, DataType type, String description, String defaultValue) {
		this(name, type, description, defaultValue, "", -1);
	}

	public WSComponentApplicationParameter(String name, DataType type, String description, String defaultValue, int inputOrder) {
		this(name, type, description, defaultValue, "", inputOrder);
	}

	public WSComponentApplicationParameter(String name, DataType type,
			String description, String defaultValue, String applicationArgument, int inputOrder) {
		this.name = name;
		this.description = description;
		this.defaultValue = defaultValue;
		this.applicationArgument = applicationArgument;
		this.inputOrder = inputOrder;
		this.type = type;
	}

	@XmlAttribute (required = true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute (required = false)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@XmlValue
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@XmlAttribute
	public String getApplicationArgument() {
		return applicationArgument;
	}

	public void setApplicationArgument(String applicationArgument) {
		this.applicationArgument = applicationArgument;
	}

	@XmlAttribute
	public int getInputOrder() {
		return inputOrder;
	}

	public void setInputOrder(int inputOrder) {
		this.inputOrder = inputOrder;
	}

	@XmlAttribute
	public DataType getType() {
		return type;
	}

	public void setType(DataType type) {
		this.type = type;
	}
}
