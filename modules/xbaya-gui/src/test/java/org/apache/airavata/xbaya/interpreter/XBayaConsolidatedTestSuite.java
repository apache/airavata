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
//package org.apache.airavata.xbaya.interpreter;
//
//import org.apache.airavata.xbaya.interpreter.utils.WorkflowTestUtils;
//import org.apache.axis2.AxisFault;
//import org.apache.axis2.engine.ListenerManager;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Rule;
//import org.junit.rules.MethodRule;
//import org.junit.rules.TestWatchman;
//import org.junit.runner.RunWith;
//import org.junit.runners.Suite;
//import org.junit.runners.model.FrameworkMethod;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//@RunWith(Suite.class)
//@Suite.SuiteClasses({XBayaClientTest.class, SimpleMathWorkflowTest.class, WorkflowTest.class,
//        ComplexMathWorkflowTest.class, CrossProductWorkflowTest.class, ForEachWorkflowTest.class,
//        SimpleForEachWorkflowTest.class, ComplexForEachWorkflowTest.class,
//        WorkflowTrackingTest.class, RegistryServiceTest.class})
//public class XBayaConsolidatedTestSuite {
//    private static ListenerManager manager = null;
//
//    final static Logger logger = LoggerFactory.getLogger(XBayaConsolidatedTestSuite.class);
//
//    @Rule
//    public MethodRule watchman = new TestWatchman() {
//        public void starting(FrameworkMethod method) {
//            logger.info("{} being run...", method.getName());
//        }
//    };
//
//    @BeforeClass
//    public static void startServer() throws AxisFault {
//        logger.info("Starting simple Axis2 Server...");
//        manager = WorkflowTestUtils.axis2ServiceStarter();
//    }
//
//    @AfterClass
//    public static void stopServer() throws AxisFault {
//        logger.info("Stopping simple Axis2 Server...");
//        manager.stop();
//    }
//
//}
