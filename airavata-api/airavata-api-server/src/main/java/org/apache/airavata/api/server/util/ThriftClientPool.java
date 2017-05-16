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
package org.apache.airavata.api.server.util;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThriftClientPool<T extends TServiceClient> implements
        AutoCloseable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ThriftClientPool.class);

    private final GenericObjectPool internalPool;

    public ThriftClientPool(ClientFactory<T> clientFactory,
                            GenericObjectPool.Config poolConfig, String host, int port) {
        this(clientFactory, new BinaryOverSocketProtocolFactory(host, port),
                poolConfig);
    }

    public ThriftClientPool(ClientFactory<T> clientFactory,
                            ProtocolFactory protocolFactory, GenericObjectPool.Config poolConfig) {
        this.internalPool = new GenericObjectPool(new ThriftClientFactory(
                clientFactory, protocolFactory), poolConfig);
    }

    class ThriftClientFactory extends BasePoolableObjectFactory {

        private ClientFactory<T> clientFactory;
        private ProtocolFactory protocolFactory;

        public ThriftClientFactory(ClientFactory<T> clientFactory,
                                   ProtocolFactory protocolFactory) {
            this.clientFactory = clientFactory;
            this.protocolFactory = protocolFactory;
        }

        @Override
        public T makeObject() throws Exception {
            try {
                TProtocol protocol = protocolFactory.make();
                return clientFactory.make(protocol);
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
                throw new ThriftClientException(
                        "Can not make a new object for pool", e);
            }
        }

        public void destroyObject(T obj) throws Exception {
            if (obj.getOutputProtocol().getTransport().isOpen()) {
                obj.getOutputProtocol().getTransport().close();
            }
            if (obj.getInputProtocol().getTransport().isOpen()) {
                obj.getInputProtocol().getTransport().close();
            }
        }
    }

    public static interface ClientFactory<T> {

        T make(TProtocol tProtocol);
    }

    public static interface ProtocolFactory {

        TProtocol make();
    }

    public static class BinaryOverSocketProtocolFactory implements
            ProtocolFactory {

        private String host;
        private int port;

        public BinaryOverSocketProtocolFactory(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public TProtocol make() {
            TTransport transport = new TSocket(host, port);
            try {
                transport.open();
            } catch (TTransportException e) {
                LOGGER.warn(e.getMessage(), e);
                throw new ThriftClientException("Can not make protocol", e);
            }
            return new TBinaryProtocol(transport);
        }
    }

    public static class ThriftClientException extends RuntimeException {

        // Fucking Eclipse
        private static final long serialVersionUID = -2275296727467192665L;

        public ThriftClientException(String message, Exception e) {
            super(message, e);
        }

    }

    public T getResource() {
        try {
            return (T) internalPool.borrowObject();
        } catch (Exception e) {
            throw new ThriftClientException(
                    "Could not get a resource from the pool", e);
        }
    }

    public void returnResourceObject(T resource) {
        try {
            internalPool.returnObject(resource);
        } catch (Exception e) {
            throw new ThriftClientException(
                    "Could not return the resource to the pool", e);
        }
    }

    public void returnBrokenResource(T resource) {
        returnBrokenResourceObject(resource);
    }

    public void returnResource(T resource) {
        returnResourceObject(resource);
    }

    protected void returnBrokenResourceObject(T resource) {
        try {
            internalPool.invalidateObject(resource);
        } catch (Exception e) {
            throw new ThriftClientException(
                    "Could not return the resource to the pool", e);
        }
    }

    public void destroy() {
        close();
    }

    public void close() {
        try {
            internalPool.close();
        } catch (Exception e) {
            throw new ThriftClientException("Could not destroy the pool", e);
        }
    }
}