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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class SQLData {
	private String tableName;
	private Map<String,List<String>> fieldData;
	private List<String> primaryKeys;
	private Map<String,ForiegnKeyData> foriegnKeys;
	
	public static class ForiegnKeyData{
		public String tableAndField;
		public String jpaClassName;
		public String jpaResourceClassName;
		public ForiegnKeyData(String tableAndField, String jpaClassName,String jpaResourceClassName) {
			this.tableAndField = tableAndField;
			this.jpaClassName = jpaClassName;
			this.jpaResourceClassName = jpaResourceClassName;
		}
	}
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public Map<String, List<String>> getFieldData() {
		if (fieldData==null){
			fieldData=new HashMap<String, List<String>>();
		}
		return fieldData;
	}
	
	public void setFieldData(Map<String, List<String>> fieldData) {
		this.fieldData = fieldData;
	}
	public List<String> getPrimaryKeys() {
		if (primaryKeys==null){
			primaryKeys=new ArrayList<String>();
		}
		return primaryKeys;
	}
	public void setPrimaryKeys(List<String> primaryKeys) {
		this.primaryKeys = primaryKeys;
	}
	public Map<String,ForiegnKeyData> getForiegnKeys() {
		if (foriegnKeys==null){
			foriegnKeys=new HashMap<String, ForiegnKeyData>();
		}
		return foriegnKeys;
	}
	public void setForiegnKeys(Map<String,ForiegnKeyData> foriegnKeys) {
		this.foriegnKeys = foriegnKeys;
	}
}
