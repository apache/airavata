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
package org.apache.airavata.file.manager.core.remote.client.gridftp.myproxy;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.apache.airavata.file.manager.core.remote.client.gridftp.GridFTPConstants;
import org.apache.log4j.Logger;
import org.globus.myproxy.MyProxy;
import org.ietf.jgss.GSSCredential;

public class SecurityContext {

    /**
     *
     */
    public static final String GRIDFTP_CLIENT_PROPERTY = "gridftp-client.properties";
    private Properties properties;
    protected GSSCredential gssCredential;

    private MyProxyCredentials myProxyCredentials;
    private static final Logger log = Logger.getLogger(SecurityContext.class);

    private String userName = null;
    private String password = null;

    /**
     *
     * Constructs a ApplicationGlobalContext.
     *
     * @throws Exception
     */

    public SecurityContext() throws Exception {
        log.setLevel(org.apache.log4j.Level.INFO);
        loadConfiguration();

    }

    public SecurityContext(String user, String pwd) throws Exception {

        this.userName = user;
        this.password = pwd;

        log.setLevel(org.apache.log4j.Level.INFO);
        loadConfiguration();

    }

    /**
     *
     * @throws Exception
     */
    public void login() throws Exception {
        gssCredential = myProxyCredentials.getDefaultCredentials();
    }

    public GSSCredential getProxyCredentials(GSSCredential credential) throws Exception {
        return myProxyCredentials.getProxyCredentials(credential);
    }

    public GSSCredential renewCredentials(GSSCredential credential) throws Exception {
        return myProxyCredentials.renewCredentials(credential);
    }

    public static String getProperty(String name) {
        try {
            SecurityContext context = new SecurityContext();
            return context.getProperties().getProperty(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Load the configration file
     *
     * @throws Exception
     */
    private void loadConfiguration() throws Exception {
        try {

            System.out.println("In the load configurations method .....");

            if (properties == null) {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

                InputStream propertyStream = classLoader.getResourceAsStream(GRIDFTP_CLIENT_PROPERTY);
                properties = new Properties();
                if (myProxyCredentials == null) {
                    this.myProxyCredentials = new MyProxyCredentials();
                }
                if (propertyStream != null) {
                    properties.load(propertyStream);
                    String myproxyServerTmp = properties.getProperty(GridFTPConstants.MYPROXY_SERVER);
                    if (myproxyServerTmp != null) {
                        this.myProxyCredentials.setMyProxyHostname(myproxyServerTmp.trim());
                    }
                    String myproxyPortTemp = properties.getProperty(GridFTPConstants.MYPROXY_PORT);
                    if (myproxyPortTemp != null && myproxyPortTemp.trim().length() > 0) {
                        this.myProxyCredentials.setMyProxyPortNumber(Integer.parseInt(myproxyPortTemp.trim()));
                    } else {
                        this.myProxyCredentials.setMyProxyPortNumber(MyProxy.DEFAULT_PORT);
                    }

                    this.myProxyCredentials.setMyProxyUserName(userName);
                    this.myProxyCredentials.setMyProxyPassword(password);

                    String myproxytime = properties.getProperty(GridFTPConstants.MYPROXY_LIFETIME);
                    if (myproxytime != null) {
                        this.myProxyCredentials.setMyProxyLifeTime(Integer.parseInt(myproxytime));
                    }

                    String currentDirectory = System.getProperty("projectDirectory");
                    String certificatePath = currentDirectory + File.separatorChar
                            + properties.getProperty(GridFTPConstants.TRUSTED_CERTS_FILE);

                    this.myProxyCredentials.setTrustedCertificatePath(certificatePath);

                    System.out.println("Certificate path - " + certificatePath);

                    this.myProxyCredentials.init();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            throw new Exception(e);
        }

    }

    /**
     * @return the properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * @param properties
     *            the properties to set
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Returns the raw gssCredential, without creating a proxy.
     *
     * @return The gssCredential
     */
    public GSSCredential getRawCredential() throws Exception{

        return gssCredential;
    }

    /**
     * Returns the gssCredential.
     *
     * @return The gssCredential
     */
    public GSSCredential getGssCredential() throws Exception{

        if (this.gssCredential == null)
            return null;

        return renewCredentials(gssCredential);
    }

    /**
     * Sets gssCredential.
     *
     * @param gssCredential
     *            The gssCredential to set.
     */
    public void setGssCredential(GSSCredential gssCredential) {
        this.gssCredential = gssCredential;
    }

    public MyProxyCredentials getMyProxyCredentials() {
        return myProxyCredentials;
    }

    public void setMyProxyCredentials(MyProxyCredentials myProxyCredentials) {
        this.myProxyCredentials = myProxyCredentials;
    }
}