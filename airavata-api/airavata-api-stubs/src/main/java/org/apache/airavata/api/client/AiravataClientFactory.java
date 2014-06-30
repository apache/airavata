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

package org.apache.airavata.api.client;

import java.lang.reflect.InvocationTargetException;

import org.apache.airavata.api.Airavata;
//import org.apache.airavata.api.appcatalog.ApplicationCatalogAPI;
import org.apache.airavata.model.error.AiravataClientConnectException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataClientFactory {

    private final static Logger logger = LoggerFactory.getLogger(AiravataClientFactory.class);

    public static Airavata.Client createAiravataClient(String serverHost, int serverPort) throws AiravataClientConnectException{
        try {
            TTransport transport = new TSocket(serverHost, serverPort);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
//            TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, "APIServer");
            return new Airavata.Client(protocol);
        } catch (TTransportException e) {
            throw new AiravataClientConnectException("Unable to connect to the server at "+serverHost+":"+serverPort);
        }
    }
    
    /*public static ApplicationCatalogAPI.Client createApplicationCatalogClient(String serverHost, int serverPort) throws AiravataClientConnectException{
        try {
            TTransport transport = new TSocket(serverHost, serverPort);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
//            TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, "AppCatalog");
            return new ApplicationCatalogAPI.Client(protocol);
        } catch (TTransportException e) {
            throw new AiravataClientConnectException("Unable to connect to the server at "+serverHost+":"+serverPort);
        }
    }
    
    public static <T extends org.apache.thrift.TServiceClient> T createApplicationCatalogClient(String serverHost, int serverPort, Class<T> type) throws Exception{
        try {
            TTransport transport = new TSocket(serverHost, serverPort);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, "AppCatalog");
            return  type.getConstructor(TProtocol.class).newInstance(mp);
        } catch (TTransportException e) {
            throw new AiravataClientConnectException("Unable to connect to the server at "+serverHost+":"+serverPort);
        } catch (Exception e) {
			throw new Exception("Invalid Airavata API Service "+type.getClass().getCanonicalName());
		}
    }*/
}