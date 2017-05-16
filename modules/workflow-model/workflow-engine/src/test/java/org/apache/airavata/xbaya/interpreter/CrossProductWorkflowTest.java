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
//
//package org.apache.airavata.xbaya.interpreter;
//
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.util.UUID;
//
////import org.apache.airavata.registry.api.AiravataRegistry2;
//import org.apache.airavata.workflow.model.exceptions.WorkflowException;
//import org.apache.airavata.workflow.model.wf.Workflow;
//import org.apache.airavata.xbaya.XBayaConfiguration;
//import org.apache.airavata.xbaya.interpreter.utils.WorkflowTestUtils;
//import org.apache.airavata.xbaya.interpretor.SSWorkflowInterpreterInteractorImpl;
//import org.apache.airavata.xbaya.interpretor.StandaloneNotificationSender;
//import org.apache.airavata.xbaya.interpretor.WorkflowInterpreter;
//import org.apache.airavata.xbaya.interpretor.WorkflowInterpreterConfiguration;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.MethodRule;
//import org.junit.rules.TestWatchman;
//import org.junit.runners.model.FrameworkMethod;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class CrossProductWorkflowTest {
//    final Logger logger = LoggerFactory.getLogger(CrossProductWorkflowTest.class);
//
//    @Rule
//    public MethodRule watchman = new TestWatchman() {
//        public void starting(FrameworkMethod method) {
//            logger.info("{} being run...", method.getName());
//        }
//    };
//
//    @Test
//    public void testScheduleDynamically() throws IOException, URISyntaxException, WorkflowException {
//        logger.info("Running CrossProductWorkflowTest...");
//        URL systemResource = this.getClass().getClassLoader().getSystemResource("LevenshteinDistance.xwf");
//        Workflow workflow = new Workflow(WorkflowTestUtils.readWorkflow(systemResource));
//        XBayaConfiguration conf = WorkflowTestUtils.getConfiguration();
////        AiravataRegistry2 registry = conf.getJcrComponentRegistry()==null? null:conf.getJcrComponentRegistry().getExperimentCatalog();
////		WorkflowInterpreterConfiguration workflowInterpreterConfiguration = new WorkflowInterpreterConfiguration(workflow, UUID.randomUUID().toString(),conf.getMessageBoxURL(), conf.getBrokerURL(), registry, conf, null,null,true);
//		WorkflowInterpreterConfiguration workflowInterpreterConfiguration = new WorkflowInterpreterConfiguration(workflow,
//                UUID.randomUUID().toString(),conf.getMessageBoxURL(), conf.getBrokerURL(), conf.getAiravataAPI(), conf, null,null,true);
//		workflowInterpreterConfiguration.setNotifier(new StandaloneNotificationSender(workflowInterpreterConfiguration.getTopic(),workflowInterpreterConfiguration.getWorkflow()));
//
//        WorkflowInterpreter interpretor = new WorkflowInterpreter(workflowInterpreterConfiguration,new SSWorkflowInterpreterInteractorImpl());
//        interpretor.scheduleDynamically();
//    }
//}
