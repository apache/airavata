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
package org.apache.airavata.restapi.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.airavata.core.exception.RegistryExceptions;
import org.apache.airavata.iam.exception.AuthExceptions;
import org.apache.airavata.restapi.exception.GlobalExceptionHandler;
import org.apache.airavata.restapi.exception.InvalidRequestException;
import org.apache.airavata.restapi.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tests for {@link GlobalExceptionHandler} using MockMvc.
 *
 * <p>Uses a minimal stub controller (in the controller package so the
 * {@code @RestControllerAdvice(basePackages)} filter applies) that throws
 * specific exceptions to verify the handler produces correct HTTP status
 * codes and sanitized error bodies.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(
        classes = {
            GlobalExceptionHandlerTest.TestConfig.class,
            GlobalExceptionHandlerTest.StubController.class,
            GlobalExceptionHandler.class
        })
class GlobalExceptionHandlerTest {

    @Configuration
    static class TestConfig {}

    @Autowired
    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/test")
    static class StubController {

        @GetMapping("/registry-exception")
        public String registryException() throws RegistryExceptions.RegistryException {
            throw new RegistryExceptions.RegistryException("secret DB connection string leaked");
        }

        @GetMapping("/app-registry-exception")
        public String appRegistryException() throws RegistryExceptions.AppRegistryException {
            throw new RegistryExceptions.AppRegistryException("internal app registry detail");
        }

        @GetMapping("/experiment-registry-exception")
        public String experimentRegistryException() throws RegistryExceptions.ExperimentRegistryException {
            throw new RegistryExceptions.ExperimentRegistryException("internal experiment detail");
        }

        @GetMapping("/workflow-registry-exception")
        public String workflowRegistryException() throws RegistryExceptions.WorkflowRegistryException {
            throw new RegistryExceptions.WorkflowRegistryException("internal workflow detail");
        }

        @GetMapping("/resource-not-found")
        public String resourceNotFound() {
            throw new ResourceNotFoundException("Gateway", "gw-123");
        }

        @GetMapping("/invalid-request")
        public String invalidRequest() {
            throw new InvalidRequestException("name must not be blank");
        }

        @GetMapping("/illegal-argument")
        public String illegalArgument() {
            throw new IllegalArgumentException("bad parameter value");
        }

        @GetMapping("/authorization-exception")
        public String authorizationException() throws AuthExceptions.AuthorizationException {
            throw new AuthExceptions.AuthorizationException("not permitted");
        }

        @GetMapping("/generic-exception")
        public String genericException() throws Exception {
            throw new Exception("should never appear in response");
        }
    }

    @Test
    void registryException_returns500WithSanitizedMessage() throws Exception {
        mockMvc.perform(get("/test/registry-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Registry error"))
                .andExpect(jsonPath("$.path").value("/test/registry-exception"))
                .andExpect(jsonPath("$.timestamp").isNumber());
    }

    @Test
    void appRegistryException_returns500WithSanitizedMessage() throws Exception {
        mockMvc.perform(get("/test/app-registry-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("App registry error"));
    }

    @Test
    void experimentRegistryException_returns500WithSanitizedMessage() throws Exception {
        mockMvc.perform(get("/test/experiment-registry-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Experiment registry error"));
    }

    @Test
    void workflowRegistryException_returns500WithSanitizedMessage() throws Exception {
        mockMvc.perform(get("/test/workflow-registry-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Workflow registry error"));
    }

    @Test
    void resourceNotFound_returns404WithMessage() throws Exception {
        mockMvc.perform(get("/test/resource-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Gateway not found: gw-123"));
    }

    @Test
    void invalidRequest_returns400WithMessage() throws Exception {
        mockMvc.perform(get("/test/invalid-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("name must not be blank"));
    }

    @Test
    void illegalArgument_returns400() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("bad parameter value"));
    }

    @Test
    void authorizationException_returns403() throws Exception {
        mockMvc.perform(get("/test/authorization-exception"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("not permitted"));
    }

    @Test
    void genericException_returns500WithGenericMessage() throws Exception {
        mockMvc.perform(get("/test/generic-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }

    @Test
    void internalServerErrors_neverLeakRawExceptionMessages() throws Exception {
        mockMvc.perform(get("/test/registry-exception"))
                .andExpect(jsonPath("$.message").value("Registry error"))
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret"))));

        mockMvc.perform(get("/test/generic-exception"))
                .andExpect(jsonPath("$.message").value("Internal server error"))
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("should never appear"))));
    }

    @Test
    void allResponses_haveConsistentStructure() throws Exception {
        for (String path : new String[] {
            "/test/registry-exception",
            "/test/resource-not-found",
            "/test/invalid-request",
            "/test/illegal-argument",
            "/test/generic-exception"
        }) {
            mockMvc.perform(get(path))
                    .andExpect(jsonPath("$.status").isNumber())
                    .andExpect(jsonPath("$.error").isString())
                    .andExpect(jsonPath("$.message").isString())
                    .andExpect(jsonPath("$.timestamp").isNumber())
                    .andExpect(jsonPath("$.path").value(path));
        }
    }
}
