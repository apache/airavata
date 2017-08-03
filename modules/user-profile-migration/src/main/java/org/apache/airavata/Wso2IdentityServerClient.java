/**
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
 */
package org.apache.airavata;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

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


public class Wso2IdentityServerClient {
    /**
     * Server url of the WSO2 Carbon Server
     */
    private static String SEVER_URL = "https://idp.scigap.org:9443/services/";


    public static RemoteUserStoreManagerServiceStub getAdminServiceClient(String adminUserName, String adminPassword, String adminService){

        /**
         * trust store path.  this must contains server's  certificate or Server's CA chain
         */

        /* The below code snippet is intentionally commented for the build to pass,
         * because the private key and certificate file are not committed to GitHub,
         * which are needed to run the client */

//        String trustStore = System.getProperty("user.dir") + File.separator +
//                "modules" + File.separator + "user-profile-migration" + File.separator +
//                "src" + File.separator + "main" + File.separator +
//                "resources" + File.separator + "wso2carbon.jks";
//        System.out.println("file path : " + trustStore);

        /**
         * Call to https://localhost:9443/services/   uses HTTPS protocol.
         * Therefore we to validate the server certificate or CA chain. The server certificate is looked up in the
         * trust store.
         * Following code sets what trust-store to look for and its JKs password.
         */

//        System.setProperty("javax.net.ssl.trustStore",  trustStore );

//        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        // idp.scigap.org:9443 certificate has expired, so the following disables checking the certificate
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType){}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType){}
                }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLContext.setDefault(sc);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        /**
         * Axis2 configuration context
         */
        ConfigurationContext configContext;
        RemoteUserStoreManagerServiceStub adminStub;

        try {

            /**
             * Create a configuration context. A configuration context contains information for
             * axis2 environment. This is needed to create an axis2 service client
             */
            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);

            /**
             * end point url with service name
             */
//            String serviceEndPoint = SEVER_URL + "RemoteUserStoreManagerService";
            String serviceEndPoint = SEVER_URL + adminService;

            /**
             * create stub and service client
             */
            adminStub = new RemoteUserStoreManagerServiceStub(configContext, serviceEndPoint);
            ServiceClient client = adminStub._getServiceClient();
            Options option = client.getOptions();

            /**
             * Setting a authenticated cookie that is received from Carbon server.
             * If you have authenticated with Carbon server earlier, you can use that cookie, if
             * it has not been expired
             */
            option.setProperty(HTTPConstants.COOKIE_STRING, null);

            /**
             * Setting basic auth headers for authentication for carbon server
             */
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(adminUserName);
            auth.setPassword(adminPassword);
            auth.setPreemptiveAuthentication(true);
            option.setProperty(HTTPConstants.AUTHENTICATE, auth);
            option.setManageSession(true);
            return adminStub;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}