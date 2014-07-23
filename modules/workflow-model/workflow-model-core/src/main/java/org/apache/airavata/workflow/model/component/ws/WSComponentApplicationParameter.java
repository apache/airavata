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

package org.apache.airavata.workflow.model.component.ws;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;

@XmlRootElement(name="Parameter")
@XmlType(propOrder = {"name", "type", "description", "defaultValue"})
public class WSComponentApplicationParameter {
	private String name;
	private QName type;
	private String description;
	private String defaultValue;
	
	public WSComponentApplicationParameter() {
	}
	
	public WSComponentApplicationParameter(String name, QName type,
			String description, String defaultValue) {
		this.name = name;
		this.type = type;
		this.description = description;
		this.defaultValue = defaultValue;
	}

	@XmlAttribute (required = true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute (required = true)
	public QName getType() {
		return type;
	}
	public void setType(QName type) {
		this.type = type;
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
	
}
