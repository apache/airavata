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
package org.apache.airavata.file.manager.core.remote.client.http;

import org.apache.airavata.file.manager.core.remote.client.RemoteStorageClient;
import org.apache.airavata.model.file.transfer.LSEntryModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

public class HTTPStorageClient implements RemoteStorageClient {
    private final static Logger logger = LoggerFactory.getLogger(HTTPStorageClient.class);

    public static enum Protocol {
        HTTP, HTTPS
    }

    private String host;
    private int port;
    private Protocol protocol;

    public HTTPStorageClient(Protocol protocol, String host, int port) throws KeyManagementException, NoSuchAlgorithmException {
        this.protocol = protocol;
        this.host = host;
        this.port = port;

        // Create a new trust manager that trust all certificates
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Activate the new trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    /**
     * Reads a remote file, write it to local temporary directory and returns a file pointer to it
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    @Override
    public File readFile(String filePath) throws Exception {
        String url = "";
        if (protocol == Protocol.HTTP)
            url += "http://";
        else
            url += "https://";
        url += host + ":" + port;
        if (!filePath.startsWith("/"))
            filePath += "/" + filePath;
        url += filePath;

        URL fileUrl = new URL(url);
        URLConnection urlConnection = fileUrl.openConnection();
        ReadableByteChannel rbc = Channels.newChannel(urlConnection.getInputStream());
        String localFilePath = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString();
        FileOutputStream fos = new FileOutputStream(localFilePath);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

        return new File(localFilePath);
    }

    /**
     * Writes the source file in the local storage to specified path in the remote storage
     *
     * @param sourceFile
     * @param filePath
     * @return
     * @throws Exception
     */
    @Override
    public void writeFile(File sourceFile, String filePath) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a directory listing of the specified directory
     *
     * @param directoryPath
     * @return
     * @throws Exception
     */
    @Override
    public List<LSEntryModel> getDirectoryListing(String directoryPath) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Move the specified file from source to destination within the same storage resource
     *
     * @param currentPath
     * @param newPath
     * @throws Exception
     */
    @Override
    public void moveFile(String currentPath, String newPath) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * @param sourcePath
     * @param destinationPath
     * @throws Exception
     */
    @Override
    public void copyFile(String sourcePath, String destinationPath) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Rename file with the given name
     *
     * @param filePath
     * @param newFileName
     * @throws Exception
     */
    @Override
    public void renameFile(String filePath, String newFileName) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Delete the specified file
     *
     * @param filePath
     * @throws Exception
     */
    @Override
    public void deleteFile(String filePath) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Create new directory in the specified file
     *
     * @param newDirPath
     * @throws Exception
     */
    @Override
    public void mkdir(String newDirPath) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks whether specified file exists in the remote storage system
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    @Override
    public boolean checkFileExists(String filePath) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks whether the given path is a directory
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    @Override
    public boolean checkIsDirectory(String filePath) throws Exception {
        return false;
    }
}