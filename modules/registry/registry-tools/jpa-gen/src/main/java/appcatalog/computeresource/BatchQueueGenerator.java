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
package appcatalog.computeresource;

import generators.JPAClassGenerator;
import generators.JPAResourceClassGenerator;
import generators.SQLGenerator;

import java.util.Arrays;

import model.JPAClassModel;
import model.JPAResourceClassModel;
import model.SQLData;

public class BatchQueueGenerator {
	private static SQLData createSQLData() {
		SQLData data = new SQLData();
		data.setTableName("BATCH_QUEUE");
		data.getFieldData().put("COMPUTE_RESOURCE_ID", Arrays.asList(new String[]{"VARCHAR", "(255)", "NOT", "NULL"}));
		data.getFieldData().put("QUEUE_NAME", Arrays.asList(new String[]{"VARCHAR", "(255)", "NOT", "NULL"}));
		data.getFieldData().put("QUEUE_DESCRIPTION", Arrays.asList(new String[]{"VARCHAR", "(255)",}));
		data.getFieldData().put("MAX_RUNTIME", Arrays.asList(new String[]{"INTEGER"}));
		data.getFieldData().put("MAX_NODES", Arrays.asList(new String[]{"INTEGER"}));
		data.getFieldData().put("MAX_PROCESSORS", Arrays.asList(new String[]{"INTEGER"}));
		data.getFieldData().put("MAX_JOB_IN_QUEUE", Arrays.asList(new String[]{"INTEGER"}));
		data.getPrimaryKeys().add("COMPUTE_RESOURCE_ID");
		data.getPrimaryKeys().add("QUEUE_NAME");
		data.getForiegnKeys().put("COMPUTE_RESOURCE_ID", new SQLData.ForiegnKeyData("COMPUTE_RESOURCE(RESOURCE_ID)","ComputeResource","ComputeHostResource"));
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
		jpaClassGenerator.setJpaClassPackageName("org.apache.aiaravata.application.catalog.data.model");
		JPAClassModel model = jpaClassGenerator.createJPAClassModel(data);
		System.out.println(jpaClassGenerator.generateJPAClass(model));
		System.out.println(jpaClassGenerator.generateJPAPKClass(model.pkClassModel));
		System.out.println(jpaClassGenerator.generatePersistenceXmlEntry(model));
	}
	
	public static void testJPAResourceClassGen() {
		SQLData data = createSQLData();
		JPAClassGenerator jpaClassGenerator = new JPAClassGenerator();
		JPAClassModel model = jpaClassGenerator.createJPAClassModel(data);
		JPAResourceClassGenerator jpaResourceClassGenerator = new JPAResourceClassGenerator();
		jpaResourceClassGenerator.setExceptionClassName("AppCatalogException");
		jpaResourceClassGenerator.setJpaUtilsClassName("AppCatalogJPAUtils");
		jpaResourceClassGenerator.setResourceTypeClassName("AppCatalogResourceType");
		jpaResourceClassGenerator.setQueryGeneratorClassName("AppCatalogQueryGenerator");

		JPAResourceClassModel model2 = jpaResourceClassGenerator.createJPAResourceClassModel(model);
		System.out.println(jpaResourceClassGenerator.generateJPAResourceClass(model2));
		System.out.println(jpaResourceClassGenerator.generateAbstractResourceClassUpdates(model2));
		System.out.println(jpaResourceClassGenerator.generateAppCatalogResourceTypeUpdates(model2));
		System.out.println(jpaResourceClassGenerator.generateAppCatalogJPAUtilUpdates(model2));
		
	}
	public static void main(String[] args) {
		testSqlGen();
		testJPAClassGen();
		testJPAResourceClassGen();
	}
}
