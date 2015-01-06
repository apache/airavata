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
package org.apache.airavata.gfac.monitor.util;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultSaslConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.util.Collections;
import java.util.List;

public class AMQPConnectionUtil {
    private final static Logger logger = LoggerFactory.getLogger(AMQPConnectionUtil.class);
    public static Connection connect(List<String>hosts,String vhost, String proxyFile) {
        Collections.shuffle(hosts);
        for (String host : hosts) {
            Connection connection = connect(host, vhost, proxyFile);
            if (host != null) {
                System.out.println("connected to " + host);
                return connection;
            }
        }
        return null;
    }

    public static Connection connect(String host, String vhost, String proxyFile) {
        Connection connection;
        try {
            String keyPassPhrase = "test123";
            KeyStore ks = X509Helper.keyStoreFromPEM(proxyFile, keyPassPhrase);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, keyPassPhrase.toCharArray());

            KeyStore tks = X509Helper.trustKeyStoreFromCertDir();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(tks);

            SSLContext c = SSLContext.getInstance("SSLv3");
            c.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setPort(5671);
            factory.useSslProtocol(c);
            factory.setVirtualHost(vhost);
            factory.setSaslConfig(DefaultSaslConfig.EXTERNAL);

            connection = factory.newConnection();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
        return connection;
    }

}
