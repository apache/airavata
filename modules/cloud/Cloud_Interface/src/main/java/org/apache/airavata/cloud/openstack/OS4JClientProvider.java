package org.apache.airavata.cloud.openstack;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;

/**
 * Provider class for Openstack Client using Openstack4j.
 * @author Mangirish Wagle
 *
 */
public class OS4JClientProvider {

	private static OSClient os = null;
	private static Integer apiVersion = null;

	/**
	 * Function that authenticates to openstack using v3 APIs.
	 * @param endPoint
	 * @param userName
	 * @param password
	 * @param domain
	 * @param project
	 * @return OSClient object that can be used for other Openstack operations.
	 */
	public static OSClient getOSClientV3(String endPoint, String userName, String password,
			String domain, String project){

		/*
		System.out.println("Endpoint: " + endPoint);
		System.out.println("userName: " + userName);
		System.out.println("password: " + password);
		System.out.println("domain: " + domain);
		System.out.println("project: " + project);
		*/

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

		return os;
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
	public static OSClient getOSClientV2(String endPoint, String userName, String password,
			String domain, String project){

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

		return os;
	}
}
