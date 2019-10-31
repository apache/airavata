package org.apache.airavata.common.utils;

import org.apache.airavata.base.api.BaseAPI;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class ThriftClientPoolTest {

    @Mocked
    private BaseAPI.Client mockClient;

    @Test
    public void testWithDefaultConfig() throws TException {
        new Expectations() {
            {
                mockClient.getAPIVersion();
                result = "0.19";
                mockClient.getInputProtocol().getTransport().isOpen();
                result = true;
                mockClient.getOutputProtocol().getTransport().isOpen();
                result = true;
            }
        };

        GenericObjectPoolConfig<BaseAPI.Client> poolConfig = new GenericObjectPoolConfig<>();
        ThriftClientPool<BaseAPI.Client> thriftClientPool = new ThriftClientPool<>((protocol) -> mockClient, () -> null,
                poolConfig);
        BaseAPI.Client client = thriftClientPool.getResource();
        thriftClientPool.returnResource(client);
        thriftClientPool.close();

        new Verifications() {
            {
                mockClient.getInputProtocol().getTransport().close();
                mockClient.getOutputProtocol().getTransport().close();
            }
        };
    }

    @Test
    public void testWithAbandonConfigAndAbandoned() throws TException {

        new Expectations() {
            {
                mockClient.getAPIVersion();
                result = "0.19";
                mockClient.getInputProtocol().getTransport().isOpen();
                result = true;
                mockClient.getOutputProtocol().getTransport().isOpen();
                result = true;
            }
        };

        GenericObjectPoolConfig<BaseAPI.Client> poolConfig = new GenericObjectPoolConfig<>();
        // timeBetweenEvictionRunsMillis must be positive for abandoned removal on
        // maintenance to run
        poolConfig.setTimeBetweenEvictionRunsMillis(1);
        AbandonedConfig abandonedConfig = new AbandonedConfig();
        abandonedConfig.setRemoveAbandonedTimeout(1);
        abandonedConfig.setRemoveAbandonedOnMaintenance(true);
        abandonedConfig.setLogAbandoned(true);
        ThriftClientPool<BaseAPI.Client> thriftClientPool = new ThriftClientPool<>((protocol) -> mockClient, () -> null,
                poolConfig, abandonedConfig);
        thriftClientPool.getResource();
        try {
            // Sleep long enough for the client to be considered abandoned
            Thread.sleep(1001);
            thriftClientPool.close();
        } catch (InterruptedException e) {
            Assert.fail("sleep interrupted");
        }

        new Verifications() {
            {
                // Verify client is destroyed when abandoned
                mockClient.getInputProtocol().getTransport().close();
                times = 1;
                mockClient.getOutputProtocol().getTransport().close();
                times = 1;
            }
        };
    }

    /**
     * Just like #{@link #testWithAbandonConfigAndAbandoned()} but using default
     * configuration.
     * 
     * @throws TException
     * @throws ApplicationSettingsException
     */
    @Test
    @Ignore("Test requires long wait time to account for default removeAbandonedTimeout")
    public void testWithDefaultAbandonedRemovalEnabled() throws TException, ApplicationSettingsException {

        new Expectations() {
            {
                mockClient.getAPIVersion();
                result = "0.19";
                mockClient.getInputProtocol().getTransport().isOpen();
                result = true;
                mockClient.getOutputProtocol().getTransport().isOpen();
                result = true;
            }
        };

        GenericObjectPoolConfig<BaseAPI.Client> poolConfig = new GenericObjectPoolConfig<>();
        // timeBetweenEvictionRunsMillis must be positive for abandoned removal on
        // maintenance to run
        poolConfig.setTimeBetweenEvictionRunsMillis(1);
        ServerSettings.setSetting("thrift.client.pool.abandoned.removal.enabled", "true");
        ThriftClientPool<BaseAPI.Client> thriftClientPool = new ThriftClientPool<>((protocol) -> mockClient, () -> null,
                poolConfig);
        thriftClientPool.getResource();
        try {
            // Sleep long enough for the client to be considered abandoned
            // Default removeAbandonedTimeout is 300 seconds
            Thread.sleep(new AbandonedConfig().getRemoveAbandonedTimeout() * 1000 + 1);
            thriftClientPool.close();
        } catch (InterruptedException e) {
            Assert.fail("sleep interrupted");
        }

        new Verifications() {{
            // Verify client is destroyed when abandoned
            mockClient.getInputProtocol().getTransport().close(); times = 1;
            mockClient.getOutputProtocol().getTransport().close(); times = 1;
        }};
    }
}
