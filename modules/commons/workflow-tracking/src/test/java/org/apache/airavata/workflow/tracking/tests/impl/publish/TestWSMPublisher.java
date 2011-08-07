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

package org.apache.airavata.workflow.tracking.tests.impl.publish;

import java.io.IOException;

import org.apache.airavata.workflow.tracking.impl.publish.WSMPublisher;
import org.apache.axis2.addressing.EndpointReference;
import org.junit.*;

public class TestWSMPublisher {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @org.junit.Test
    public final void testWSMPublisherConstructor1() {

        EndpointReference brokerEpr = new EndpointReference("http://invalid/broker/address");
        WSMPublisher publisher = new WSMPublisher(10, false, brokerEpr);

    }

    @org.junit.Test
    public final void testWSMPublisherConstructor2() {

        try {
            WSMPublisher publisher = new WSMPublisher(10, false, "http://invalid/broker/address", "TestTopic1");

        } catch (IOException e) {
            // fail("Test failed");
        }

    }

    @org.junit.Test
    public final void testWSMPublisherConstructor3() {
        try {

            EndpointReference epr = new EndpointReference("http://invalid/broker/address");

            WSMPublisher publisher = new WSMPublisher(10, false, epr.getAddress());

        } catch (Exception e) {
            e.printStackTrace();
            // fail();
        }
    }

    @org.junit.Test
    public final void testWSMPublisherConstructor4() {
        try {

            EndpointReference epr = new EndpointReference("http://invalid/broker/address");

            // According to addressing format.
            String eprFormat = "<BrokerEPR><wsa:Address xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">%s</wsa:Address></BrokerEPR>";

            String str = String.format(eprFormat, "http://invalid/broker/address");

            WSMPublisher publisher = new WSMPublisher(10, false, str, true);

        } catch (Exception e) {
            e.printStackTrace();
            // fail();
        }
    }

    @org.junit.Test
    public final void testWSMPublisherConstructor5() {
        // try {
        //
        // EndpointReference epr = new EndpointReference(
        // "http://invalid/broker/address");
        //
        // AnnotationProps annotationProps = AnnotationProps.newProps(
        // AnnotationConsts.ExperimentID, "TestexperId1");
        // annotationProps.set(AnnotationConsts.ServiceLocation,
        // "testServiceAddress");
        //
        // ConstructorProps props = ConstructorProps.newProps();
        // props.set(ConstructorConsts.BROKER_EPR, epr.getAddress());
        // props.set(ConstructorConsts.ENABLE_ASYNC_PUBLISH, "false");
        // props.set(ConstructorConsts.ENABLE_BATCH_PROVENANCE, "false");
        // props.set(ConstructorConsts.ANNOTATIONS, annotationProps);
        //
        // Notifier notifier = NotifierFactory.createGenericNotifier();
        //
        // } catch (Exception e) {
        // e.printStackTrace();
        // fail();
        // }
    }

}
