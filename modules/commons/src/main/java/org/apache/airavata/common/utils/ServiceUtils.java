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
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.common.utils;
//
//import java.io.IOException;
//import java.net.SocketException;
//
//import org.apache.airavata.common.exception.ApplicationSettingsException;
//import org.apache.axis2.context.ConfigurationContext;
//import org.apache.axis2.description.TransportInDescription;
//import org.apache.axis2.util.Utils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class ServiceUtils {
//    private static final Logger log = LoggerFactory.getLogger(ServiceUtils.class);
////    private static final String REPOSITORY_PROPERTIES = "airavata-server.properties";
//    public static final String IP = "ip";
//    public static final String PORT = "port";
//
//	public static String generateServiceURLFromConfigurationContext(
//			ConfigurationContext context, String serviceName) throws IOException, ApplicationSettingsException {
////		URL url = ServiceUtils.class.getClassLoader()
////				.getResource(REPOSITORY_PROPERTIES);
//		 String localAddress = null;
//        String port = null;
////        Properties properties = new Properties();
//        try {
//            localAddress = ServerSettings.getSetting(IP);
//        } catch (ApplicationSettingsException e) {
//			//we will ignore this exception since the properties file will not contain the values
//			//when it is ok to retrieve them from the axis2 context
//		}
//        if(localAddress == null){
//	        try {
//	            localAddress = Utils.getIpAddress(context
//	                    .getAxisConfiguration());
//	        } catch (SocketException e) {
//	            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//	        }
//        }
//        String protocol="http";
//        if(ServerSettings.isEnableHttps()){
//            protocol="https";
//        }
//
//        try {
//            port = ServerSettings.getTomcatPort(protocol);
//        } catch (ApplicationSettingsException e) {
//            //we will ignore this exception since the properties file will not contain the values
//            //when it is ok to retrieve them from the axis2 context
//        }
//        if (port == null) {
//            TransportInDescription transportInDescription = context
//                .getAxisConfiguration().getTransportsIn()
//                .get(protocol);
//            if (transportInDescription != null
//                && transportInDescription.getParameter(PORT) != null) {
//                port = (String) transportInDescription
//                    .getParameter(PORT).getValue();
//            }
//        }
//        localAddress = protocol+"://" + localAddress + ":" + port;
//        localAddress = localAddress + "/"
//    		//We are not using axis2 config context to get the context root because it is invalid
//            //+ context.getContextRoot() + "/"
//    		//FIXME: the context root will be correct after updating the web.xml 
//            + ServerSettings.getServerContextRoot() + "/"
//            + context.getServicePath() + "/"
//            + serviceName;
//        log.debug("Service Address Configured:" + localAddress);
//        return localAddress;
//	}
//}
