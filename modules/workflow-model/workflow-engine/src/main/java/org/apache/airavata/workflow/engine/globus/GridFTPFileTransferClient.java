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
package org.apache.airavata.workflow.engine.globus;
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
//package org.apache.airavata.xbaya.globus;
//
//import org.globusonline.transfer.APIError;
//import org.globusonline.transfer.BaseTransferAPIClient;
//import org.globusonline.transfer.JSONTransferAPIClient;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.IOException;
//import java.security.GeneralSecurityException;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//
//public class GridFTPFileTransferClient {
//    private JSONTransferAPIClient client;
//    private static DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
//
//    public GridFTPFileTransferClient(JSONTransferAPIClient client) {
//        this.client = client;
//    }
//
//    public static void main(String args[]) {
//        String username = "heshan";
//        String caFile = "/home/heshan/Dev/globusonline/transfer-api-client-java.git/trunk/ca/gd-bundle_ca.cert";
//        String certFile = "/tmp/x509up_u780936";
//        String keyFile = "/tmp/x509up_u780936";
//        String baseUrl = null;
//
//        String sourceEndpoint = "xsede#ranger";
//        String sourceFilePath = "~/tmp.log";
//        String destEndpoint = "xsede#trestles";
//        String destFilePath = "~/tmp.log.copy";
//
//        // String destEndpoint = "heshan#my_testEndpoint";
//        // String sourceFilePath = "~/var_tables.mod";
//        try {
//            JSONTransferAPIClient c = new JSONTransferAPIClient(username, caFile, certFile, keyFile, baseUrl);
//            System.out.println("base url: " + c.getBaseUrl());
//            GridFTPFileTransferClient e = new GridFTPFileTransferClient(c);
//            e.transfer(sourceEndpoint, sourceFilePath, destEndpoint, destFilePath);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Transfers a file from source endpoint to destination endpoint.
//     * 
//     * @param sourceEndpoint
//     *            Source endpoint
//     * @param sourceFilePath
//     *            Source file path
//     * @param destEndpoint
//     *            Destination endpoint
//     * @param destFilePath
//     *            Destination file path
//     * @throws IOException
//     *             IOException
//     * @throws JSONException
//     *             JSONException
//     * @throws GeneralSecurityException
//     *             GeneralSecurityException
//     * @throws APIError
//     *             APIError
//     */
//    public void transfer(String sourceEndpoint, String sourceFilePath, String destEndpoint, String destFilePath)
//            throws IOException, JSONException, GeneralSecurityException, APIError {
//        System.out.println("Starting transfer...");
//
//        // displayTasksummary();
//        // displayTaskList(60 * 60 * 24 * 7); // tasks at most a week old
//        // displayEndpointList();
//
//        if (!autoActivate(sourceEndpoint) || !autoActivate(destEndpoint)) {
//            System.err.println("Unable to auto activate go tutorial endpoints, " + " exiting");
//            return;
//        }
//
//        // displayLs(sourceEndpoint, "~");
//        // displayLs(destEndpoint, "~");
//
//        JSONTransferAPIClient.Result r = client.getResult(FileTransferConstants.SUBMISSION_ID_ENDPOINT);
//        String submissionId = r.document.getString(FileTransferConstants.VALUE);
//        JSONObject transfer = new JSONObject();
//        transfer.put(FileTransferConstants.DATA_TYPE, FileTransferConstants.TRANSFER);
//        transfer.put(FileTransferConstants.DEPLOYMENT_ID, submissionId);
//        JSONObject item = new JSONObject();
//        item.put(FileTransferConstants.DATA_TYPE, FileTransferConstants.TRANSFER_ITEM);
//        item.put(FileTransferConstants.SOURCE_ENDPOINT, sourceEndpoint);
//        item.put(FileTransferConstants.SOURCE_PATH, sourceFilePath);
//        item.put(FileTransferConstants.DESTINATION_ENDPOINT, destEndpoint);
//        item.put(FileTransferConstants.DESTINATION_PATH, destFilePath);
//        transfer.append(FileTransferConstants.DATA, item);
//
//        r = client.postResult(FileTransferConstants.TRANSFER_ENDPOINT, transfer.toString(), null);
//        String taskId = r.document.getString(FileTransferConstants.TASK_ID);
//        if (!waitForTask(taskId, 120)) {
//            System.out.println("Transfer not complete after 2 minutes, exiting");
//            return;
//        }
//
//        System.out.println("Transfer completed...");
//
//        // displayTasksummary();
//        // displayLs(destEndpoint, "~");
//    }
//
//    public void displayTasksummary() throws IOException, JSONException, GeneralSecurityException, APIError {
//        JSONTransferAPIClient.Result r = client.getResult("/tasksummary");
//        System.out.println("Task Summary for " + client.getUsername() + ": ");
//        Iterator keysIter = r.document.sortedKeys();
//        while (keysIter.hasNext()) {
//            String key = (String) keysIter.next();
//            if (!key.equals("DATA_TYPE"))
//                System.out.println("  " + key + ": " + r.document.getString(key));
//        }
//    }
//
//    public void displayTaskList(long maxAge) throws IOException, JSONException, GeneralSecurityException, APIError {
//        Map<String, String> params = new HashMap<String, String>();
//        if (maxAge > 0) {
//            long minTime = System.currentTimeMillis() - 1000 * maxAge;
//            params.put("filter", "request_time:" + isoDateFormat.format(new Date(minTime)) + ",");
//        }
//        JSONTransferAPIClient.Result r = client.getResult("/task_list", params);
//
//        int length = r.document.getInt("length");
//        if (length == 0) {
//            System.out.println("No tasks were submitted in the last " + maxAge + " seconds");
//            return;
//        }
//        JSONArray tasksArray = r.document.getJSONArray("DATA");
//        for (int i = 0; i < tasksArray.length(); i++) {
//            JSONObject taskObject = tasksArray.getJSONObject(i);
//            System.out.println("Task " + taskObject.getString("task_id") + ":");
//            displayTask(taskObject);
//        }
//    }
//
//    private static void displayTask(JSONObject taskObject) throws JSONException {
//        Iterator keysIter = taskObject.sortedKeys();
//        while (keysIter.hasNext()) {
//            String key = (String) keysIter.next();
//            if (!key.equals("DATA_TYPE") && !key.equals("LINKS") && !key.endsWith("_link")) {
//                System.out.println("  " + key + ": " + taskObject.getString(key));
//            }
//        }
//    }
//
//    public boolean autoActivate(String endpointName) throws IOException, JSONException, GeneralSecurityException,
//            APIError {
//        // Note: in a later release, auto-activation will be done at
//        // /autoactivate instead.
//        String resource = BaseTransferAPIClient.endpointPath(endpointName) + "/autoactivate";
//        JSONTransferAPIClient.Result r = client.postResult(resource, null, null);
//        String code = r.document.getString("code");
//        if (code.startsWith("AutoActivationFailed")) {
//            return false;
//        }
//        return true;
//    }
//
//    public void displayLs(String endpointName, String path) throws IOException, JSONException,
//            GeneralSecurityException, APIError {
//        Map<String, String> params = new HashMap<String, String>();
//        if (path != null) {
//            params.put("path", path);
//        }
//        String resource = BaseTransferAPIClient.endpointPath(endpointName) + "/ls";
//        JSONTransferAPIClient.Result r = client.getResult(resource, params);
//        System.out.println("Contents of " + path + " on " + endpointName + ":");
//
//        JSONArray fileArray = r.document.getJSONArray("DATA");
//        for (int i = 0; i < fileArray.length(); i++) {
//            JSONObject fileObject = fileArray.getJSONObject(i);
//            System.out.println("  " + fileObject.getString("name"));
//            Iterator keysIter = fileObject.sortedKeys();
//            while (keysIter.hasNext()) {
//                String key = (String) keysIter.next();
//                if (!key.equals("DATA_TYPE") && !key.equals("LINKS") && !key.endsWith("_link") && !key.equals("name")) {
//                    System.out.println("    " + key + ": " + fileObject.getString(key));
//                }
//            }
//        }
//
//    }
//
//    public boolean waitForTask(String taskId, int timeout) throws IOException, JSONException, GeneralSecurityException,
//            APIError {
//        String status = "ACTIVE";
//        JSONTransferAPIClient.Result r;
//
//        String resource = "/task/" + taskId;
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("fields", "status");
//
//        while (timeout > 0 && status.equals("ACTIVE")) {
//            r = client.getResult(resource, params);
//            status = r.document.getString("status");
//            try {
//                Thread.sleep(10000);
//            } catch (InterruptedException e) {
//                return false;
//            }
//            timeout -= 10;
//        }
//
//        if (status.equals("ACTIVE"))
//            return false;
//        return true;
//    }
//}
