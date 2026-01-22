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
package org.apache.airavata.restapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Unit test to verify all REST API controllers have the expected endpoint mappings.
 * This test uses reflection to verify controller classes have the correct annotations
 * and endpoint paths without requiring a full Spring context.
 */
public class RestEndpointRegistrationTest {

    /**
     * Map of controller class names to their expected base paths.
     */
    private static final Map<String, String> EXPECTED_CONTROLLERS = new HashMap<>() {{
        put("org.apache.airavata.restapi.controller.ApplicationDeploymentController", "/api/v1/application-deployments");
        put("org.apache.airavata.restapi.controller.ApplicationInterfaceController", "/api/v1/application-interfaces");
        put("org.apache.airavata.restapi.controller.AuthController", "/api/v1/auth");
        put("org.apache.airavata.restapi.controller.ComputeResourceController", "/api/v1/compute-resources");
        put("org.apache.airavata.restapi.controller.ConnectivityTestController", "/api/v1/connectivity-test");
        put("org.apache.airavata.restapi.controller.CredentialController", "/api/v1");
        put("org.apache.airavata.restapi.controller.DataProductController", "/api/v1/data-products");
        put("org.apache.airavata.restapi.controller.ExperimentController", "/api/v1/experiments");
        put("org.apache.airavata.restapi.controller.GatewayController", "/api/v1/gateways");
        put("org.apache.airavata.restapi.controller.GroupController", "/api/v1/groups");
        put("org.apache.airavata.restapi.controller.GroupResourceProfileController", "/api/v1/group-resource-profiles");
        put("org.apache.airavata.restapi.controller.JobController", "/api/v1/jobs");
        put("org.apache.airavata.restapi.controller.ProcessController", "/api/v1/processes");
        put("org.apache.airavata.restapi.controller.ProjectController", "/api/v1/projects");
        put("org.apache.airavata.restapi.controller.SSHKeyController", "/api/v1/ssh-keygen");
        put("org.apache.airavata.restapi.controller.StorageResourceController", "/api/v1/storage-resources");
        put("org.apache.airavata.restapi.controller.UserController", "/api/v1/users");
        put("org.apache.airavata.restapi.controller.UserResourceProfileController", "/api/v1/user-resource-profiles");
        put("org.apache.airavata.restapi.controller.WorkflowController", "/api/v1/workflows");
    }};

    /**
     * Expected minimum number of endpoints per controller.
     * This ensures each controller has meaningful functionality.
     */
    private static final Map<String, Integer> MINIMUM_ENDPOINTS_PER_CONTROLLER = new HashMap<>() {{
        put("ApplicationDeploymentController", 3);
        put("ApplicationInterfaceController", 5);
        put("AuthController", 2);
        put("ComputeResourceController", 4);
        put("ConnectivityTestController", 3);
        put("CredentialController", 5);
        put("DataProductController", 5);
        put("ExperimentController", 4);
        put("GatewayController", 4);
        put("GroupController", 5);
        put("GroupResourceProfileController", 5);
        put("JobController", 3);
        put("ProcessController", 5);
        put("ProjectController", 4);
        put("SSHKeyController", 1);
        put("StorageResourceController", 4);
        put("UserController", 5);
        put("UserResourceProfileController", 5);
        put("WorkflowController", 5);
    }};

    @Test
    public void shouldLoadAllExpectedControllerClasses() {
        List<String> missingClasses = new ArrayList<>();
        List<String> foundClasses = new ArrayList<>();

        for (String className : EXPECTED_CONTROLLERS.keySet()) {
            try {
                Class<?> clazz = Class.forName(className);
                foundClasses.add(clazz.getSimpleName());
            } catch (ClassNotFoundException e) {
                missingClasses.add(className);
            }
        }

        System.out.println("=== Controller Class Loading Test ===");
        System.out.println("Expected controllers: " + EXPECTED_CONTROLLERS.size());
        System.out.println("Found controllers: " + foundClasses.size());
        if (!missingClasses.isEmpty()) {
            System.out.println("Missing controllers: " + missingClasses);
        }

        assertThat(missingClasses)
            .as("All expected REST controller classes should be loadable")
            .isEmpty();
    }

    @Test
    public void shouldHaveRestControllerAnnotation() {
        List<String> missingAnnotations = new ArrayList<>();

        for (String className : EXPECTED_CONTROLLERS.keySet()) {
            try {
                Class<?> clazz = Class.forName(className);
                if (!clazz.isAnnotationPresent(RestController.class)) {
                    missingAnnotations.add(clazz.getSimpleName() + " missing @RestController");
                }
            } catch (ClassNotFoundException e) {
                // Handled in other test
            }
        }

        System.out.println("\n=== @RestController Annotation Test ===");
        if (!missingAnnotations.isEmpty()) {
            System.out.println("Controllers without @RestController: " + missingAnnotations);
        } else {
            System.out.println("All controllers have @RestController annotation");
        }

        assertThat(missingAnnotations)
            .as("All controllers should have @RestController annotation")
            .isEmpty();
    }

    @Test
    public void shouldHaveCorrectRequestMappingPaths() {
        List<String> wrongPaths = new ArrayList<>();

        for (Map.Entry<String, String> entry : EXPECTED_CONTROLLERS.entrySet()) {
            String className = entry.getKey();
            String expectedBasePath = entry.getValue();

            try {
                Class<?> clazz = Class.forName(className);
                RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
                
                if (mapping != null) {
                    String[] paths = mapping.value();
                    if (paths.length == 0) {
                        paths = mapping.path();
                    }
                    
                    boolean pathFound = false;
                    for (String path : paths) {
                        if (path.equals(expectedBasePath)) {
                            pathFound = true;
                            break;
                        }
                    }
                    
                    if (!pathFound && paths.length > 0) {
                        wrongPaths.add(clazz.getSimpleName() + 
                            " expected: " + expectedBasePath + 
                            " actual: " + Arrays.toString(paths));
                    }
                } else if (!expectedBasePath.isEmpty()) {
                    // Some controllers may not have class-level @RequestMapping
                    // This is OK for controllers like CredentialController
                }
            } catch (ClassNotFoundException e) {
                // Handled in other test
            }
        }

        System.out.println("\n=== RequestMapping Path Test ===");
        if (!wrongPaths.isEmpty()) {
            System.out.println("Controllers with wrong paths: " + wrongPaths);
        } else {
            System.out.println("All controllers have correct base paths");
        }

        assertThat(wrongPaths)
            .as("All controllers should have correct @RequestMapping paths")
            .isEmpty();
    }

    @Test
    public void shouldHaveMinimumEndpointsPerController() {
        List<String> insufficientEndpoints = new ArrayList<>();

        for (String className : EXPECTED_CONTROLLERS.keySet()) {
            try {
                Class<?> clazz = Class.forName(className);
                String simpleClassName = clazz.getSimpleName();
                int endpointCount = countEndpoints(clazz);
                
                Integer minimumExpected = MINIMUM_ENDPOINTS_PER_CONTROLLER.get(simpleClassName);
                if (minimumExpected != null && endpointCount < minimumExpected) {
                    insufficientEndpoints.add(simpleClassName + 
                        " has " + endpointCount + " endpoints, expected at least " + minimumExpected);
                }
            } catch (ClassNotFoundException e) {
                // Handled in other test
            }
        }

        System.out.println("\n=== Minimum Endpoints Test ===");
        if (!insufficientEndpoints.isEmpty()) {
            System.out.println("Controllers with insufficient endpoints: " + insufficientEndpoints);
        } else {
            System.out.println("All controllers have minimum required endpoints");
        }

        assertThat(insufficientEndpoints)
            .as("All controllers should have minimum number of endpoints")
            .isEmpty();
    }

    @Test
    public void shouldHaveCrudOperationsForResourceControllers() {
        // Controllers that should have full CRUD operations
        List<String> crudControllers = Arrays.asList(
            "ComputeResourceController",
            "StorageResourceController",
            "GatewayController",
            "ProjectController",
            "ExperimentController",
            "GroupController",
            "GroupResourceProfileController",
            "WorkflowController"
        );

        List<String> missingCrud = new ArrayList<>();

        for (String controllerName : crudControllers) {
            String className = "org.apache.airavata.restapi.controller." + controllerName;
            try {
                Class<?> clazz = Class.forName(className);
                
                boolean hasGet = hasAnnotation(clazz, GetMapping.class);
                boolean hasPost = hasAnnotation(clazz, PostMapping.class);
                boolean hasPut = hasAnnotation(clazz, PutMapping.class);
                boolean hasDelete = hasAnnotation(clazz, DeleteMapping.class);

                if (!hasGet) missingCrud.add(controllerName + " missing GET");
                if (!hasPost) missingCrud.add(controllerName + " missing POST");
                if (!hasPut) missingCrud.add(controllerName + " missing PUT");
                if (!hasDelete) missingCrud.add(controllerName + " missing DELETE");

            } catch (ClassNotFoundException e) {
                missingCrud.add(controllerName + " class not found");
            }
        }

        System.out.println("\n=== CRUD Operations Test ===");
        if (!missingCrud.isEmpty()) {
            System.out.println("Missing CRUD operations: " + missingCrud);
        } else {
            System.out.println("All resource controllers have full CRUD operations");
        }

        assertThat(missingCrud)
            .as("Resource controllers should have all CRUD operations (GET, POST, PUT, DELETE)")
            .isEmpty();
    }

    @Test
    public void printEndpointSummary() {
        System.out.println("\n=== Complete REST API Endpoint Summary ===\n");
        
        int totalEndpoints = 0;
        
        for (String className : EXPECTED_CONTROLLERS.keySet()) {
            try {
                Class<?> clazz = Class.forName(className);
                String basePath = EXPECTED_CONTROLLERS.get(className);
                
                System.out.println(clazz.getSimpleName() + " [" + basePath + "]:");
                
                for (Method method : clazz.getDeclaredMethods()) {
                    String endpoint = getEndpointInfo(method, basePath);
                    if (endpoint != null) {
                        System.out.println("  " + endpoint);
                        totalEndpoints++;
                    }
                }
                System.out.println();
                
            } catch (ClassNotFoundException e) {
                System.out.println("ERROR: " + className + " not found");
            }
        }
        
        System.out.println("Total controllers: " + EXPECTED_CONTROLLERS.size());
        System.out.println("Total endpoints: " + totalEndpoints);
        
        assertThat(totalEndpoints)
            .as("Should have reasonable number of endpoints")
            .isGreaterThan(50);
    }

    private int countEndpoints(Class<?> clazz) {
        int count = 0;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GetMapping.class) ||
                method.isAnnotationPresent(PostMapping.class) ||
                method.isAnnotationPresent(PutMapping.class) ||
                method.isAnnotationPresent(DeleteMapping.class)) {
                count++;
            }
        }
        return count;
    }

    private boolean hasAnnotation(Class<?> clazz, Class<? extends java.lang.annotation.Annotation> annotation) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                return true;
            }
        }
        return false;
    }

    private String getEndpointInfo(Method method, String basePath) {
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        if (getMapping != null) {
            String path = getMapping.value().length > 0 ? getMapping.value()[0] : 
                         (getMapping.path().length > 0 ? getMapping.path()[0] : "");
            return "GET " + basePath + path + " -> " + method.getName() + "()";
        }

        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        if (postMapping != null) {
            String path = postMapping.value().length > 0 ? postMapping.value()[0] :
                         (postMapping.path().length > 0 ? postMapping.path()[0] : "");
            return "POST " + basePath + path + " -> " + method.getName() + "()";
        }

        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        if (putMapping != null) {
            String path = putMapping.value().length > 0 ? putMapping.value()[0] :
                         (putMapping.path().length > 0 ? putMapping.path()[0] : "");
            return "PUT " + basePath + path + " -> " + method.getName() + "()";
        }

        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        if (deleteMapping != null) {
            String path = deleteMapping.value().length > 0 ? deleteMapping.value()[0] :
                         (deleteMapping.path().length > 0 ? deleteMapping.path()[0] : "");
            return "DELETE " + basePath + path + " -> " + method.getName() + "()";
        }

        return null;
    }
}
