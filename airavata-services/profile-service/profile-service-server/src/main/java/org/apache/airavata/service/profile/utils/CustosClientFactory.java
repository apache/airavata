package org.apache.airavata.service.profile.utils;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.custos.client.profile.service.CustosProfileServiceClientFactory;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustosClientFactory {

    private final static Logger logger = LoggerFactory.getLogger(CustosClientFactory.class);

    private static String serverHost;
    private static String serverPort;

    {
        try {
            serverHost = ServerSettings.getCustosProfileServiceHost();
            serverPort = ServerSettings.getCustosProfileServicePort();
        } catch (ApplicationSettingsException e) {
            logger.error("Internal error in creating client");
            e.printStackTrace();
        }
    }

    public static org.apache.custos.profile.tenant.cpi.TenantProfileService.Client getCustosTenantProfileClient() throws TException {
        try {
            org.apache.custos.profile.tenant.cpi.TenantProfileService.Client custosClient = CustosProfileServiceClientFactory.createCustosTenantProfileServiceClient(serverHost, Integer.parseInt(serverPort));
            return custosClient;
        } catch (Exception e) {
            throw new TException("Unable to create custos tenant profile client", e);
        }
    }

    public static org.apache.custos.profile.user.cpi.UserProfileService.Client getCustosUserProfileClient() throws TException{
        try {
            org.apache.custos.profile.user.cpi.UserProfileService.Client custosClient = CustosProfileServiceClientFactory.createCustosUserProfileServiceClient(serverHost, Integer.parseInt(serverPort));
            return custosClient;
        } catch (Exception e) {
            throw new TException("Unable to create custos user profile client", e);
        }
    }

    public static org.apache.custos.profile.iam.admin.services.cpi.IamAdminServices.Client getCustosIamAdminServicesClient() throws TException{
        try {
            org.apache.custos.profile.iam.admin.services.cpi.IamAdminServices.Client custosClient = CustosProfileServiceClientFactory.createCustosIamAdminServiceClient(serverHost, Integer.parseInt(serverPort));
            return custosClient;
        } catch (Exception e) {
            throw new TException("Unable to create custos user profile client", e);
        }
    }
}
