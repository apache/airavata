/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.helix.impl.task.aws;

import org.apache.airavata.agents.api.AgentUtils;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;

public final class AWSTaskUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AWSTaskUtil.class);

    private AWSTaskUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static Ec2Client buildEc2Client(String token, String gatewayId, String region) throws Exception {
        LOGGER.info("Building EC2 client for token {} and gateway id {} in region {}", token, gatewayId, region);
        PasswordCredential pwdCred = AgentUtils.getCredentialClient().getPasswordCredential(token, gatewayId);
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(pwdCred.getLoginUserName(), pwdCred.getPassword()); // TODO support using AWS Credential
        return Ec2Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }
}
