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

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.execution.service.ProcessService;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.core.util.DBConstants;
import org.apache.airavata.restapi.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/processes")
@Tag(name = "Processes")
public class ProcessController {
    private final ProcessService processService;

    public ProcessController(ProcessService processService) {
        this.processService = processService;
    }

    @GetMapping("/{processId}")
    public ProcessModel getProcess(@PathVariable String processId) throws RegistryException {
        var process = processService.getProcess(processId);
        if (process == null) {
            throw new ResourceNotFoundException("Process", processId);
        }
        return process;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createProcess(
            @RequestParam String experimentId, @RequestBody ProcessModel process) throws RegistryException {
        var processId = processService.addProcess(process, experimentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("processId", processId));
    }

    @PutMapping("/{processId}")
    public ResponseEntity<Void> updateProcess(@PathVariable String processId, @RequestBody ProcessModel process)
            throws RegistryException {
        process.setProcessId(processId);
        processService.updateProcess(process, processId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<ProcessModel> getProcesses(@RequestParam String experimentId) throws RegistryException {
        return processService.getProcessList(DBConstants.Process.EXPERIMENT_ID, experimentId);
    }
}
