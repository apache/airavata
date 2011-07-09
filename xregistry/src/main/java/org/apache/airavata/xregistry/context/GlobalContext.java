/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.airavata.xregistry.context;

import static org.apache.airavata.xregistry.XregistryOptions.DBURL;
import static org.apache.airavata.xregistry.XregistryOptions.MYPROXY_LIFETIME;
import static org.apache.airavata.xregistry.XregistryOptions.MYPROXY_PASSWD;
import static org.apache.airavata.xregistry.XregistryOptions.MYPROXY_SERVER;
import static org.apache.airavata.xregistry.XregistryOptions.MYPROXY_USERNAME;
import static org.apache.airavata.xregistry.XregistryOptions.SECURITY_ENABLED;
import static org.apache.airavata.xregistry.XregistryOptions.SSL_HOST_KEY_FILE;
import static org.apache.airavata.xregistry.XregistryOptions.SSL_TRUSTED_CERT_FILE;
import static org.apache.airavata.xregistry.utils.Utils.createCredentials;
import static org.apache.airavata.xregistry.utils.Utils.findBooleanProperty;
import static org.apache.airavata.xregistry.utils.Utils.findIntegerProperty;
import static org.apache.airavata.xregistry.utils.Utils.findStringProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Timer;

import org.apache.airavata.xregistry.XregistryConstants;
import org.apache.airavata.xregistry.XregistryException;
import org.apache.airavata.xregistry.XregistryOptions;
import org.apache.airavata.xregistry.auth.Authorizer;
import org.apache.airavata.xregistry.auth.AuthorizerImpl;
import org.apache.airavata.xregistry.db.JdbcStorage;
import org.apache.airavata.xregistry.group.GroupManager;
import org.apache.airavata.xregistry.group.GroupManagerImpl;
import org.apache.airavata.xregistry.utils.CWsdlUpdateTask;
import org.apache.airavata.xregistry.utils.ProxyRenewer;
import org.apache.airavata.xregistry.utils.Utils;
import org.globus.myproxy.MyProxy;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

/**
 * This is the central configuration Store for Xregistry
 */

public class GlobalContext {
    private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

    private Properties config;

    private JdbcStorage storage;

    private GroupManager groupManager;

    private Authorizer authorizer;

    private String trustedCertsFile;

    private String hostcertsKeyFile;

    private GSSCredential credential;

    private final boolean isClientSide;

    private String dbUrl;

    private int port;
    
    private boolean securityEnabled;
    
    private String userDN;
    
    private X509Certificate[] trustedCertificates;
    
    
    public GlobalContext(boolean clientSide) throws XregistryException{
        this(clientSide,(String)null);
    }
    
    
    public GlobalContext(boolean clientSide,String propertiesFileName) throws XregistryException {
        this(clientSide,loadPropertiesFromPorpertyFile(propertiesFileName));
    }
        
    private static Properties loadPropertiesFromPorpertyFile(String propertiesFileName) throws XregistryException{    
            try {
                Properties properties = new Properties();
                InputStream propertiesIn;
                if(propertiesFileName != null){
                    //try explicit parameter
                    propertiesIn = new FileInputStream(propertiesFileName);
                }else{
                    // xregistry.properties file on current directory
                    File propertiesFile = new File(XregistryConstants.X_REGISTRY_PROPERTY_FILE);
                    if(propertiesFile.exists()){
                        propertiesIn = new FileInputStream(propertiesFile);
                    }else{
                        //3) xregistry/xregistry.properties file on classpath
                        propertiesIn = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(
                                "xregistry/" + XregistryConstants.X_REGISTRY_PROPERTY_FILE);
                    }
                }
                if(propertiesIn != null){
                    properties.load(propertiesIn);
                    propertiesIn.close();
                }else{
                    throw new XregistryException("Can not load configuration, the configuration order " +
                       "1) explict parameter 2) xregistry.properties file on current directory 3) xregistry/xregistry.properties file on classpath");
                }
                return properties;
            } catch (FileNotFoundException e) {
                throw new XregistryException();
            } catch (IOException e) {
                throw new XregistryException();
            }
       }
            
       public GlobalContext(boolean clientSide,Properties properties) throws XregistryException {  
           this.isClientSide = clientSide;
           this.config = properties;
           try{
            if(clientSide){
                String loginDataFile = System.getProperty("login.file");
                if(loginDataFile == null){
                    loginDataFile = System.getProperty("user.home")+"/.xregistry";
                }
                File loginPropertiesFile = new File(loginDataFile);
                if(loginPropertiesFile.exists()){
                    InputStream loginPropertiesIn = new FileInputStream(loginPropertiesFile); 
                    config.load(loginPropertiesIn);
                    loginPropertiesIn.close();
                }    
            }
        } catch (FileNotFoundException e) {
            throw new XregistryException();
        } catch (IOException e) {
            throw new XregistryException();
        }
                
        if (!clientSide) {
            loadConfiguration();
            storage = new JdbcStorage(dbUrl, MYSQL_DRIVER);
            //intialize databse
            initDatabaseTables();

            //start Xregistry support modules
            groupManager = new GroupManagerImpl(this);
            authorizer = new AuthorizerImpl(this, groupManager);
            
            startSheduledTasks();
        }
    }

    public GSSCredential getCredential() throws XregistryException {
        try {
            if (credential == null) {
                credential = createMyProxyCredentails();
                if (credential == null) {
                    if (isClientSide) {
                        throw new XregistryException(
                                "At the client side, myproxy credentails must present");
                    } else {
                        credential = createCredentials();
                    }
                }
                userDN = credential.getName().toString();
            }
            return credential;
        } catch (GSSException e) {
            throw new XregistryException(e);
        }
    }

    public String getHostcertsKeyFile() {
        return hostcertsKeyFile;
    }

    public X509Certificate[] getTrustedCertificates() {
        return trustedCertificates;
    }


    public void setTrustedCertificates(X509Certificate[] trustedCertificates) {
        this.trustedCertificates = trustedCertificates;
    }


    public String getTrustedCertsFile() {
        return trustedCertsFile;
    }

    public void setCredential(GSSCredential credential) throws XregistryException {
        try {
            this.credential = credential;
            userDN = credential.getName().toString();
        } catch (GSSException e) {
            throw new XregistryException(e);
        }
    }

    public void loadConfiguration() throws XregistryException {

        this.hostcertsKeyFile = findStringProperty(config, SSL_HOST_KEY_FILE, hostcertsKeyFile);
        this.trustedCertsFile = findStringProperty(config, SSL_TRUSTED_CERT_FILE, trustedCertsFile);
        this.dbUrl = findStringProperty(config, DBURL, dbUrl);
        if (dbUrl == null) {
            throw new XregistryException("Database URL for underline database is not defined");
        }
        this.port = findIntegerProperty(config, XregistryOptions.PORT, 6666);
        this.securityEnabled = findBooleanProperty(config, SECURITY_ENABLED, true);
    }

    public Connection createConnection() throws XregistryException {
        return storage.connect();
    }

    public void closeConnection(Connection connection) throws XregistryException {
        try {
            storage.closeConnection(connection);
        } catch (SQLException e) {
            throw new XregistryException(e);
        }
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

    public Authorizer getAuthorizer() {
        return authorizer;
    }

    public void setTrustedCertsFile(String trustedCertsFile) {
        this.trustedCertsFile = trustedCertsFile;
    }

    public int getPort() {
        return port;
    }

    public void setHostcertsKeyFile(String hostcertsKeyFile) {
        this.hostcertsKeyFile = hostcertsKeyFile;
    }

    private GSSCredential createMyProxyCredentails() throws XregistryException {
        String myproxyUserNameStr = config.getProperty(MYPROXY_USERNAME);
        String myproxyPasswdStr = config.getProperty(MYPROXY_PASSWD);
        String myproxyServerStr = config.getProperty(MYPROXY_SERVER);
        String myproxyLifetimeStr = config.getProperty(MYPROXY_LIFETIME);

        if (myproxyUserNameStr != null && myproxyPasswdStr != null && myproxyServerStr != null) {
            int myproxyLifetime = 14400;
            if (myproxyLifetimeStr != null) {
                myproxyLifetime = Integer.parseInt(myproxyLifetimeStr.trim());
            }
            ProxyRenewer proxyRenewer = new ProxyRenewer(myproxyUserNameStr, myproxyPasswdStr,
                    MyProxy.DEFAULT_PORT, myproxyLifetime, myproxyServerStr,trustedCertsFile);
            return proxyRenewer.renewProxy();
        } else {
            System.out.println("Can not find sufficent information to load myproxy, server = "+ myproxyServerStr+" User Name=" 
                    + myproxyUserNameStr + " passwd is "+ myproxyPasswdStr == null? "":"Not" + null);
            return null;
        }

    }
    

    /**
     * If data base tables are defined as SQL queries in file placed at 
     * xregistry/tables.sql in the classpath, those SQL queries are execuated against the 
     * data base. On the file, any line start # is igonred as a comment.
     * @throws XregistryException
     */
    private void initDatabaseTables() throws XregistryException{
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream sqltablesAsStream = cl.getResourceAsStream(
                "xregistry/" + XregistryConstants.XREGISTRY_SQL_FILE);
        
        if(sqltablesAsStream == null){
            return;
        }
        
        Connection connection = createConnection();
        try{
         Statement statement = connection.createStatement();
         
         
         String docAsStr = Utils.readFromStream(sqltablesAsStream);
         StringTokenizer t = new StringTokenizer(docAsStr,";");
         
         while(t.hasMoreTokens()){
             String line = t.nextToken();
             if(line.trim().length() > 0 && !line.startsWith("#")){
                 System.out.println(line.trim());
                 statement.executeUpdate(line.trim());
             }
         }
        } catch (SQLException e) {
            throw new XregistryException(e);
        } finally{
            closeConnection(connection);
        }
        
    }
    
    
    public void startSheduledTasks(){
        Timer timer = new Timer();
        int delay = 5000;   // delay for 5 sec.
        int period = 1000*60*10; //every 10 minute 
        timer.scheduleAtFixedRate(new CWsdlUpdateTask(this), delay, period);
    }
    

    public void setUserDN(String userDN) {
        this.userDN = userDN;
    }


    public boolean isSecurityEnabled() {
        return securityEnabled;
    }

    public String getUserDN() {
        return userDN;
    }


    public Properties getConfig() {
        return config;
    }
    
    
}
