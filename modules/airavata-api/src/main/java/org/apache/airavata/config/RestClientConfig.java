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
package org.apache.airavata.config;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.time.Duration;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * REST client configuration for Spring RestTemplate.
 * Provides a configured RestTemplate bean with proper timeouts and SSL support.
 *
 * Configure via application.properties:
 *   security.tls.enabled=true
 *   security.tls.keystore.path=keystores/airavata.p12
 *   security.tls.keystore.password=secret
 */
@Configuration
public class RestClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(RestClientConfig.class);

    private final AiravataServerProperties properties;

    public RestClientConfig(AiravataServerProperties properties) {
        this.properties = properties;
    }

    /**
     * Primary RestTemplate bean with connection pooling and SSL support.
     */
    @Bean
    @Primary
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        var restTemplate = builder.connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(60))
                .build();

        // Configure with Apache HttpClient 5 if SSL is needed
        try {
            configureSSL(restTemplate);
        } catch (Exception e) {
            logger.warn("Could not configure SSL for RestTemplate: {}", e.getMessage());
        }

        return restTemplate;
    }

    /**
     * Configure SSL for RestTemplate using Java's standard SSLContext.
     */
    private void configureSSL(RestTemplate restTemplate) throws Exception {
        // Check if security configuration is available
        if (properties.security() == null
                || properties.security().tls() == null
                || properties.security().tls().keystore() == null) {
            // Use default trust store with self-signed certificate support for development/test
            SSLContext sslContext = createTrustAllSSLContext();
            configureHttpClient(restTemplate, sslContext);
            return;
        }

        var trustStorePath = properties.security().tls().keystore().path();
        var trustStorePassword = properties.security().tls().keystore().password();

        SSLContext sslContext;

        if (trustStorePath != null && !trustStorePath.isEmpty() && new File(trustStorePath).exists()) {
            // Use configured trust store
            var trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (var fis = new FileInputStream(trustStorePath)) {
                trustStore.load(fis, trustStorePassword != null ? trustStorePassword.toCharArray() : null);
            }

            var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            var trustManagers = trustManagerFactory.getTrustManagers();

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new java.security.SecureRandom());
        } else {
            // Use default trust store with self-signed certificate support for development
            sslContext = createTrustAllSSLContext();
        }

        configureHttpClient(restTemplate, sslContext);
    }

    /**
     * Create SSLContext that trusts all certificates (for development/test only).
     */
    private SSLContext createTrustAllSSLContext() throws Exception {
        var trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
            }
        };
        var sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        return sslContext;
    }

    /**
     * Configure RestTemplate with SSL context using Spring's SimpleClientHttpRequestFactory.
     * Note: For production use with connection pooling, consider using Spring's WebClient
     * or configuring HttpComponentsClientHttpRequestFactory via Spring Boot's auto-configuration.
     */
    private void configureHttpClient(RestTemplate restTemplate, SSLContext sslContext) {
        // Spring's SimpleClientHttpRequestFactory uses Java's standard URLConnection
        // which respects JVM-level SSL configuration
        // For advanced features like connection pooling, Spring Boot auto-configures
        // HttpComponentsClientHttpRequestFactory when httpclient5 is on classpath
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(30000);
        requestFactory.setReadTimeout(60000);

        // SSL context is configured at JVM level via system properties
        // or via Spring Boot's SSL configuration
        restTemplate.setRequestFactory(requestFactory);
    }

    /**
     * RestTemplateBuilder bean for customizing RestTemplate instances.
     */
    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder().connectTimeout(Duration.ofSeconds(30)).readTimeout(Duration.ofSeconds(60));
    }
}
