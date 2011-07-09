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

package org.apache.airavata.wsmg.msgbox.tests;

import java.io.StringReader;
import java.util.Iterator;
import java.util.UUID;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.airavata.wsmg.msgbox.client.MsgBoxClient;
import org.apache.airavata.wsmg.msgbox.util.MsgBoxUtils;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MessageBoxStressTest extends TestCase {
    private int port = 5555;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        InMemoryMessageBoxServer.start(null, null);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testMessageBox() throws Exception {

        String msgBoxId = UUID.randomUUID().toString();
        MsgBoxClient user = new MsgBoxClient();

        // test publish with Epr
        EndpointReference msgBoxEpr = user.createMessageBox("http://localhost:" + port
                + "/axis2/services/MsgBoxService", 500L);

        System.out.println(msgBoxEpr.toString());
        user.storeMessage(msgBoxEpr, 500L,
                MsgBoxUtils.reader2OMElement(new StringReader("<test>A simple test message</test>")));

        Iterator<OMElement> iterator = user.takeMessagesFromMsgBox(msgBoxEpr, 500L);
        int i = 0;
        if (iterator != null)
            while (iterator.hasNext()) {
                i++;
                System.out.println("Retrieved message :" + i);
                System.out.println(iterator.next().toStringWithConsume());
            }

        System.out.println("Delete message box response :  " + user.deleteMsgBox(msgBoxEpr, 500L));

        // test invocations with id encoded in the Url
        msgBoxId = UUID.randomUUID().toString();
        user = new MsgBoxClient();

        // test publish with Epr
        msgBoxEpr = user.createMessageBox("http://localhost:" + port + "/axis2/services/MsgBoxService", 500L);

        String mesgboxUrl = "http://localhost:" + port + "/axis2/services/MsgBoxService/clientid/" + msgBoxId;

        ServiceClient client = new ServiceClient();
        client.getOptions().setTo(new EndpointReference(mesgboxUrl));

        OMElement request = OMAbstractFactory.getOMFactory().createOMElement(new QName("foo"));
        request.setText("bar");
        client.sendReceive(request);

        iterator = user.takeMessagesFromMsgBox(new EndpointReference(mesgboxUrl), 500L);
        assertTrue(iterator.hasNext());
        iterator.next();
        assertFalse(iterator.hasNext());

        System.out.println("All tests Done");

    }

}
