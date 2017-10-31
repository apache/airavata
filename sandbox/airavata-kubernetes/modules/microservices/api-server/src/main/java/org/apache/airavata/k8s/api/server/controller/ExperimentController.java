/**
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
 */
package org.apache.airavata.k8s.api.server.controller;

import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.resources.experiment.ExperimentResource;
import org.apache.airavata.k8s.api.server.service.ExperimentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@RestController
@RequestMapping(path="/experiment")
public class ExperimentController {

    @Resource
    private ExperimentService experimentService;

    @PostMapping( path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public long createExperiment(@RequestBody ExperimentResource resource) {
        return experimentService.create(resource);
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ExperimentResource> getAllExperiments() {
        return this.experimentService.getAll();
    }

    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExperimentResource findExperimentById(@PathVariable("id") long id) {
        return this.experimentService.findById(id)
                .orElseThrow(() -> new ServerRuntimeException("Experiment with id " + id + " not found"));
    }

    @GetMapping(path = "{id}/launch", produces = MediaType.APPLICATION_JSON_VALUE)
    public long launchExperiment(@PathVariable("id") long id) {
        return this.experimentService.launchExperiment(id);
    }
}
