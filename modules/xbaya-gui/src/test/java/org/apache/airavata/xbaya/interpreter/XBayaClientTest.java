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
package org.apache.airavata.xbaya.interpreter;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.ListenerManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XBayaClientTest {
    final static Logger logger = LoggerFactory.getLogger(XBayaClientTest.class);

    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            logger.info("{} being run...", method.getName());
        }
    };

    @Test
	public void testInvokeWorkflowString() {
//		  try {
//              ListenerManager manager = axis2ServiceStarter();
//              XBayaClient xBayaClient = new XBayaClient("xbaya1.properties");
//              xBayaClient.loadWorkflowFromaFile("/Users/lahirugunathilake/localsimplemath.xwf");
//              NameValue[] nameValues = xBayaClient.setInputs("xbaya1.properties");
//              String s = xBayaClient.runWorkflow("test",nameValues,"admin");
//              org.junit.Assert.assertEquals("test",s);

//              manager.stop();*/
//          }  catch (URISyntaxException e) {
//			TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

    private ListenerManager axis2ServiceStarter() throws AxisFault {
        try {
            ConfigurationContext configContext = ConfigurationContextFactory.createBasicConfigurationContext
                    ("axis2_default.xml");
            AxisService service = AxisService.createService(EchoService.class.getName(), configContext.getAxisConfiguration());
            configContext.deployService(service);
            ListenerManager manager = new ListenerManager();
            manager.init(configContext);
            manager.start();
            return manager;
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }
}
