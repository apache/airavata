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
package org.apache.airavata.api.thrift.server;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.airavata.api.thrift.handler.GroupManagerServiceHandler;
import org.apache.airavata.api.thrift.handler.IamAdminServiceHandler;
import org.apache.airavata.api.thrift.handler.TenantProfileServiceHandler;
import org.apache.airavata.api.thrift.handler.UserProfileServiceHandler;
import org.apache.airavata.common.utils.DBInitConfig;
import org.apache.airavata.common.utils.DBInitializer;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.profile.commons.utils.user.UserProfileCatalogDBInitConfig;
import org.apache.airavata.profile.groupmanager.cpi.GroupManagerService;
import org.apache.airavata.profile.groupmanager.cpi.group_manager_cpiConstants;
import org.apache.airavata.profile.iam.admin.services.cpi.IamAdminServices;
import org.apache.airavata.profile.iam.admin.services.cpi.iam_admin_services_cpiConstants;
import org.apache.airavata.profile.tenant.cpi.TenantProfileService;
import org.apache.airavata.profile.tenant.cpi.profile_tenant_cpiConstants;
import org.apache.airavata.profile.user.cpi.UserProfileService;
import org.apache.airavata.profile.user.cpi.profile_user_cpiConstants;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Created by goshenoy on 03/08/2017.
 */
@Component
public class ProfileServiceServer implements IServer {

    private static final Logger logger = LoggerFactory.getLogger(ProfileServiceServer.class);

    private static final String SERVER_NAME = "Profile Service Server";
    private static final String SERVER_VERSION = "1.0";

    private ServerStatus status;
    private TServer server;
    private List<DBInitConfig> dbInitConfigs = Arrays.asList(new UserProfileCatalogDBInitConfig());

    @Autowired
    private ApplicationContext applicationContext;

    public ProfileServiceServer() {
        setStatus(ServerStatus.STOPPED);
    }

    public void updateTime() {}

    public Date getTime() {
        return null;
    }

    public String getName() {
        return SERVER_NAME;
    }

    public String getVersion() {
        return SERVER_VERSION;
    }

    public void start() throws Exception {

        try {
            setStatus(ServerStatus.STARTING);

            logger.info("Initialing profile service databases...");
            for (DBInitConfig dbInitConfig : dbInitConfigs) {
                DBInitializer.initializeDB(dbInitConfig);
            }
            logger.info("Profile service databases initialized successfully");

            final int serverPort = Integer.parseInt(ServerSettings.getProfileServiceServerPort());

            // Get handlers from Spring context
            UserProfileServiceHandler userProfileHandler = applicationContext.getBean(UserProfileServiceHandler.class);
            TenantProfileServiceHandler tenantProfileHandler =
                    applicationContext.getBean(TenantProfileServiceHandler.class);
            IamAdminServiceHandler iamAdminHandler = applicationContext.getBean(IamAdminServiceHandler.class);
            GroupManagerServiceHandler groupManagerHandler =
                    applicationContext.getBean(GroupManagerServiceHandler.class);

            // create multiple processors for each profile-service
            var userProfileProcessor = new UserProfileService.Processor<>(userProfileHandler);
            var teneantProfileProcessor = new TenantProfileService.Processor<>(tenantProfileHandler);
            var iamAdminServicesProcessor = new IamAdminServices.Processor<>(iamAdminHandler);
            var groupmanagerProcessor = new GroupManagerService.Processor<>(groupManagerHandler);

            // create a multiplexed processor
            TMultiplexedProcessor profileServiceProcessor = new TMultiplexedProcessor();
            profileServiceProcessor.registerProcessor(
                    profile_user_cpiConstants.USER_PROFILE_CPI_NAME, userProfileProcessor);
            profileServiceProcessor.registerProcessor(
                    profile_tenant_cpiConstants.TENANT_PROFILE_CPI_NAME, teneantProfileProcessor);
            profileServiceProcessor.registerProcessor(
                    iam_admin_services_cpiConstants.IAM_ADMIN_SERVICES_CPI_NAME, iamAdminServicesProcessor);
            profileServiceProcessor.registerProcessor(
                    group_manager_cpiConstants.GROUP_MANAGER_CPI_NAME, groupmanagerProcessor);

            TServerTransport serverTransport;
            InetSocketAddress inetSocketAddress = new InetSocketAddress("0.0.0.0", serverPort);
            serverTransport = new TServerSocket(inetSocketAddress);
            TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
            options.minWorkerThreads = 30;
            server = new TThreadPoolServer(options.processor(profileServiceProcessor));

            new Thread() {
                public void run() {
                    server.serve();
                    setStatus(ServerStatus.STOPPED);
                    logger.info("Profile Service Server Stopped.");
                }
            }.start();
            new Thread() {
                public void run() {
                    while (!server.isServing()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    if (server.isServing()) {
                        setStatus(ServerStatus.STARTED);
                        logger.info("Starting Profile Service Server on Port " + serverPort);
                        logger.info("Listening to Profile Service Server clients ....");
                    }
                }
            }.start();
        } catch (TTransportException e) {
            setStatus(ServerStatus.FAILED);
            throw new Exception("Error while starting the Profile Service Server", e);
        }
    }

    public void stop() throws Exception {

        if (server != null && server.isServing()) {
            setStatus(ServerStatus.STOPING);
            server.stop();
        }
    }

    public void restart() throws Exception {

        stop();
        start();
    }

    public void configure() throws Exception {}

    public ServerStatus getStatus() throws Exception {
        return status;
    }

    private void setStatus(ServerStatus stat) {
        status = stat;
        status.updateTime();
    }

    public TServer getServer() {
        return server;
    }

    public void setServer(TServer server) {
        this.server = server;
    }
}
