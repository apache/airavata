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


public class DataType implements Type {

    private org.apache.airavata.schemas.gfac.DataType.Enum dataType;

    public DataType() {
        dataType = org.apache.airavata.schemas.gfac.DataType.Enum.forString("String");
    }

    public DataType(String type) {
        dataType = org.apache.airavata.schemas.gfac.DataType.Enum.forString(type);
    }

    public void setType(String type) {
        dataType = org.apache.airavata.schemas.gfac.DataType.Enum.forString(type);
    }

    public String getType() {
        return dataType.toString();
    }

    public String toString() {
        return dataType.toString();
    }
    
    public String toXml() {
    	return null;
    }

	public Type fromXml(String xml) {
		return null;
	}
}