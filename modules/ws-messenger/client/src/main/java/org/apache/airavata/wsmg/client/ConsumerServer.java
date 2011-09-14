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

package org.apache.airavata.wsmg.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.transport.http.SimpleHTTPServer;

public class ConsumerServer {

    private static final String ADDRESSING_VALIDATE_ACTION = "addressing.validateAction";
    public static final String SUPPORT_SINGLE_OP = "supportSingleOperation";
    private ConsumerNotificationHandler handler;
    private int listeningPort;
    private SimpleHTTPServer server;
    private AxisService consumerService = null;

    public ConsumerServer(int listenPort, ConsumerNotificationHandler h) {

        if (listenPort <= 0 || h == null) {
            throw new IllegalArgumentException("invalid arguments supplied");
        }

        this.listeningPort = listenPort;
        this.handler = h;
        this.server = null;
        this.consumerService = null;
        // setConsumerServiceUrl(listenPort);

    }

    public String[] getConsumerServiceEPRs() {
        return consumerService.getEPRs();
    }

    /**
     * @param args
     * @throws AxisFault
     * @throws Exception
     */
    public void start() throws AxisFault {
        ConfigurationContext context = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);

        context.setProperty(ADDRESSING_VALIDATE_ACTION, Boolean.valueOf(false));

        context.getAxisConfiguration().addParameter(SUPPORT_SINGLE_OP, Boolean.valueOf(true));

        context.getAxisConfiguration().addParameter(ADDRESSING_VALIDATE_ACTION, Boolean.valueOf(false));

        Map<String, MessageReceiver> msgRecieverMap = new HashMap<String, MessageReceiver>();
        ConsumerMsgReceiver conMsgRcv = new ConsumerMsgReceiver(handler);

        msgRecieverMap.put("http://www.w3.org/ns/wsdl/in-only", conMsgRcv);
        AxisService service = AxisService.createService(ConsumerService.class.getName(),
                context.getAxisConfiguration(), msgRecieverMap, null, null, ConsumerServer.class.getClassLoader());

        context.getAxisConfiguration().addService(service);
        server = new SimpleHTTPServer(context, this.listeningPort);
        server.start();

        consumerService = service;

    }

    public void stop() {
        // TODO: add an exeption
        server.stop();
    }

    public int getListenPort() {
        return listeningPort;
    }

}
