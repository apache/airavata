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
//*/
//package org.apache.airavata.xbaya.interpreter.utils;
//
//import org.apache.airavata.client.stub.interpretor.NameValue;
//import org.apache.airavata.xbaya.XBayaConfiguration;
//import org.apache.airavata.xbaya.XBayaConstants;
//import org.apache.airavata.xbaya.interpreter.EchoService;
//import org.apache.airavata.xbaya.interpreter.LevenshteinDistanceService;
//import org.apache.airavata.xbaya.interpreter.ComplexMathService;
//import org.apache.airavata.xbaya.interpretor.HeaderConstants;
//import org.apache.airavata.xbaya.interpretor.WorkflowInterpretorSkeleton;
//import org.apache.axis2.AxisFault;
//import org.apache.axis2.context.ConfigurationContext;
//import org.apache.axis2.context.ConfigurationContextFactory;
//import org.apache.axis2.description.AxisService;
//import org.apache.axis2.engine.ListenerManager;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.nio.MappedByteBuffer;
//import java.nio.channels.FileChannel;
//import java.nio.charset.Charset;
//import java.util.HashMap;
//import java.util.Map;
//
//
//public class WorkflowTestUtils implements HeaderConstants{
//
//    public static XBayaConfiguration getConfiguration() throws URISyntaxException {
//        Map<String, String> configuration = new HashMap<String, String>();
//        configuration.put(HEADER_ELEMENT_GFAC,XBayaConstants.DEFAULT_GFAC_URL.toString());
//        configuration.put(HEADER_ELEMENT_REGISTRY,XBayaConstants.REGISTRY_URL.toASCIIString());
//        configuration.put(HEADER_ELEMENT_PROXYSERVER,XBayaConstants.DEFAULT_MYPROXY_SERVER);
//        configuration.put(HEADER_ELEMENT_MSGBOX,XBayaConstants.DEFAULT_MESSAGE_BOX_URL.toString());
//        configuration.put(HEADER_ELEMENT_DSC,XBayaConstants.DEFAULT_DSC_URL.toString());
//        configuration.put(HEADER_ELEMENT_BROKER,XBayaConstants.DEFAULT_BROKER_URL.toString());
//        return (new WorkflowInterpretorSkeleton()).getConfiguration(configuration);
//    }
//
//    public static String readWorkflow(URL url) throws IOException, URISyntaxException {
//        FileInputStream stream = new FileInputStream(new File(url.toURI()));
//        try {
//            FileChannel fc = stream.getChannel();
//            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
//            /* Instead of using default, pass in a decoder. */
//            return Charset.defaultCharset().decode(bb).toString();
//        } finally {
//            stream.close();
//        }
//    }
//
//    public static ListenerManager axis2ServiceStarter() throws AxisFault {
//        try {
//            ConfigurationContext configContext = ConfigurationContextFactory
//                    .createBasicConfigurationContext("axis2_default.xml");
//
//            AxisService echoService = AxisService.createService(EchoService.class.getName(),
//                    configContext.getAxisConfiguration());
//            configContext.deployService(echoService);
//            AxisService distanceService = AxisService.createService(LevenshteinDistanceService.class.getName(),
//                    configContext.getAxisConfiguration());
//            configContext.deployService(distanceService);
//            AxisService mathService = AxisService.createService(ComplexMathService.class.getName(),
//                    configContext.getAxisConfiguration());
//            configContext.deployService(mathService);
//
//            ListenerManager manager = new ListenerManager();
//            manager.init(configContext);
//            manager.start();
//            return manager;
//        } catch (Exception e) {
//            throw AxisFault.makeFault(e);
//        }
//    }
//}
