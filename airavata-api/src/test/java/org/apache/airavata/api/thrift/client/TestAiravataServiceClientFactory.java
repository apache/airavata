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
package org.apache.airavata.api.thrift.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.security.AuthzToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {org.apache.airavata.config.JpaConfig.class})
@TestPropertySource(locations = "classpath:airavata.properties")
public class TestAiravataServiceClientFactory {

    @Autowired
    private static AiravataServerProperties properties;

    public static void main(String[] a) throws ApplicationSettingsException, AiravataClientException {
        AuthzToken token = new AuthzToken();
        token.setAccessToken(
                "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJBUGFKRUpERFc4ZEdzMExnc3ozYUdydERsZ2U0eWlQblFibUNsYnpJX2NVIn0.eyJqdGkiOiI1NmMwZDZmYy0yMGVhLTQ1Y2UtODUwNC1kMTY0MTZkYTdkYzEiLCJleHAiOjE2NDc0NTQyNjcsIm5iZiI6MCwiaWF0IjoxNjQ3NDUyNDY3LCJpc3MiOiJodHRwczovL2lhbWRldi5zY2lnYXAub3JnL2F1dGgvcmVhbG1zL3NlYWdyaWQiLCJhdWQiOiJwZ2EiLCJzdWIiOiI3ZGZkYjI4MS1lNWIzLTQ4MjQtOTcxZC00YzQ2ZmNkMzIwYTEiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJwZ2EiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiI1NWVkODI5OS0xN2FiLTQwNTEtYTBjYy0zMjgzNWQ1MTVlNjUiLCJhY3IiOiIxIiwiY2xpZW50X3Nlc3Npb24iOiIwMjU2OTljNS1lY2I2LTQ2ZDYtYmYwNy01ZDczOTk1ZTI3YjMiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cHM6Ly9kZXYuc2VhZ3JpZC5vcmciXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsicGdhIjp7InJvbGVzIjpbImdhdGV3YXktdXNlciJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsInZpZXctcHJvZmlsZSJdfX0sIm5hbWUiOiJFcm9tYSBBYmV5c2luZ2hlIiwicHJlZmVycmVkX3VzZXJuYW1lIjoiMjAyMXRlc3QxIiwiZ2l2ZW5fbmFtZSI6IkVyb21hIiwiZmFtaWx5X25hbWUiOiJBYmV5c2luZ2hlIiwiZW1haWwiOiJlcm9tYS5hYmV5c2luZ2hlQGdtYWlsLmNvbSJ9.eMIrTzyc43CLkxCauiXIwPV99CmsBDbSbiIVEE9Qd3ASyJKXlzkrWsUVPE-g43i1iBKaHBcnLPkmzVz8Hb0B1wtDA5nKSgipGYjfJfaWdMzBrW1PkpeWMKDZHN3m4OS7YZnzQki0YJFvL1-IZsYf2UCnr_lsOi2M-dnj9xwEJ_VIdvvHl9I6ivhBUywYDU0uL9EoSL3kAes7FvooOhXnZiRxJpZK82VPZZiVAb-nv5xgCwQw0ipbm8b0kIta4cxhjKKDhyINRvGXJjqN3kRNsahYHLnwsRqRjabgvbSfe4vtS5iRoPO-qF-I-rSMf2jZPREMWxdLQ9uPXEk9mFxqbQ");
        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put(Constants.GATEWAY_ID, "seagrid");
        claimsMap.put(Constants.USER_NAME, "2021test1");
        token.setClaimsMap(claimsMap);
        Airavata.Client apiClient = AiravataServiceClientFactory.createAiravataClient(
                "apidev.scigap.org", 8930, ServerSettings.isTLSEnabled(), properties);

        List<String> outputNames = new ArrayList<>();
        outputNames.add("Gaussian-Application-Output");
        outputNames.add("Gaussian-Standar-Out");
        try {
            apiClient.fetchIntermediateOutputs(
                    token,
                    "Clone_of_Gaussian16_on_Mar_16,_2022_1:42_PM_1ad9e887-6ec4-4b1a-9ffb-e028ccb3c86c",
                    outputNames);
        } catch (Exception e) {
            var exception = new AiravataClientException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setParameter("Error while fetching intermediate outputs");
            exception.initCause(e);
            throw exception;
        }
    }
}
