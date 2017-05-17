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
package model;
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


public class JPAClassField{
	public String tableColumnName;
	public String fieldName;
	public String fieldNameConstant;
	public String fieldDataType;
	public String fieldTitle;
	public boolean primaryKey;
	public boolean foriegnKey=false;
	public String foriegnKeyJPAClass;
	public String foriegnKeyJPAResourceClass;
	public JPAClassField(String tableColumnName, String fieldName,
			String fieldDataType, String fieldTitle, boolean primaryKey,boolean foriegnKey,String foriegnKeyJPAClass,String foriegnKeyJPAResourceClass) {
		this.tableColumnName = tableColumnName;
		this.fieldName = fieldName;
		this.fieldDataType = fieldDataType;
		this.fieldTitle = fieldTitle;
		this.primaryKey=primaryKey;
		this.foriegnKey=foriegnKey;
		this.foriegnKeyJPAClass=foriegnKeyJPAClass;
		this.foriegnKeyJPAResourceClass=foriegnKeyJPAResourceClass;
	}
	
	
}