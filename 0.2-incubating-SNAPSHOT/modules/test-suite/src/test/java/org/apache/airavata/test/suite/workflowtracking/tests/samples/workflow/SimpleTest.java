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

package org.apache.airavata.test.suite.workflowtracking.tests.samples.workflow;

/**
 * Simple test that generates notifications from the perspective of an application (e.g. Jython script or web service)
 * or a Workflow engine. The following 1 service workflow is simulated:
 * 
 * <pre>
 * ................ ________
 * ........001 --->|        |---> XXXX
 * ................| FuBar1 |
 * ........002 --->|________|---> YYYY
 * </pre>
 * 
 */
public class SimpleTest {

    // public static long runWorkflowEngine(Notifier notifier, int count) throws Exception {
    //
    // Timer t = Timer.initAndStart("wfEngine");
    // for (int i=0; i < count; i++) {
    //
    // // start running the workflow
    // notifier.workflowInvoked(
    // // optional input param names to workflow
    // new String[]{"dataFileID", "confFileURL"},
    // // optional input param values to workflow
    // new String[]{"leadId:dgk8634yas", "gridftp://tempuri.org/conf.txt"}
    // );
    //
    // // start to invoke a service
    // ServiceObj svcObj =
    // notifier.invokeServiceStarted(
    // // optional input param names to service
    // new String[]{"dataFileID", "confFileURL"},
    // // optional input param values to service
    // new String[]{"leadId:dgk8634yas", "gridftp://tempuri.org/conf.txt"},
    // "Node1", // node ID in workflow
    // "step1", // timestep in workflow execution
    // "{http://foobar.org}FuBar1" // Servce QName
    // );
    //
    // // invocation returned successfully
    // notifier.serviceFinishedSuccess(svcObj);
    //
    // // OR invocation failed
    // // notifier.serviceFinishedFailed(svcObj, "service failed");
    //
    // // workflow completed successfully
    // notifier.workflowFinishedSuccess();
    //
    // // OR workflow failed
    // // notifier.workflowFinishedFailed("i failed!");
    //
    // // Done with workflow. call flush to ensure messages are sent...
    // notifier.flush();
    // }
    //
    // return t.end("done: " + count);
    // }
    //
    // public static long runService(Notifier notifier, int count) throws Exception {
    //
    // String done = "done " + count;
    // Timer t = Timer.initAndStart("service"); // start timer
    // for (int i=0; i < count; i++) {
    //
    // // APP STARTS
    // notifier.serviceInvoked("FuBarApp1"); // we can optionally pass the input params & values too
    //
    // // START TO RECEIVE A FILE
    // DataObj fileObj =
    // notifier.fileReceiveStarted("leadId-001", // leadID
    // "gridftp://remotehost.org/foo.dat", // remote file source
    // "/data/tmp/foo.dat"); // local file destination
    // /* APP DOES FILE TRANSFER ... */
    // // DONE RECEIVING FILE
    // notifier.fileReceiveFinished(fileObj);
    //
    // // THE RECEIVED FILE WILL BE CONSUMED IN A COMPUTATION
    // // ALONG WITH ANOTHER LOCAL FILE
    // notifier.dataConsumed(fileObj);
    // notifier.dataConsumed("leadId-002", "/etc/app/config/foo.cfg");
    //
    // // send info message on some processing
    // int runs = 2;
    // notifier.info("i'm going to use input file for " + runs + " number of WRF runs ...");
    //
    // // START COMPUTATION RUNS
    // for (int j=0; j < runs; j++) {
    // // START COMPUTATION
    // DurationObj compObj = notifier.computationStarted("WRF Run");
    // /* APP DOES COMPUTATION ... */
    // // FINISHED COMPUTATION
    // notifier.computationFinished(compObj);
    // }
    //
    // // NOTIFY THAT A FILE WAS PRODUCED
    // DataObj fileObj2 = notifier.fileProduced("/data/tmp/output/bar.dat");
    //
    // // START SENDING OUTPUT FILE TO REMOTE LOCATION
    // notifier.fileSendStarted(fileObj2, "gridftp://remotehost.org/bar.dat");
    // /* APP DOES FILE TRANSFER ... */
    // // DONE SENDING FILE
    // notifier.fileSendFinished(fileObj2);
    //
    // // PUBLISH THE OUTPUT FILE URL AS MESSAGE FOR VIZ
    // notifier.publishURL("Output visualization", "http://localhost/" + fileObj2.getLocalLocation());
    //
    // // APPLICATION COMPLETES
    // notifier.sendingResult(null, null);
    //
    // // DONE SENDING NOTIFICATIONS. CALL FLUSH TO ENSURE ALL MESSAGES HAVE BEEN PUBLISHED...
    // notifier.flush();
    // }
    // return t.end(done); // end timer
    //
    // }
    //
    // public static void main(String[] args) throws Exception {
    //
    // System.out.println("USAGE: java SimpleTest [service | engine] <iterations>");
    //
    // boolean runAsEngine = "engine".equalsIgnoreCase(args[0]); // run test as if from workflow engine
    // boolean runAsService = "service".equalsIgnoreCase(args[0]); // run test as if from application service
    // if (!runAsEngine && !runAsService) {
    // throw new Exception("pass either 'service' or 'engine' as param");
    // }
    //
    //
    // int count = 1;
    // try {
    // count = Integer.parseInt(args[1]);
    // } catch (NumberFormatException e) {
    // // ignore and use count = 1
    // }
    //
    // System.out.println("Running as : " + (runAsEngine ? "ENGINE" : "SERVICE") +
    // " for " + count + " iterations");
    //
    // // create the properties constructor...see CONSTS.java for more options
    // // Passing System.getProperties() ensures that any system properties that are set are
    // // carried over into the constructor
    // Props props =
    // //Props.newProps(System.getProperties()).
    // Props.newProps("properties.xml").
    // set(CONSTS.WORKFLOW_ID, "wf100201").
    // set(CONSTS.ENABLE_NAME_RESOLVER, "false").
    // // set(NAME_RESOLVER_URL, ""). // since name resolver is disabled, we do not need URL
    // //set(IN_XML_MESSAGE, ""). // pass the incoming SOAP message if async response to sender required
    // set(CONSTS.BROKER_URL, "rainier.extreme.indiana.edu:12346").
    // set(CONSTS.TOPIC, "somerandomtopic");
    //
    // // when running from the service perspective, additional attributes are need
    // // ...not needed when running as workflow engine
    // if (runAsService) {
    // props.
    // /* either this service's QName should be passed as SERVICE_ID or */
    // /* its WSDL should be passed as SERVICE_WSDL to extract its QName */
    // set(CONSTS.SERVICE_ID, "{http://foobar.org}FuBar1").
    // //set(CONSTS.SERVICE_WSDL, "<wsdl:definitions/>").
    // set(CONSTS.NODE_ID, "N1").
    // set(CONSTS.TIMESTEP, "1");
    // }
    //
    // // creates the default notifier and passes above properties to it as parameters
    // Notifier notifier = NotifierFactory.createNotifier(props);
    //
    // long time = -1;
    // if (runAsService) {
    // time = runService(notifier, count);
    //
    // }
    //
    // if (runAsEngine) {
    // time = runWorkflowEngine(notifier, count);
    // }
    //
    // System.out.println("Done in " + time + " millis");
    // }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (C) 2004 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice, the list of authors in the original
 * source code, this list of conditions and the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * disclaimer listed in this license in the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme! Lab. For further information please
 * visit http://org.apache.airavata/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and wherever such third-party acknowledgments
 * normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall not be used to endorse or promote
 * products derived from this software without prior written permission from Indiana University. For written permission,
 * please contact http://org.apache.airavata/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name nor may "Indiana University" appear in
 * their name, without prior written permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided does not infringe the patent or any other
 * intellectual property rights of any other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE
 * MADE. INDIANA UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF INFRINGEMENT OF
 * THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS. INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS
 * FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE. LICENSEE ASSUMES THE
 * ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF
 * INFORMATION GENERATED USING SOFTWARE.
 */

