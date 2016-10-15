/*
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
 *
*/
package org.apache.airavata.sharing.registry;

import org.apache.airavata.sharing.registry.models.Domain;
import org.apache.airavata.sharing.registry.models.User;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SharingRegistryServiceTest {
    private final static Logger logger = LoggerFactory.getLogger(SharingRegistryServiceTest.class);


    @Test
    @Ignore("Test is only for demonstration purposes")
    public void test() throws TException {
        String serverHost = "gw56.iu.xsede.org";
        int serverPort = 7878;

        TTransport transport = new TSocket(serverHost, serverPort);
        transport.open();
        TProtocol protocol = new TBinaryProtocol(transport);
        SharingRegistryService.Client sharingServiceClient = new SharingRegistryService.Client(protocol);

        Domain domain = new Domain();
        //has to be one word
        domain.setName("test-domain"+System.currentTimeMillis());
        //optional
        domain.setDescription("test domain description");

        String domainId = sharingServiceClient.createDomain(domain);

        User user = new User();
        String userName = "test-user";
        String userId1 =  userName + "@" + domainId;
        //required
        user.setUserId(userId1);
        //required
        user.setUserName(userName);
        //required
        user.setDomainId(domainId);
        //required
        user.setFirstName("John");
        //required
        user.setLastName("Doe");
        //required
        user.setEmail("john.doe@abc.com");
        //optional - this should be bytes of the users image icon
        byte[] icon = new byte[10];
        user.setIcon(icon);

        //can be manually set. otherwise will be set to the current time by the system
        user.setCreatedTime(System.currentTimeMillis());
        user.setUpdatedTime(System.currentTimeMillis());

        sharingServiceClient.registerUser(user);
    }
}