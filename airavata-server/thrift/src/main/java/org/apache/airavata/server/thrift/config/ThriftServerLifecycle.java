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
package org.apache.airavata.server.thrift.config;

import java.net.InetSocketAddress;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class ThriftServerLifecycle implements SmartLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(ThriftServerLifecycle.class);

    private final TMultiplexedProcessor processor;

    @Value("${airavata.thrift.port:8930}")
    private int port;

    @Value("${airavata.thrift.host:#{null}}")
    private String host;

    @Value("${airavata.thrift.min-threads:50}")
    private int minThreads;

    private volatile TServer server;
    private volatile boolean running;

    public ThriftServerLifecycle(TMultiplexedProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void start() {
        try {
            TServerTransport serverTransport;
            if (host == null || host.isBlank()) {
                serverTransport = new TServerSocket(port);
            } else {
                serverTransport = new TServerSocket(new InetSocketAddress(host, port));
            }

            TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
            options.minWorkerThreads = minThreads;
            server = new TThreadPoolServer(options.processor(processor));

            Thread serveThread = new Thread(() -> {
                logger.info("Airavata Thrift Server starting on port {}", port);
                server.serve();
                logger.info("Airavata Thrift Server stopped");
            }, "airavata-thrift-server");
            serveThread.setDaemon(true);
            serveThread.start();

            running = true;
            logger.info("Airavata Thrift Server lifecycle started (port={})", port);
        } catch (TTransportException e) {
            throw new IllegalStateException("Failed to start Thrift server on port " + port, e);
        }
    }

    @Override
    public void stop() {
        if (server != null && server.isServing()) {
            server.stop();
        }
        running = false;
        logger.info("Airavata Thrift Server lifecycle stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        // Start after other beans, stop before them
        return Integer.MAX_VALUE - 100;
    }
}
