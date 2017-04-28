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
package test;

import generators.JPAClassGenerator;
import generators.JPAResourceClassGenerator;
import generators.SQLGenerator;

import java.util.Arrays;

import model.JPAClassModel;
import model.JPAResourceClassModel;
import model.SQLData;

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

public class Test {


	private static SQLData createSQLData() {
		SQLData data = new SQLData();
		data.setTableName("COMMUNITY_USER");
		data.getFieldData().put("GATEWAY_NAME", Arrays.asList(new String[]{"VARCHAR", "256", "NOT", "NULL"}));
		data.getFieldData().put("COMMUNITY_USER_NAME", Arrays.asList(new String[]{"VARCHAR", "256", "NOT", "NULL"}));
		data.getFieldData().put("TOKEN_ID", Arrays.asList(new String[]{"VARCHAR", "256", "NOT", "NULL"}));
		data.getFieldData().put("COMMUNITY_USER_EMAIL", Arrays.asList(new String[]{"VARCHAR", "256", "NOT", "NULL"}));
		data.getFieldData().put("CREATION_TIME", Arrays.asList(new String[]{"TIMESTAMP", "DEFAULT", "NOW()"}));
		data.getFieldData().put("CPU_COUNT", Arrays.asList(new String[]{"INTEGER"}));
		data.getPrimaryKeys().add("GATEWAY_NAME");
		data.getPrimaryKeys().add("COMMUNITY_USER_NAME");
		data.getPrimaryKeys().add("TOKEN_ID");
		data.getForiegnKeys().put("EXPERIMENT_ID", new SQLData.ForiegnKeyData("EXPERIMENT(EXPERIMENT_ID)","Experiment","ExperimentResource"));
		return data;
	}
	public static void testSqlGen() {
		SQLData data = createSQLData();
		SQLGenerator sqlGenerator = new SQLGenerator();
		System.out.println(sqlGenerator.generateSQLCreateQuery(data));
	}
	
	public static void testJPAClassGen() {
		SQLData data = createSQLData();
		JPAClassGenerator jpaClassGenerator = new JPAClassGenerator();
		JPAClassModel model = jpaClassGenerator.createJPAClassModel(data);
		System.out.println(jpaClassGenerator.generateJPAClass(model));
		System.out.println(jpaClassGenerator.generateJPAPKClass(model.pkClassModel));
	}
	
	public static void testJPAResourceClassGen() {
		SQLData data = createSQLData();
		JPAClassGenerator jpaClassGenerator = new JPAClassGenerator();
		JPAClassModel model = jpaClassGenerator.createJPAClassModel(data);
		JPAResourceClassGenerator jpaResourceClassGenerator = new JPAResourceClassGenerator();
		JPAResourceClassModel model2 = jpaResourceClassGenerator.createJPAResourceClassModel(model);
		System.out.println(jpaResourceClassGenerator.generateJPAResourceClass(model2));
		System.out.println(jpaResourceClassGenerator.generateAbstractResourceClassUpdates(model2));
		System.out.println(jpaResourceClassGenerator.generateAppCatalogResourceTypeUpdates(model2));
		System.out.println(jpaResourceClassGenerator.generateAppCatalogJPAUtilUpdates(model2));
		
	}
	public static void main(String[] args) {
		testJPAResourceClassGen();
	}
}
