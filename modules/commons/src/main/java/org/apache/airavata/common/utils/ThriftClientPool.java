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
package org.apache.airavata.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.airavata.base.api.BaseAPI;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThriftClientPool<T extends BaseAPI.Client> implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ThriftClientPool.class);

    private final GenericObjectPool<T> internalPool;

    /**
     * StringWriter that flushes to SLF4J logger.
     */
    private static class ErrorLoggingStringWriter extends StringWriter {

        @Override
        public void flush() {
            logger.error(this.toString());
            // Reset buffer
            this.getBuffer().setLength(0);
        }
    }

    public ThriftClientPool(ClientFactory<T> clientFactory, GenericObjectPoolConfig<T> poolConfig, String host,
            int port) {
        this(clientFactory, new BinaryOverSocketProtocolFactory(host, port), poolConfig);
    }

    public ThriftClientPool(ClientFactory<T> clientFactory, ProtocolFactory protocolFactory,
            GenericObjectPoolConfig<T> poolConfig) {

        AbandonedConfig abandonedConfig = null;
        if (ApplicationSettings.isThriftClientPoolAbandonedRemovalEnabled()) {
            abandonedConfig = new AbandonedConfig();
            abandonedConfig.setRemoveAbandonedOnBorrow(true);
            abandonedConfig.setRemoveAbandonedOnMaintenance(true);
            if (ApplicationSettings.isThriftClientPoolAbandonedRemovalLogged()) {
                abandonedConfig.setLogAbandoned(true);
                abandonedConfig.setLogWriter(new PrintWriter(new ErrorLoggingStringWriter()));
            } else {
                abandonedConfig.setLogAbandoned(false);
            }
        }
        this.internalPool = new GenericObjectPool<T>(new ThriftClientFactory(clientFactory, protocolFactory),
                poolConfig, abandonedConfig);
    }

    public ThriftClientPool(ClientFactory<T> clientFactory, ProtocolFactory protocolFactory,
            GenericObjectPoolConfig<T> poolConfig, AbandonedConfig abandonedConfig) {

        if (abandonedConfig != null && abandonedConfig.getRemoveAbandonedOnMaintenance()
                && poolConfig.getTimeBetweenEvictionRunsMillis() <= 0) {
            logger.warn("Abandoned removal is enabled but"
                    + " removeAbandonedOnMaintenance won't run since"
                    + " timeBetweenEvictionRunsMillis is not positive, current value: {}",
                    poolConfig.getTimeBetweenEvictionRunsMillis());
        }
        this.internalPool = new GenericObjectPool<T>(new ThriftClientFactory(clientFactory, protocolFactory),
                poolConfig, abandonedConfig);
    }

    class ThriftClientFactory extends BasePooledObjectFactory<T> {

        private ClientFactory<T> clientFactory;
        private ProtocolFactory protocolFactory;

        public ThriftClientFactory(ClientFactory<T> clientFactory, ProtocolFactory protocolFactory) {
            this.clientFactory = clientFactory;
            this.protocolFactory = protocolFactory;
        }

        @Override
        public T create() throws Exception {
            try {
                TProtocol protocol = protocolFactory.make();
                return clientFactory.make(protocol);
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
                throw new ThriftClientException("Can not make a new object for pool", e);
            }
        }

        @Override
        public void destroyObject(PooledObject<T> pooledObject) throws Exception {
            T obj = pooledObject.getObject();
            if (obj.getOutputProtocol().getTransport().isOpen()) {
                obj.getOutputProtocol().getTransport().close();
            }
            if (obj.getInputProtocol().getTransport().isOpen()) {
                obj.getInputProtocol().getTransport().close();
            }
        }

        @Override
        public PooledObject<T> wrap(T obj) {
            return new DefaultPooledObject<T>(obj);
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
                logger.warn(e.getMessage(), e);
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
            for( int i = 0; i < 10 ; i++) {
                // This tries to fetch a client from the pool and validate it before returning.
                final T client = internalPool.borrowObject();
                try {
                    String apiVersion = client.getAPIVersion();
                    logger.debug("Validated client and fetched api version " + apiVersion);
                    return client;
                } catch (Exception e) {
                    logger.warn("Failed to validate the client. Retrying " + i, e);
                    returnBrokenResource(client);
                }
            }
            throw new Exception("Failed to fetch a client form the pool after validation");
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
