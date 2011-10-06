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

package org.apache.airavata.xbaya.test.jython;

import junit.framework.TestCase;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.invoker.GenericInvoker;
import org.apache.airavata.xbaya.invoker.Invoker;
import org.apache.airavata.xbaya.jython.lib.NotificationSender;
import org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable;
import org.apache.airavata.xbaya.test.service.adder.AdderService;
import org.apache.airavata.xbaya.test.service.multiplier.MultiplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JythonLibraryTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(JythonLibraryTest.class);

    /**
     * @throws XBayaException
     */
    @SuppressWarnings("boxing")
    public void testSimpleMath() throws XBayaException {

        AdderService service = new AdderService();
        service.run();
        String adderWSDLLoc = service.getServiceWsdlLocation();

        WorkflowNotifiable notifier = new NotificationSender(XBayaConstants.DEFAULT_BROKER_URL.toString(), "test-topic");

        Invoker invoker = new GenericInvoker(null, adderWSDLLoc, "adder", null, null, notifier);
        invoker.setup();
        invoker.setOperation("add");
        invoker.setInput("x", 2);
        invoker.setInput("y", 3);
        invoker.invoke();

        Object output = invoker.getOutput("z");
        logger.info("z = " + output);

        service.shutdownServer();
    }

    /**
     * @throws XBayaException
     */
    @SuppressWarnings("boxing")
    public void testComplexMath() throws XBayaException {

        AdderService adder = new AdderService();
        adder.run();
        String adderWSDLLoc = adder.getServiceWsdlLocation();

        MultiplierService multiplier = new MultiplierService();
        multiplier.run();
        String multiplierWSDLLoc = multiplier.getServiceWsdlLocation();

        WorkflowNotifiable notifier = new NotificationSender(XBayaConstants.DEFAULT_BROKER_URL.toString(), "test-topic");

        Invoker adderInvoker1 = new GenericInvoker(null, adderWSDLLoc, "adder", null, null, notifier);
        adderInvoker1.setup();
        adderInvoker1.setOperation("add");
        adderInvoker1.setInput("x", 2);
        adderInvoker1.setInput("y", 3);
        adderInvoker1.invoke();

        Object output1 = adderInvoker1.getOutput("z");
        logger.info("output1 = " + output1);

        Invoker adderInvoker2 = new GenericInvoker(null, adderWSDLLoc, "adder", null, null, notifier);
        adderInvoker2.setup();
        adderInvoker2.setOperation("add");
        adderInvoker2.setInput("x", 4);
        adderInvoker2.setInput("y", 5);
        adderInvoker2.invoke();

        Object output2 = adderInvoker2.getOutput("z");
        logger.info("output2 = " + output2);

        Invoker multiplierInvoker = new GenericInvoker(null, multiplierWSDLLoc, "multiplier", null, null,
                notifier);
        multiplierInvoker.setup();
        multiplierInvoker.setOperation("multiply");
        multiplierInvoker.setInput("x", output1);
        multiplierInvoker.setInput("y", output2);
        multiplierInvoker.invoke();

        Object output3 = multiplierInvoker.getOutput("z");
        logger.info("output3 = " + output3);

        adder.shutdownServer();
        multiplier.shutdownServer();
    }
}