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

import org.apache.airavata.commons.gfac.type.Type;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.airavata.schemas.gfac.GFacParameterDocument;
import org.apache.airavata.schemas.gfac.ParameterType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;

public class ActualParameter implements Type {

	private static final long serialVersionUID = -6022759981837350675L;
	private GFacParameterDocument paramDoc;

	public ActualParameter() {
		this.paramDoc = GFacParameterDocument.Factory.newInstance();
		this.paramDoc.addNewGFacParameter();

		// default type is String
		this.paramDoc.getGFacParameter().changeType(StringParameterType.type);
	}

	public ActualParameter(SchemaType type) {
		this.paramDoc = GFacParameterDocument.Factory.newInstance();
		this.paramDoc.addNewGFacParameter();
		this.paramDoc.getGFacParameter().changeType(type);
	}

	public ParameterType getType() {
		return this.paramDoc.getGFacParameter();
	}

	public boolean hasType(DataType.Enum type) {
		return this.paramDoc.getGFacParameter().getType() == type;
	}

	public String toXML() {
		return this.paramDoc.xmlText();
	}

    public void setParamDoc(GFacParameterDocument paramDoc) {
        this.paramDoc = paramDoc;
    }

    public static ActualParameter fromXML(String xml) throws XmlException {
		ActualParameter param = new ActualParameter();
		param.paramDoc = GFacParameterDocument.Factory.parse(xml);
		return param;
	}
}