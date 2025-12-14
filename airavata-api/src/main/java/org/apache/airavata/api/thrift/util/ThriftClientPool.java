/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.api.thrift.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.airavata.api.model.BaseAPI;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThriftClientPool<T extends BaseAPI.Client> implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ThriftClientPool.class);

    private final BlockingQueue<T> pool;
    private final ClientFactory<T> clientFactory;
    private final ProtocolFactory protocolFactory;
    private final int maxTotal;
    private final int maxIdle;
    private final long maxWaitMillis;
    private final long timeBetweenEvictionRunsMillis;
    private final AtomicInteger createdCount = new AtomicInteger(0);
    private final AiravataServerProperties properties;

    public ThriftClientPool(ClientFactory<T> clientFactory, PoolConfig poolConfig, String host, int port) {
        this(clientFactory, new BinaryOverSocketProtocolFactory(host, port), poolConfig, null);
    }

    public ThriftClientPool(ClientFactory<T> clientFactory, ProtocolFactory protocolFactory, PoolConfig poolConfig) {
        this(clientFactory, protocolFactory, poolConfig, null);
    }

    public ThriftClientPool(
            ClientFactory<T> clientFactory,
            ProtocolFactory protocolFactory,
            PoolConfig poolConfig,
            AiravataServerProperties properties) {
        this.clientFactory = clientFactory;
        this.protocolFactory = protocolFactory;
        this.properties = properties;
        this.maxTotal = poolConfig.getMaxTotal();
        this.maxIdle = poolConfig.getMaxIdle() > 0 ? poolConfig.getMaxIdle() : poolConfig.getMaxTotal();
        this.maxWaitMillis = poolConfig.getMaxWaitMillis();
        this.timeBetweenEvictionRunsMillis = poolConfig.getTimeBetweenEvictionRunsMillis();
        this.pool = new LinkedBlockingQueue<>(maxIdle);
    }

    private T createClient() throws Exception {
        try {
            TProtocol protocol = protocolFactory.make();
            T client = clientFactory.make(protocol);
            createdCount.incrementAndGet();
            return client;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new ThriftClientException("Can not make a new object for pool", e);
        }
    }

    private void destroyClient(T client) {
        try {
            if (client.getOutputProtocol().getTransport().isOpen()) {
                client.getOutputProtocol().getTransport().close();
            }
            if (client.getInputProtocol().getTransport().isOpen()) {
                client.getInputProtocol().getTransport().close();
            }
        } catch (Exception e) {
            logger.warn("Error destroying client", e);
        }
    }

    public static interface ClientFactory<T> {

        T make(TProtocol tProtocol);
    }

    public static interface ProtocolFactory {

        TProtocol make();
    }

    public static class BinaryOverSocketProtocolFactory implements ProtocolFactory {

        private String host;
        private int port;

        public BinaryOverSocketProtocolFactory(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public TProtocol make() {
            TTransport transport;
            try {
                transport = new TSocket(host, port);
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
            for (int i = 0; i < 10; i++) {
                // This tries to fetch a client from the pool and validate it before returning.
                T client = null;
                try {
                    // Try to get from pool first
                    client = pool.poll(maxWaitMillis, TimeUnit.MILLISECONDS);
                    if (client == null && createdCount.get() < maxTotal) {
                        // Create new client if under max total
                        client = createClient();
                    }
                    if (client == null) {
                        throw new Exception("Pool exhausted and max total reached");
                    }
                    // Validate client
                    String apiVersion = client.getAPIVersion();
                    logger.debug("Validated client and fetched api version " + apiVersion);
                    return client;
                } catch (Exception e) {
                    logger.warn("Failed to validate the client. Retrying " + i, e);
                    if (client != null) {
                        returnBrokenResource(client);
                    }
                }
            }
            throw new Exception("Failed to fetch a client from the pool after validation");
        } catch (Exception e) {
            throw new ThriftClientException("Could not get a resource from the pool", e);
        }
    }

    public void returnResourceObject(T resource) {
        try {
            if (!pool.offer(resource)) {
                // Pool is full, destroy the client
                destroyClient(resource);
                createdCount.decrementAndGet();
            }
        } catch (Exception e) {
            logger.warn("Error returning resource to pool", e);
            destroyClient(resource);
            createdCount.decrementAndGet();
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
            destroyClient(resource);
            createdCount.decrementAndGet();
        } catch (Exception e) {
            logger.warn("Error destroying broken resource", e);
        }
    }

    public void destroy() {
        close();
    }

    public void close() {
        try {
            T client;
            while ((client = pool.poll()) != null) {
                destroyClient(client);
                createdCount.decrementAndGet();
            }
        } catch (Exception e) {
            logger.error("Error closing pool", e);
        }
    }

    /**
     * Simple pool configuration class to replace GenericObjectPoolConfig
     */
    public static class PoolConfig {
        private int maxTotal = 8;
        private int maxIdle = 8;
        private long maxWaitMillis = -1;
        private long timeBetweenEvictionRunsMillis = -1;

        public int getMaxTotal() {
            return maxTotal;
        }

        public void setMaxTotal(int maxTotal) {
            this.maxTotal = maxTotal;
        }

        public int getMaxIdle() {
            return maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public long getMaxWaitMillis() {
            return maxWaitMillis;
        }

        public void setMaxWaitMillis(long maxWaitMillis) {
            this.maxWaitMillis = maxWaitMillis;
        }

        public long getTimeBetweenEvictionRunsMillis() {
            return timeBetweenEvictionRunsMillis;
        }

        public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
            this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        }
    }
}
