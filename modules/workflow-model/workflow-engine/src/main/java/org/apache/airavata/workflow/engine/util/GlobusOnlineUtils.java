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
package org.apache.airavata.workflow.engine.util;
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.xbaya.util;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpHost;
//import org.apache.http.auth.AuthScope;
//import org.apache.http.auth.UsernamePasswordCredentials;
//import org.apache.http.client.AuthCache;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.CredentialsProvider;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.protocol.HttpClientContext;
//import org.apache.http.impl.auth.BasicScheme;
//import org.apache.http.impl.client.BasicAuthCache;
//import org.apache.http.impl.client.BasicCredentialsProvider;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//import org.globusonline.transfer.APIError;
//import org.globusonline.transfer.Authenticator;
//import org.globusonline.transfer.GoauthAuthenticator;
//import org.globusonline.transfer.JSONTransferAPIClient;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.json.JSONTokener;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.security.GeneralSecurityException;
//import java.security.KeyManagementException;
//import java.security.NoSuchAlgorithmException;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//public class GlobusOnlineUtils {
//    public static final String ACCESS_TOKEN = "access_token";
//
//    private static String goUserName;
//    private static String goPWD;
//
//    public static void main(String[] args) {
////        String s = appendFileName("/~/Desktop/1.docx", "/~/");
////        System.out.println(s);
//
//    }
//
//    public GlobusOnlineUtils(String goUsername, String goPwd) {
//        goUserName = goUsername;
//        goPWD = goPwd;
//    }
//
//    public String getAuthenticationToken() {
//        String token = null;
//        HttpHost targetHost = new HttpHost(GOConstants.NEXUS_API_HOST, GOConstants.NEXUS_API_PORT, GOConstants.NEXUS_API_SCHEMA);
//        CredentialsProvider credsProvider = new BasicCredentialsProvider();
//        credsProvider.setCredentials(
//                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
//                new UsernamePasswordCredentials(goUserName, goPWD));
//
//        CloseableHttpClient httpclient = HttpClients.custom()
//                .setDefaultCredentialsProvider(credsProvider).build();
//        try {
//
//            // Create AuthCache instance
//            AuthCache authCache = new BasicAuthCache();
//            // Generate BASIC scheme object and add it to the local
//            // auth cache
//            BasicScheme basicScheme = new BasicScheme();
//            authCache.put(targetHost, basicScheme);
//
//            // Add AuthCache to the execution context
//            HttpClientContext localContext = HttpClientContext.create();
//            localContext.setAuthCache(authCache);
//
//            HttpGet httpget = new HttpGet(GOConstants.GOAUTH_TOKEN_REQ_URL);
//            httpget.addHeader("accept", "application/json");
//            System.out.println("executing request: " + httpget.getRequestLine());
//            System.out.println("to target: " + targetHost);
//
//            CloseableHttpResponse response = httpclient.execute(targetHost, httpget, localContext);
//            try {
//                HttpEntity entity = response.getEntity();
//                InputStream entityContent = entity.getContent();
//                InputStreamReader reader = new InputStreamReader(entityContent);
//                JSONTokener tokenizer = new JSONTokener(reader);
//                JSONObject json = new JSONObject(tokenizer);
//                token = (String)json.get(ACCESS_TOKEN);
//                entityContent.close();
//                EntityUtils.consume(entity);
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            } finally {
//                response.close();
//            }
//            //}
//        } catch (ClientProtocolException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                httpclient.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return token;
//    }
//
//    public JSONTransferAPIClient getAuthenticated (){
//        JSONTransferAPIClient jsonTransferAPIClient = null;
//        try {
//            String authenticationToken = getAuthenticationToken();
//            Authenticator authenticator = new GoauthAuthenticator(authenticationToken);
//            jsonTransferAPIClient = new JSONTransferAPIClient(goUserName,
//                    null, GOConstants.BASEURL);
//            jsonTransferAPIClient.setAuthenticator(authenticator);
//        } catch (KeyManagementException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        return jsonTransferAPIClient;
//    }
//
//    public String transferFiles (TransferFile tf){
//        String taskId = null;
//        try {
//            JSONTransferAPIClient apiClient = getAuthenticated();
//            String submissionId = apiClient.getSubmissionId();
//            tf.setSubmission_id(submissionId);
//            JSONObject jsonObject = new JSONObject(tf);
//            JSONTransferAPIClient.Result result = apiClient.transfer(jsonObject);
//            taskId = (String)result.document.get("task_id");
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (GeneralSecurityException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (APIError apiError) {
//            apiError.printStackTrace();
//        }
//        return taskId;
//    }
//
//    public TransferFile getTransferFile (String sourceEp,
//                                                String destEp,
//                                                String sourcePath,
//                                                String destPath,
//                                                String label){
//
//        TransferFile transferFile = new TransferFile();
//
//
//        transferFile.setPreserve_timestamp(false);
//        transferFile.setDATA_TYPE("transfer");
//        transferFile.setEncrypt_data(false);
//        transferFile.setSync_level(null);
//        transferFile.setSource_endpoint(sourceEp);
//        transferFile.setLabel(label);
//        transferFile.setDestination_endpoint(destEp);
//        transferFile.setLength(2);
//        transferFile.setDeadline(getDeadlineForTransfer());
//        transferFile.setNotify_on_succeeded(true);
//        transferFile.setNotify_on_failed(true);
//        transferFile.setVerify_checksum(false);
//        transferFile.setDelete_destination_extra(false);
//        Data[] datas = new Data[1];
//        Data data = new Data();
//        data.setDATA_TYPE("transfer_item");
//        data.setDestination_path(appendFileName(sourcePath, destPath));
//        data.setVerify_size(null);
//        data.setSource_path(sourcePath);
//        data.setRecursive(false);
//        datas[0] = data;
//        transferFile.setDATA(datas);
//        return transferFile;
//    }
//
//    private static String appendFileName(String sourcePath, String destPath){
//        String[] split = sourcePath.split(File.separator);
//        String fileName = split[split.length - 1];
//        if (destPath.endsWith(File.separator)){
//            destPath = destPath.concat(fileName);
//        }else {
//            destPath = destPath.concat("/" + fileName);
//        }
//        System.out.println(destPath);
//        return destPath;
//    }
//
//    private String getDeadlineForTransfer (){
//        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(calendar.DAY_OF_MONTH, 1);
//        Date tomorrow = calendar.getTime();
//        String date = dateFormat.format(tomorrow);
//        System.out.println(date);
//        return date;
//    }
//
//    public List<String> getEPList() throws IOException, APIError, GeneralSecurityException, JSONException {
//        List<String> epList = new ArrayList<String>();
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("limit", "0");
//        JSONTransferAPIClient transferAPIClient = getAuthenticated();
//        JSONTransferAPIClient.Result result = transferAPIClient.getResult("/endpoint_list", params);
//        JSONObject document = result.document;
//        JSONArray dataArray = document.getJSONArray("DATA");
//        for (int i = 0; i < dataArray.length(); i++ ){
//            JSONObject jsonObject = dataArray.getJSONObject(i);
//            String epName = (String)jsonObject.get("canonical_name");
//            epList.add(epName);
//        }
//        return epList;
//    }
//
//}
