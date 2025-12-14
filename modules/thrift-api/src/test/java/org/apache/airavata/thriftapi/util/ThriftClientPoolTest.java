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
package org.apache.airavata.thriftapi.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.thriftapi.service.BaseAPI;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ThriftClientPoolTest {

    private BaseAPI.Client mockClient;
    private TProtocol mockInputProtocol;
    private TProtocol mockOutputProtocol;
    private TTransport mockInputTransport;
    private TTransport mockOutputTransport;

    @BeforeEach
    public void setUp() {
        mockClient = mock(BaseAPI.Client.class);
        mockInputProtocol = mock(TProtocol.class);
        mockOutputProtocol = mock(TProtocol.class);
        mockInputTransport = mock(TTransport.class);
        mockOutputTransport = mock(TTransport.class);

        when(mockClient.getInputProtocol()).thenReturn(mockInputProtocol);
        when(mockClient.getOutputProtocol()).thenReturn(mockOutputProtocol);
        when(mockInputProtocol.getTransport()).thenReturn(mockInputTransport);
        when(mockOutputProtocol.getTransport()).thenReturn(mockOutputTransport);
        when(mockInputTransport.isOpen()).thenReturn(true);
        when(mockOutputTransport.isOpen()).thenReturn(true);
    }

    @Test
    public void testWithDefaultConfig() throws TException {
        when(mockClient.getAPIVersion()).thenReturn("0.19");

        var poolConfig = new ThriftClientPool.PoolConfig();
        var mockProtocol = mock(TProtocol.class);
        var thriftClientPool = new ThriftClientPool<>((protocol) -> mockClient, () -> mockProtocol, poolConfig);
        var client = thriftClientPool.getResource();
        thriftClientPool.returnResource(client);
        thriftClientPool.close();

        verify(mockInputTransport).close();
        verify(mockOutputTransport).close();
    }

    /**
     * Just like #{@link #testWithAbandonConfigAndAbandoned()} but using default
     * configuration.
     *
     * @throws TException
     * @throws ApplicationSettingsException
     */
    @Test
    @Disabled("Test requires long wait time to account for default removeAbandonedTimeout")
    public void testWithDefaultAbandonedRemovalEnabled() throws TException, ApplicationSettingsException {
        when(mockClient.getAPIVersion()).thenReturn("0.19");

        var poolConfig = new ThriftClientPool.PoolConfig();
        poolConfig.setTimeBetweenEvictionRunsMillis(1);
        var mockProtocol = mock(TProtocol.class);
        var thriftClientPool = new ThriftClientPool<>((protocol) -> mockClient, () -> mockProtocol, poolConfig);
        thriftClientPool.getResource();
        thriftClientPool.close();

        // Verify client is destroyed when pool is closed
        verify(mockInputTransport, times(1)).close();
        verify(mockOutputTransport, times(1)).close();
    }
}
