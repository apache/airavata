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
import java.security.KeyStore;
import java.time.Duration;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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
        RestTemplate restTemplate = builder.connectTimeout(Duration.ofSeconds(30))
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
     * Configure SSL for RestTemplate using Apache HttpClient 5.
     */
    private void configureSSL(RestTemplate restTemplate) throws Exception {
        // Check if security configuration is available
        if (properties.security() == null || 
            properties.security().tls() == null || 
            properties.security().tls().keystore() == null) {
            // Use default trust store with self-signed certificate support for development/test
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial((TrustStrategy) (chain, authType) -> true)
                    .build();
            configureHttpClient(restTemplate, sslContext);
            return;
        }

        String trustStorePath = properties.security().tls().keystore().path();
        String trustStorePassword = properties.security().tls().keystore().password();

        SSLContext sslContext;

        if (trustStorePath != null && !trustStorePath.isEmpty() && new File(trustStorePath).exists()) {
            // Use configured trust store
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(
                    new java.io.FileInputStream(trustStorePath),
                    trustStorePassword != null ? trustStorePassword.toCharArray() : null);

            sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(trustStore, null)
                    .build();
        } else {
            // Use default trust store with self-signed certificate support for development
            sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial((TrustStrategy) (chain, authType) -> true)
                    .build();
        }

        configureHttpClient(restTemplate, sslContext);
    }

    /**
     * Configure HttpClient with SSL context and set it as the request factory.
     */
    private void configureHttpClient(RestTemplate restTemplate, SSLContext sslContext) {

        var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                        .setSslContext(sslContext)
                        .build())
                .setMaxConnTotal(50)
                .setMaxConnPerRoute(20)
                .build();

        CloseableHttpClient httpClient =
                HttpClients.custom().setConnectionManager(connectionManager).build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout(30000);

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
