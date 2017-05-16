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
package org.apache.airavata.workflow.model.wf;


import org.apache.airavata.model.application.io.DataType;

public class WorkflowInput {
	private String name;
	private DataType type;
	private Object defaultValue;
	private Object value;
	private boolean optional;
	
	public WorkflowInput(String name,Object value) throws InvalidDataFormatException {
		this(name, null, null, value, false);
	}
	
	public WorkflowInput(String name,DataType type,Object defaultValue,Object value, boolean optional) throws InvalidDataFormatException {
		setName(name);
		setType(type);
		setDefaultValue(defaultValue);
		setValue(value);
		setOptional(optional);
	}
	
	public String getName() {
		return name;
	}
	
	private void setName(String name) {
		this.name = name;
	}
	
	public DataType getType() {
		return type;
	}
	
	private void setType(DataType type) {
		this.type = type;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	private void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public Object getValue() {
		return value;
	}
	
	private void validateData(Object data) throws InvalidDataFormatException{
		if (data!=null){
			//TODO validate against type
		}
	}
	
	public void setValue(Object value) throws InvalidDataFormatException {
		validateData(value);
		this.value = value;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}
}
