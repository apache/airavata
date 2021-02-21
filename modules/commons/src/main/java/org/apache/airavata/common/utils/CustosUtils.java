package org.apache.airavata.common.utils;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.custos.clients.CustosClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CustosUtils {

    private final static Logger logger = LoggerFactory.getLogger(CustosUtils.class);

    public static CustosClientProvider getCustosClientProvider() throws ApplicationSettingsException, IOException {
        try {

            return new CustosClientProvider.Builder()
                    .setServerHost(ServerSettings.getCustosServerHost())
                    .setServerPort(ServerSettings.getCustosServerPort())
                    .setClientId(ServerSettings.getCustosClientId())
                    .setClientSec(ServerSettings.getCustosClientSec()).build();

        } catch (ApplicationSettingsException e) {
            logger.error("Failed to create Custos client provider", e);
            throw e;
        }

    }
}
