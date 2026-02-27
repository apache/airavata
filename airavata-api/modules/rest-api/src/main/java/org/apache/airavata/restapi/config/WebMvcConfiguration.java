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
package org.apache.airavata.restapi.config;

import org.apache.airavata.restapi.security.AuthenticationInterceptor;
import org.apache.airavata.restapi.security.ResearchContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration("restWebMvcConfiguration")
public class WebMvcConfiguration implements WebMvcConfigurer {

    private static final long CORS_MAX_AGE = 3600;

    private final AuthenticationInterceptor authenticationInterceptor;
    private final ResearchContextInterceptor researchContextInterceptor;

    public WebMvcConfiguration(
            AuthenticationInterceptor authenticationInterceptor,
            ResearchContextInterceptor researchContextInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.researchContextInterceptor = researchContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor).addPathPatterns("/api/v1/**");
        registry.addInterceptor(researchContextInterceptor)
                .addPathPatterns("/api/v1/research/**", "/api/v1/research-hub/**");
    }

    /**
     * Enable CORS for /api/v1 so the portal (different origin) can call REST endpoints.
     * Without this, browser preflight OPTIONS requests get 405 Method Not Allowed
     * and credential (and other) GET/POST/DELETE requests fail.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(CORS_MAX_AGE);
    }
}
