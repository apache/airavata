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

package org.apache.airavata.commons.gfac.type;

import org.apache.airavata.schemas.gfac.HostDescriptionDocument;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;

public class HostDescription implements Type {

	private HostDescriptionDocument hostDocument;

	public HostDescription() {
		this.hostDocument = HostDescriptionDocument.Factory.newInstance();
		this.hostDocument.addNewHostDescription();
	}

	public HostDescription(SchemaType type) {
		this();
		this.hostDocument.getHostDescription().changeType(type);
	}

	public HostDescriptionType getType() {
		return this.hostDocument.getHostDescription();
	}

	public String toXML() {
		return hostDocument.xmlText();
	}

	public static HostDescription fromXML(String xml) throws XmlException {
		HostDescription host = new HostDescription();
		host.hostDocument = HostDescriptionDocument.Factory.parse(xml);
		return host;
	}
}