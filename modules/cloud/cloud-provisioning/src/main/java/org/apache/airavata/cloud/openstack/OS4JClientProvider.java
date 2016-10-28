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
package org.apache.airavata.cloud.openstack;

import java.util.Properties;

import org.apache.airavata.cloud.util.Constants;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;

public class OS4JClientProvider {

	private static OSClient os = null;
	private static Integer apiVersion = null;


	public static OSClient getOSClient(Properties properties) {

		try {
			String endPoint = properties.getProperty(Constants.OS_AUTH_URL);
			String userName = properties.getProperty(Constants.OS_USERNAME);
			String password = properties.getProperty(Constants.OS_PASSWORD);
			String domain = properties.getProperty(Constants.OS_USER_DOMAIN_NAME);
			String apiVersion = properties.getProperty(Constants.OS_IDENTITY_API_VERSION);

			// Initialize client for api version 3.
			if( apiVersion.equals("3") ) {
				String project = properties.getProperty(Constants.OS_PROJECT_DOMAIN_NAME);
				getOSClientV3(endPoint, userName, password, domain, project);
			}
			// Initialize client for api version 2.
			else if( apiVersion.equals("2") ) {
				getOSClientV2(endPoint, userName, password, domain);
			}
			else {
				throw new Exception("Non- supported Openstack API version " + properties.getProperty("OS_IDENTITY_API_VERSION"));
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			// TODO: Check with the team on how to handle exceptions.
		}
		return os;
	}

	/**
	 * Function that authenticates to openstack using v3 APIs.
	 * @param endPoint
	 * @param userName
	 * @param password
	 * @param domain
	 * @param project
	 * @return OSClient object that can be used for other OpenStack operations.
	 */
	public static void getOSClientV3(String endPoint, String userName, String password,
			String domain, String project) {

		if(os == null || apiVersion == null ||apiVersion != 3 || ! os.getEndpoint().equals(endPoint)) {

			Identifier domainIdentifier = Identifier.byName(domain);
			Identifier projectIdentifier = Identifier.byName(project);
			os = OSFactory.builderV3()
					.scopeToProject(projectIdentifier, domainIdentifier)
					.endpoint(endPoint)
					.credentials(userName, password, domainIdentifier)
					.authenticate();

			apiVersion = 3;
		}
	}

	/**
	 * Function that authenticates to openstack using v2 APIs.
	 * @param endPoint
	 * @param userName
	 * @param password
	 * @param domain
	 * @param project
	 * @return OSClient object that can be used for other Openstack operations.
	 */
	public static void getOSClientV2(String endPoint, String userName, String password,
			String domain) {

		if(os == null || apiVersion == null ||apiVersion != 2 || ! os.getEndpoint().equals(endPoint)) {

			Identifier domainIdentifier = Identifier.byName(domain);
			//Identifier projectIdentifier = Identifier.byName(project);
			os = OSFactory.builderV3()
					//.scopeToProject(projectIdentifier, domainIdentifier)
					.endpoint(endPoint)
					.credentials(userName, password, domainIdentifier)
					.authenticate();

			apiVersion = 2;
		}
	}
}
