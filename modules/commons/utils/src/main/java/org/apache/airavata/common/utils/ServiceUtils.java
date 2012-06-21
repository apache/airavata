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

package org.apache.airavata.common.utils;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.util.Utils;

public class ServiceUtils {
    private static final String REPOSITORY_PROPERTIES = "repository.properties";

	public static String generateServiceURLFromConfigurationContext(
			ConfigurationContext configctx, String serviceName) throws IOException,
			SocketException {
		URL propurl = ServiceUtils.class.getClassLoader()
				.getResource(REPOSITORY_PROPERTIES);
		Properties propFile = new Properties();
		propFile.load(propurl.openStream());
		Map<String, String> map = new HashMap<String, String>((Map) propFile);
		String localAddress = Utils.getIpAddress(configctx
				.getAxisConfiguration());
		TransportInDescription transportInDescription = configctx
				.getAxisConfiguration().getTransportsIn().get("http");
		String port;
		if (transportInDescription != null
				&& transportInDescription.getParameter("port") != null) {
			port = (String) transportInDescription.getParameter("port")
					.getValue();
		} else {
			port = map.get("port");
		}
		localAddress = "http://" + localAddress + ":" + port;
		localAddress = localAddress + "/" + configctx.getContextRoot() + "/"
				+ configctx.getServicePath() + "/" + serviceName;
		return localAddress;
	}
}
