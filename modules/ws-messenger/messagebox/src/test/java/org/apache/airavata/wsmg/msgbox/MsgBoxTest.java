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

package org.apache.airavata.wsmg.msgbox;

import java.io.StringReader;
import java.util.Iterator;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.airavata.wsmg.msgbox.client.MsgBoxClient;
import org.apache.airavata.wsmg.msgbox.util.MsgBoxUtils;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MsgBoxTest extends TestCase {

    private int port = InMemoryMessageBoxServer.TESTING_PORT;
    private long timeout = 5000L;

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

        MsgBoxClient user = new MsgBoxClient();
        StringBuilder builder = new StringBuilder();

        EndpointReference msgBoxEpr = user.createMessageBox("http://localhost:" + port
                + "/axis2/services/MsgBoxService", timeout);
        
        for (int i = 0; i < 10; i++) {
                                
            builder.delete(0, builder.capacity());
            Random x = new Random();
            for (int j = 0; j < x.nextInt(50) ; j++) {
                builder.append("123456789");
            }
            
            String msg = String.format("<msg><seq>%d</seq><fill>%s</fill></msg>", i, builder.toString());

            user.storeMessage(msgBoxEpr, timeout, MsgBoxUtils.reader2OMElement(new StringReader(msg)));

            Thread.sleep(200L);
        }

        Iterator<OMElement> iterator = null;

        try {
            iterator = user.takeMessagesFromMsgBox(msgBoxEpr, timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (iterator != null)
            while (iterator.hasNext()) {
                System.out.println(iterator.next().toStringWithConsume());
            }

        System.out.println("Delete message box response :  " + user.deleteMsgBox(msgBoxEpr, 5000L));
    }

}
