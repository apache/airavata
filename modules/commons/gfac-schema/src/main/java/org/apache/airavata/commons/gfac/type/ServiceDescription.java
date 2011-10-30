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

import org.apache.airavata.schemas.gfac.ServiceDescriptionDocument;
import org.apache.airavata.schemas.gfac.ServiceDescriptionType;
import org.apache.xmlbeans.XmlException;

public class ServiceDescription implements Type {

	private static final long serialVersionUID = -4365350045872875217L;
	private ServiceDescriptionDocument serviceDocument;

	public ServiceDescription() {
		this.serviceDocument = ServiceDescriptionDocument.Factory.newInstance();
		this.serviceDocument.addNewServiceDescription();
	}

	public ServiceDescriptionType getType() {
		return this.serviceDocument.getServiceDescription();
	}

	public String toXML() {
		return serviceDocument.xmlText();
	}

	public static ServiceDescription fromXML(String xml) throws XmlException {
		ServiceDescription service = new ServiceDescription();
		service.serviceDocument = ServiceDescriptionDocument.Factory.parse(xml);
		return service;
	}
}
