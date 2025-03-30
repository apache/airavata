/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.research.service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rf/hub")
public class ResearchHubController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResearchHubController.class);

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> resolveResearchHubUrl(@PathVariable("projectId") String projectId) {

        // TODO extract the data using the projectId
        String gitUrl = "https://github.com/AllenInstitute/bmtk-workshop.git";
        String dataPath = "bmtk";
        String jupyterUser = "airavata@apache.org";
        String randomSessionName = "session-" + UUID.randomUUID().toString().substring(0, 6);
        System.out.println();
        String spawnUrl = String.format(
                "https://hub.dev.cybershuttle.org/hub/spawn/%s/%s?git=%s&dataPath=%s",
                jupyterUser,
                randomSessionName,
                gitUrl,
                dataPath
        );

        LOGGER.info("Redirecting user to spawn URL: {}", spawnUrl);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(spawnUrl)).build();
    }
}

