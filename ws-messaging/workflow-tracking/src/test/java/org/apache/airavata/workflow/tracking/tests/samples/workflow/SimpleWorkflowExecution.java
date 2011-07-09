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

package org.apache.airavata.workflow.tracking.tests.samples.workflow;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import org.apache.airavata.workflow.tracking.GenericNotifier;
import org.apache.airavata.workflow.tracking.Notifier;
import org.apache.airavata.workflow.tracking.NotifierFactory;
import org.apache.airavata.workflow.tracking.ProvenanceNotifier;
import org.apache.airavata.workflow.tracking.WorkflowNotifier;
import org.apache.airavata.workflow.tracking.calder.CalderNotifier;
import org.apache.airavata.workflow.tracking.client.Subscription;
import org.apache.airavata.workflow.tracking.common.AnnotationConsts;
import org.apache.airavata.workflow.tracking.common.AnnotationProps;
import org.apache.airavata.workflow.tracking.common.ConstructorConsts;
import org.apache.airavata.workflow.tracking.common.ConstructorProps;
import org.apache.airavata.workflow.tracking.common.InvocationContext;
import org.apache.airavata.workflow.tracking.common.InvocationEntity;
import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;
import org.apache.airavata.wsmg.client.ConsumerNotificationHandler;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.util.XMLPrettyPrinter;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.tool.XSTCTester.TestCase;
import org.junit.*;

public class SimpleWorkflowExecution extends TestCase {

    /**
     * This class is not instantiated. So have a private default constructor.
     * 
     */
    Subscription subscription;
    Properties configs = new Properties();
    String BROKER_URL = "http://127.0.0.1:8081/axis2/services/EventingService/topic/Foo";

    private EndpointReference epr = new EndpointReference(BROKER_URL);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        // URL configURL = ClassLoader
        // .getSystemResource(TestConfigKeys.CONFIG_FILE_NAME);
        // configs.load(configURL.openStream());
        // BROKER_URL = configs
        // .getProperty(TestConfigKeys.BROKER_EVENTING_SERVICE_EPR);
        // MESSAGEBOX_URL =
        // configs.getProperty(TestConfigKeys.MSGBOX_SERVICE_EPR);
        // consumerPort = Integer.parseInt(configs
        // .getProperty(TestConfigKeys.CONSUMER_PORT));
        // BROKER_EVENTING = configs
        // .getProperty(TestConfigKeys.BROKER_EVENTING_SERVICE_EPR);

        DATA_URI_1 = new URI("lead:djsdhgfsdf");
        DATA_URI_2 = new URI("lead:skfdjhgkfg");
        DATA_URI_3 = new URI("lead:hgkdfhkgfd");
        DATA_URI_4 = new URI("lead:lshjkhdgdf");
        DATA_URI_5 = new URI("lead:fghkfhgfdg");
        DATA_URI_6 = new URI("lead:dshiuwekds");

        DATA_URLS_1 = Arrays.asList(new URI[] { new URI("http://dataserver/foo/1") });
        DATA_URLS_2 = Arrays.asList(new URI[] { new URI("http://dataserver/bar/2") });
        DATA_URLS_3 = Arrays.asList(new URI[] { new URI("http://dataserver/fubar/3"),
                new URI("http://datarepos/foobar/3") });
        DATA_URLS_4 = Arrays.asList(new URI[] { new URI("http://datarepos/fee/4") });
        DATA_URLS_5 = Arrays.asList(new URI[] { new URI("http://datarepos/fie/5") });
        DATA_URLS_6 = Arrays.asList(new URI[] { new URI("http://datarepos/foe/fum/6") });

        long now = System.currentTimeMillis();
        SERVICE_0 = new URI("http://tempuri.org/root_service/" + now);
        WORKFLOW_1 = new URI("http://tempuri.org/workflow1/" + now);
        SERVICE_1 = new URI("http://tempuri.org/service1/" + now);
        SERVICE_2 = new URI("http://tempuri.org/service2/" + now);

    }

    @After
    public void tearDown() throws Exception {
    }

    public URI WORKFLOW_1, SERVICE_1, SERVICE_2, SERVICE_0;

    private URI DATA_URI_1, DATA_URI_2, DATA_URI_3, DATA_URI_4, DATA_URI_5, DATA_URI_6;
    private List<URI> DATA_URLS_1, DATA_URLS_2, DATA_URLS_3, DATA_URLS_4, DATA_URLS_5, DATA_URLS_6;

    static class Success {
        XmlObject header;
        XmlObject body;
    }

    static class Failure {
        XmlObject header;
        XmlObject body;
    }

    public Object runWorkflow1(InvocationEntity myInvoker, URI myWorkflowID, URI myServiceID, String myNodeID,
            Integer myTimestep) throws XmlException {

        assert WORKFLOW_1.equals(myServiceID);

        WorkflowNotifier notifier = NotifierFactory.createWorkflowNotifier();
        WorkflowTrackingContext context = notifier.createTrackingContext(new Properties(), epr, myWorkflowID,
                myServiceID, myNodeID, myTimestep);

        InvocationContext myInvocation = notifier.workflowInvoked(context, myInvoker,
                XmlObject.Factory.parse("<soapHeader/>"),
                XmlObject.Factory.parse("<soapBody>input1,input2</soapBody>"), "This is the start of this workflow");

        // BEGIN SERVICE1
        {
            // prepare to invoke service1
            InvocationEntity service1 = notifier.createEntity(context, myServiceID, SERVICE_1, "NODE1", 1);
            InvocationContext service1Invocation = notifier.invokingService(context, service1,
                    XmlObject.Factory.parse("<soapHeader/>"), XmlObject.Factory.parse("<soapBody>input1</soapBody>"),
                    "This workflow is invoking a service");

            Object result = null;
            try {
                // prepare to invoke service1
                result = runService1(service1, service1.getWorkflowID(), service1.getServiceID(),
                        service1.getWorkflowNodeID(), service1.getWorkflowTimestep());

                // If this were an async invocation, we would have finished
                // sending request.
                // we acknowledge the successful request.
                notifier.invokingServiceSucceeded(context, service1Invocation, "Invoked service1 successfully");

            } catch (Exception ex) {
                // If there had been a problem sending the request on the wire,
                // we acknowledge a failed request.
                notifier.invokingServiceFailed(context, service1Invocation, ex, "Failed to invoke service1");
            }

            // At this point, we would have waited for response from service1 if
            // it were an async call.
            // assume we received response at this point and continue.
            if (result instanceof Success) {
                notifier.receivedResult(context, service1Invocation, ((Success) result).header,
                        ((Success) result).body, "got success response from service1");
            } else if (result instanceof Failure) {
                notifier.receivedFault(context, service1Invocation, ((Failure) result).header, ((Failure) result).body,
                        "got fault response from service1");
            }

        }

        // BEGIN SERVICE2
        {
            // prepare to invoke service1
            InvocationEntity service2 = notifier.createEntity(context, myServiceID, SERVICE_2, "NODE2", 2);
            InvocationContext service1Invocation = notifier.invokingService(context, service2,
                    XmlObject.Factory.parse("<soapHeader/>"),
                    XmlObject.Factory.parse("<soapBody>input2,input3</soapBody>"),
                    "This workflow is invoking another service");

            Object result = null;
            try {
                // prepare to invoke service2
                result = runService2(service2, service2.getWorkflowID(), service2.getServiceID(),
                        service2.getWorkflowNodeID(), service2.getWorkflowTimestep());

                // If this were an async invocation, we would have finished
                // sending request.
                // we acknowledge the successful request.
                notifier.invokingServiceSucceeded(context, service1Invocation, "Invoked service2 successfully");

            } catch (Exception ex) {
                // If there had been a problem sending the request on the wire,
                // we acknowledge a failed request.
                notifier.invokingServiceFailed(context, service1Invocation, ex, "Failed to invoke service2");
            }

            // At this point, we would have waited for response from service1 if
            // it were an async call.
            // assume we received response at this point and continue.
            if (result instanceof Success) {
                notifier.receivedResult(context, service1Invocation, ((Success) result).header,
                        ((Success) result).body, "got success response from service2");
            } else if (result instanceof Failure) {
                notifier.receivedFault(context, service1Invocation, ((Failure) result).header, ((Failure) result).body,
                        "got fault response from service2");
            }

        }

        Object result = null;
        notifier.sendingResult(context, myInvocation, "sending result back to the invoker of this workflow");
        try {
            result = new Success();
            notifier.sendingResponseSucceeded(context, myInvocation, "sent result to invoker");
        } catch (Exception ex) {
            notifier.sendingResponseFailed(context, myInvocation, ex);
        }

        return result;
    }

    public Object runService1(InvocationEntity myInvoker, URI myWorkflowID, URI myServiceID, String myNodeID,
            int myTimestep) throws XmlException {

        // ensure the service ID as passed is what the service thinks it's
        // service ID is
        assert SERVICE_1.equals(myServiceID);

        // if we were not publishing data products, a serviceNotifier would have
        // sufficed
        ProvenanceNotifier notifier = NotifierFactory.createProvenanceNotifier();
        WorkflowTrackingContext context = notifier.createTrackingContext(null, epr, myWorkflowID, myServiceID,
                myNodeID, myTimestep);
        InvocationContext invocationContext = notifier.serviceInvoked(context, myInvoker,
                "I (service1) was invoked by my invoker",
                AnnotationProps.newProps(AnnotationConsts.AbstractServiceID, myServiceID.toString() + "-abstract")
                        .toString());

        notifier.dataConsumed(context, DATA_URI_1, DATA_URLS_1, "consuming a file");
        notifier.dataConsumed(context, DATA_URI_2, DATA_URLS_2, "consuming another file");

        // do stuff!

        notifier.dataProduced(context, DATA_URI_3, DATA_URLS_3, "produced some file", "<someXml/>");

        boolean successResult = true;

        // produce response...either success or failure
        Object result = null;
        if (successResult) {
            Success success = new Success();
            success.header = XmlObject.Factory
                    .parse("<S:Header "
                            + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                            + "<wsa:To>http://129.79.246.253:3456</wsa:To><wsa:RelatesTo>uuid:ee4d14d0-2262-11db-86d8-cd518af91949</wsa:RelatesTo>"
                            + "</S:Header>");
            success.body = XmlObject.Factory
                    .parse("<S:Body "
                            + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                            + "<gfac:Run_OutputParams xmlns:gfac=\"http://www.extreme.indiana.edu/namespaces/2004/01/gFac\">"
                            + "<gfac:WRF_Ininitialization_Files><value>gsiftp://grid-hg.ncsa.teragrid.org//scratch/hperera/Wed_Aug_02_15_10_23_EST_2006_ARPS2WRF/outputData/namelist.input</value><value>gsiftp://grid-hg.ncsa.teragrid.org//scratch/hperera/Wed_Aug_02_15_10_23_EST_2006_ARPS2WRF/outputData/wrfbdy_d01</value><value>gsiftp://grid-hg.ncsa.teragrid.org//scratch/hperera/Wed_Aug_02_15_10_23_EST_2006_ARPS2WRF/outputData/wrfinput_d01</value></gfac:WRF_Ininitialization_Files>"
                            + "</gfac:Run_OutputParams>" + "</S:Body>");

            // notify that invocation produced a result and send result to
            // invoker
            notifier.sendingResult(context, invocationContext, success.header, success.body, "sending success");
            try {
                // since this is a sync call, we mimic an async response by
                // seting the result object
                result = success;
                // acknowledge that the result was sent successfully
                notifier.sendingResponseSucceeded(context, invocationContext);
            } catch (Exception ex) {
                // acknowledge that sending the result failed
                notifier.sendingResponseFailed(context, invocationContext, ex, "error sending response");
            }

        } else {

            Failure failure = new Failure();
            failure.header = XmlObject.Factory.parse("<faultHeader/>");
            failure.body = XmlObject.Factory.parse("<faultBody>fault1</faultBody>");

            // notify that invocation produced a fault and send fault to invoker
            notifier.sendingFault(context, invocationContext, failure.header, failure.body, "sending fault");
            try {
                // since this is a sync call, we mimic an async response by
                // seting the result object
                result = failure;
                // acknowledge that the fault was sent successfully
                notifier.sendingResponseSucceeded(context, invocationContext);
            } catch (Exception ex) {
                // acknowledge that sending the fault failed
                notifier.sendingResponseFailed(context, invocationContext, ex, "error sending response");
            }
        }

        // send result
        return result;
    }

    public Object runService2(InvocationEntity myInvoker, URI myWorkflowID, URI myServiceID, String myNodeID,
            int myTimestep) throws XmlException {

        // ensure the service ID as passed is what the service thinks it's
        // service ID is
        assert SERVICE_2.equals(myServiceID);

        // if we were not publishing data products, a serviceNotifier would have
        // sufficed
        ProvenanceNotifier notifier = NotifierFactory.createProvenanceNotifier();

        // received request
        WorkflowTrackingContext context = notifier.createTrackingContext(null, epr, myWorkflowID, myServiceID,
                myNodeID, myTimestep);
        InvocationContext invocationContext = notifier.serviceInvoked(context, myInvoker,
                "I (service2) was invoked by my invoker");

        notifier.dataConsumed(context, DATA_URI_2, DATA_URLS_2, "consuming file used by service1");
        notifier.dataConsumed(context, DATA_URI_3, DATA_URLS_3);
        notifier.dataConsumed(context, DATA_URI_4, DATA_URLS_4, null, "<dataXml>boo</dataXml>");

        // do stuff!

        notifier.dataProduced(context, DATA_URI_5, DATA_URLS_5);
        notifier.dataProduced(context, DATA_URI_6, DATA_URLS_6);

        CalderNotifier calderNotifier = NotifierFactory.createCalderNotifier();
        calderNotifier.queryStarted(context, "KIND");
        calderNotifier.queryFailedToStart(context, "KIND");
        calderNotifier.queryExpired(context, "KIND");
        calderNotifier.queryActive(context, "KIND");
        calderNotifier.triggerFound(context, "KIND");

        boolean successResult = true;

        // produce response...either success or failure
        Object result = null;
        if (successResult) {
            Success success = new Success();
            success.header = XmlObject.Factory.parse("<resultHeader/>");
            success.body = XmlObject.Factory.parse("<resultBody>output2,output3</resultBody>");

            // notify that invocation produced a result and send result to
            // invoker
            notifier.sendingResult(context, invocationContext, success.header, success.body, "sending success");
            try {
                // since this is a sync call, we mimic an async response by
                // seting the result object
                result = success;
                // acknowledge that the result was sent successfully
                notifier.sendingResponseSucceeded(context, invocationContext);
            } catch (Exception ex) {
                // acknowledge that sending the result failed
                notifier.sendingResponseFailed(context, invocationContext, ex, "error sending response");
            }

        } else {

            Failure failure = new Failure();
            failure.header = XmlObject.Factory.parse("<faultHeader/>");
            failure.body = XmlObject.Factory.parse("<faultBody>fault2</faultBody>");

            // notify that invocation produced a fault and send fault to invoker
            notifier.sendingFault(context, invocationContext, failure.header, failure.body, "sending fault");
            try {
                // since this is a sync call, we mimic an async response by
                // seting the result object
                result = failure;
                // acknowledge that the fault was sent successfully
                notifier.sendingResponseSucceeded(context, invocationContext);
            } catch (Exception ex) {
                // acknowledge that sending the fault failed
                notifier.sendingResponseFailed(context, invocationContext, ex, "error sending response");
            }
        }

        // send result
        return result;
    }

    // used to override notifier creation to use an external notifier, instead
    // of setting the
    // properties to create it in the main method
    private Notifier notifier;

    public void runSample() throws Exception {
        notifier = NotifierFactory.createNotifier();
        WorkflowTrackingContext context = notifier.createTrackingContext(null, epr, WORKFLOW_1, SERVICE_0, null, null);
        // create workflow and service instances
        {

            WorkflowNotifier notifier = NotifierFactory.createWorkflowNotifier();
            notifier.workflowInitialized(context, WORKFLOW_1, "Workflow ready to start",
                    "<wfInfo>some annotation about the workflow</wfInfo>",
                    "<dummy>just to check annotations list</dummy>");
            notifier.serviceInitialized(context, SERVICE_1);
            notifier.serviceInitialized(context, SERVICE_2);
        }

        {
            GenericNotifier notifier = NotifierFactory.createGenericNotifier();
            InvocationEntity initiatingService = notifier.createEntity(context, null, SERVICE_0, null, null);

            runWorkflow1(initiatingService, null, WORKFLOW_1, null, null);
        }

        // terminate workflow and service instances
        {
            WorkflowNotifier notifier = NotifierFactory.createWorkflowNotifier();
            notifier.workflowTerminated(context, WORKFLOW_1);
            notifier.serviceTerminated(context, SERVICE_1);
            notifier.serviceTerminated(context, SERVICE_2);
        }
    }

    @Test
    public void testSimpleTest() throws Exception {

        System.out.println("USAGE: run org.apache.airavata.workflow.tracking.samples.workflow.SimpleWorkflowExecution "
                + " [WSMessaging broker URL (default: print to stdout)]");

        ConsumerNotificationHandler handler = new ConsumerNotificationHandler() {

            public void handleNotification(SOAPEnvelope msgEnvelope) {
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    XMLPrettyPrinter.prettify(msgEnvelope, out);
                    System.out.println(new String(out.toByteArray()));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };

        WseMsgBrokerClient api = new WseMsgBrokerClient();
        api.init(BROKER_URL);
        int consumerPort = 5555;

        String[] consumerServiceEprs = api.startConsumerService(consumerPort, handler);

        api.subscribe(consumerServiceEprs[0], ">", null);

        String topic = "Foo";
        // if remote broker location specified, use WSMessaging publisher;
        ConstructorProps props = ConstructorProps
                .newProps(ConstructorConsts.ENABLE_ASYNC_PUBLISH, "false")
                .set(ConstructorConsts.ENABLE_BATCH_PROVENANCE, "true")
                .set(ConstructorConsts.ANNOTATIONS,
                        AnnotationProps.newProps(AnnotationConsts.ExperimentID,
                                "experiment-id-" + System.currentTimeMillis()).set(AnnotationConsts.UserDN,
                                "/O=IU/OU=Extreme Lab/CN=drlead"));
        if (BROKER_URL != null) {
            EndpointReference brokerEpr = api.createEndpointReference(BROKER_URL, topic);

            props.set(ConstructorConsts.BROKER_EPR, brokerEpr.getAddress());
        } else {
            props.set(ConstructorConsts.PUBLISHER_IMPL_CLASS,
                    "org.apache.airavata.workflow.tracking.impl.publish.LoopbackPublisher");
            props.set(ConstructorConsts.TOPIC, topic);
        }

        System.out.println(ConstructorConsts.ANNOTATIONS);
        System.out.println(ConstructorConsts.KARMA_IMPL);
        runSample();
        new Semaphore(0).acquire();
    }

}
