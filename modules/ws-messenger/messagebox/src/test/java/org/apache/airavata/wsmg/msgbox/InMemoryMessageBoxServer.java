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

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.transport.http.SimpleHTTPServer;

public class InMemoryMessageBoxServer {
    private static int count = 0;

    private static SimpleHTTPServer receiver;

    public static final int TESTING_PORT = 5555;

    public static final String FAILURE_MESSAGE = "Intentional Failure";

    public static synchronized void deployService(AxisService service) throws AxisFault {
        receiver.getConfigurationContext().getAxisConfiguration().addService(service);
    }

    public static synchronized void unDeployService(QName service) throws AxisFault {
        receiver.getConfigurationContext().getAxisConfiguration().removeService(service.getLocalPart());
    }

    public static synchronized void start(String repository, String axis2xml) throws Exception {
        if (count == 0) {
            ConfigurationContext er = getNewConfigurationContext(repository, axis2xml);

            receiver = new SimpleHTTPServer(er, TESTING_PORT);

            try {
                receiver.start();
                System.out.print("Server started on port " + TESTING_PORT + ".....");
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                throw new AxisFault("Thread interuptted", e1);
            }
            startMessageBox();
        }
        count++;
    }

    public static void startMessageBox() throws Exception {

        /*
         * Imitate service.xml
         */
        AxisService axisService = new AxisService("MsgBoxService");
        axisService.setServiceLifeCycle(new MsgBoxServiceLifeCycle());
        axisService.addParameter("ServiceClass", "org.apache.airavata.wsmg.msgbox.MsgBoxServiceSkeleton");
        createOperation(axisService, "storeMessages", new MsgBoxServiceMessageReceiverInOut(),
                "http://org.apache.airavata/ws-messenger/msgbox/2011/storeMessages",
                "http://org.apache.airavata/ws-messenger/msgbox/2011/MsgBoxPT/storeMessagesResponse");
        createOperation(axisService, "destroyMsgBox", new MsgBoxServiceMessageReceiverInOut(),
                "http://org.apache.airavata/ws-messenger/msgbox/2011/destroyMsgBox",
                "http://org.apache.airavata/ws-messenger/msgbox/2011/MsgBoxPT/destroyMsgBoxResponse");
        createOperation(axisService, "takeMessages", new MsgBoxServiceMessageReceiverInOut(),
                "http://org.apache.airavata/ws-messenger/msgbox/2011/takeMessages",
                "http://org.apache.airavata/ws-messenger/msgbox/2011/MsgBoxPT/takeMessagesResponse");
        createOperation(axisService, "createMsgBox", new MsgBoxServiceMessageReceiverInOut(),
                "http://org.apache.airavata/ws-messenger/msgbox/2011/createMsgBox",
                "http://org.apache.airavata/ws-messenger/msgbox/2011/MsgBoxPT/createMsgBoxResponse");

        InMemoryMessageBoxServer.deployService(axisService);

        new MsgBoxServiceLifeCycle().startUp(InMemoryMessageBoxServer.getConfigurationContext(), axisService);

    }

    public static void createOperation(AxisService axisService, String name, MessageReceiver messageReceiver,
            String inputAction, String outputAction) {
        InOutAxisOperation operation1 = new InOutAxisOperation(new QName(name));
        operation1.setMessageReceiver(messageReceiver);
        operation1.setOutputAction(outputAction);
        axisService.addOperation(operation1);
        if (inputAction != null) {
            axisService.mapActionToOperation(inputAction, operation1);
        }
    }

    public static ConfigurationContext getNewConfigurationContext(String repository, String axis2xml) throws Exception {
        return ConfigurationContextFactory.createConfigurationContextFromFileSystem(repository, axis2xml);
    }

    public static synchronized void stop() throws AxisFault {
        if (count == 1) {
            receiver.stop();
            while (receiver.isRunning()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    // nothing to do here
                }
            }
            count = 0;
            // tp.doStop();
            System.out.print("Server stopped .....");
        } else {
            count--;
        }
        receiver.getConfigurationContext().terminate();
    }

    public static ConfigurationContext getConfigurationContext() {
        return receiver.getConfigurationContext();
    }

    public static String prefixBaseDirectory(String path) {
        return path;
    }

}
