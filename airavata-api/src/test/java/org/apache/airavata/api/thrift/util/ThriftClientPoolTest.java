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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.airavata.base.api.BaseAPI;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ApplicationSettings;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, ThriftClientPoolTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
public class ThriftClientPoolTest {

    @MockitoBean
    private BaseAPI.Client mockClient;

    public ThriftClientPoolTest(BaseAPI.Client mockClient) {
        this.mockClient = mockClient;
    }

    @Test
    public void testWithDefaultConfig() throws TException {
        when(mockClient.getAPIVersion()).thenReturn("0.19");
        when(mockClient.getInputProtocol().getTransport().isOpen()).thenReturn(true);
        when(mockClient.getOutputProtocol().getTransport().isOpen()).thenReturn(true);

        GenericObjectPoolConfig<BaseAPI.Client> poolConfig = new GenericObjectPoolConfig<>();
        ThriftClientPool<BaseAPI.Client> thriftClientPool =
                new ThriftClientPool<>((protocol) -> mockClient, () -> null, poolConfig);
        BaseAPI.Client client = thriftClientPool.getResource();
        thriftClientPool.returnResource(client);
        thriftClientPool.close();

        verify(mockClient.getInputProtocol().getTransport()).close();
        verify(mockClient.getOutputProtocol().getTransport()).close();
    }

    @Test
    public void testWithAbandonConfigAndAbandoned() throws TException {

        when(mockClient.getAPIVersion()).thenReturn("0.19");
        when(mockClient.getInputProtocol().getTransport().isOpen()).thenReturn(true);
        when(mockClient.getOutputProtocol().getTransport().isOpen()).thenReturn(true);

        GenericObjectPoolConfig<BaseAPI.Client> poolConfig = new GenericObjectPoolConfig<>();
        // timeBetweenEvictionRunsMillis must be positive for abandoned removal on
        // maintenance to run
        poolConfig.setTimeBetweenEvictionRunsMillis(1);
        AbandonedConfig abandonedConfig = new AbandonedConfig();
        abandonedConfig.setRemoveAbandonedTimeout(1);
        abandonedConfig.setRemoveAbandonedOnMaintenance(true);
        abandonedConfig.setLogAbandoned(true);
        StringWriter log = new StringWriter();
        assertEquals(0, log.toString().length(), "Initial length of log is 0");
        PrintWriter logWriter = new PrintWriter(log);
        abandonedConfig.setLogWriter(logWriter);
        ThriftClientPool<BaseAPI.Client> thriftClientPool =
                new ThriftClientPool<>((protocol) -> mockClient, () -> null, poolConfig, abandonedConfig);
        thriftClientPool.getResource();
        try {
            // Sleep long enough for the client to be considered abandoned
            Thread.sleep(1001);
            thriftClientPool.close();
        } catch (InterruptedException e) {
            fail("sleep interrupted");
        }

        assertTrue(log.toString().length() > 0);
        // The stack trace should contain this method's name
        assertTrue(log.toString().contains("testWithAbandonConfigAndAbandoned"));

        // Verify client is destroyed when abandoned
        verify(mockClient.getInputProtocol().getTransport(), times(1)).close();
        verify(mockClient.getOutputProtocol().getTransport(), times(1)).close();
    }

    @Test
    public void testWithAbandonConfigAndAbandonedAndNotLogged() throws TException {

        when(mockClient.getAPIVersion()).thenReturn("0.19");
        when(mockClient.getInputProtocol().getTransport().isOpen()).thenReturn(true);
        when(mockClient.getOutputProtocol().getTransport().isOpen()).thenReturn(true);

        GenericObjectPoolConfig<BaseAPI.Client> poolConfig = new GenericObjectPoolConfig<>();
        // timeBetweenEvictionRunsMillis must be positive for abandoned removal on
        // maintenance to run
        poolConfig.setTimeBetweenEvictionRunsMillis(1);
        AbandonedConfig abandonedConfig = new AbandonedConfig();
        abandonedConfig.setRemoveAbandonedTimeout(1);
        abandonedConfig.setRemoveAbandonedOnMaintenance(true);
        abandonedConfig.setLogAbandoned(false);
        // Setup log writer so we can verify that nothing was logged
        StringWriter log = new StringWriter();
        assertEquals(0, log.toString().length(), "Initial length of log is 0");
        PrintWriter logWriter = new PrintWriter(log);
        abandonedConfig.setLogWriter(logWriter);
        ThriftClientPool<BaseAPI.Client> thriftClientPool =
                new ThriftClientPool<>((protocol) -> mockClient, () -> null, poolConfig, abandonedConfig);
        thriftClientPool.getResource();
        try {
            // Sleep long enough for the client to be considered abandoned
            Thread.sleep(1001);
            thriftClientPool.close();
        } catch (InterruptedException e) {
            fail("sleep interrupted");
        }

        // Verify that nothing was logged
        assertEquals(0, log.toString().length());

        // Verify client is destroyed when abandoned
        verify(mockClient.getInputProtocol().getTransport(), times(1)).close();
        verify(mockClient.getOutputProtocol().getTransport(), times(1)).close();
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
        when(mockClient.getInputProtocol().getTransport().isOpen()).thenReturn(true);
        when(mockClient.getOutputProtocol().getTransport().isOpen()).thenReturn(true);

        GenericObjectPoolConfig<BaseAPI.Client> poolConfig = new GenericObjectPoolConfig<>();
        // timeBetweenEvictionRunsMillis must be positive for abandoned removal on
        // maintenance to run
        poolConfig.setTimeBetweenEvictionRunsMillis(1);
        ApplicationSettings.setSetting("airavata.thrift-client-pool-abandoned-removal-enabled", "true");
        ThriftClientPool<BaseAPI.Client> thriftClientPool =
                new ThriftClientPool<>((protocol) -> mockClient, () -> null, poolConfig);
        thriftClientPool.getResource();
        try {
            // Sleep long enough for the client to be considered abandoned
            // Default removeAbandonedTimeout is 300 seconds
            Thread.sleep(new AbandonedConfig().getRemoveAbandonedTimeout() * 1000 + 1);
            thriftClientPool.close();
        } catch (InterruptedException e) {
            fail("sleep interrupted");
        }

        // Verify client is destroyed when abandoned
        verify(mockClient.getInputProtocol().getTransport(), times(1)).close();
        verify(mockClient.getOutputProtocol().getTransport(), times(1)).close();
    }

    @org.springframework.context.annotation.Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.api",
                "org.apache.airavata.config"
            },
            excludeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                            org.apache.airavata.config.ThriftServerLauncher.class
                        })
            })
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
    static class TestConfiguration {}
}
