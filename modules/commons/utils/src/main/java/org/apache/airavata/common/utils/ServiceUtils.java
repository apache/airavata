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
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ServiceUtils {
    private static final Log log = LogFactory.getLog(ServiceUtils.class);
    private static final String REPOSITORY_PROPERTIES = "airavata-server.properties";
    public static final String IP = "ip";
    public static final String PORT = "port";

	public static String generateServiceURLFromConfigurationContext(
			ConfigurationContext context, String serviceName) throws IOException {
		URL url = ServiceUtils.class.getClassLoader()
				.getResource(REPOSITORY_PROPERTIES);
		 String localAddress = null;
        String port = null;
        Properties properties = new Properties();
        try {
            properties.load(url.openStream());
            localAddress = (String) properties.get(IP);
            port = (String) properties.get(PORT);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(localAddress == null){
        try {
            localAddress = Utils.getIpAddress(context
                    .getAxisConfiguration());
        } catch (SocketException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        }
        TransportInDescription transportInDescription = context
                .getAxisConfiguration().getTransportsIn()
                .get("http");
        if (port == null) {
            if (transportInDescription != null
                    && transportInDescription.getParameter("port") != null) {
                port = (String) transportInDescription
                        .getParameter("port").getValue();
            }
        }
        localAddress = "http://" + localAddress + ":" + port;
        localAddress = localAddress + "/"
                + context.getContextRoot() + "/"
                + context.getServicePath() + "/"
                + serviceName;
        log.debug("Service Address Configured:" + localAddress);
        return localAddress;
	}
}
