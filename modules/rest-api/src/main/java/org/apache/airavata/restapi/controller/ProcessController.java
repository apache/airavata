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

import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.utils.DBConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class ProcessController {
    private final ProcessService processService;

    public ProcessController(ProcessService processService) {
        this.processService = processService;
    }

    @GetMapping("/{processId}")
    public ResponseEntity<?> getProcess(@PathVariable String processId) {
        try {
            ProcessModel process = processService.getProcess(processId);
            if (process == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(process);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createProcess(@RequestParam String experimentId, @RequestBody ProcessModel process) {
        try {
            String processId = processService.addProcess(process, experimentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("processId", processId));
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{processId}")
    public ResponseEntity<?> updateProcess(@PathVariable String processId, @RequestBody ProcessModel process) {
        try {
            process.setProcessId(processId);
            processService.updateProcess(process, processId);
            return ResponseEntity.ok().build();
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getProcesses(@RequestParam String experimentId) {
        try {
            List<ProcessModel> processes =
                    processService.getProcessList(DBConstants.Process.EXPERIMENT_ID, experimentId);
            return ResponseEntity.ok(processes);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{processId}/resource-schedule")
    public ResponseEntity<?> getProcessResourceSchedule(@PathVariable String processId) {
        try {
            ComputationalResourceSchedulingModel schedule = processService.getProcessResourceSchedule(processId);
            return ResponseEntity.ok(schedule);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{processId}/resource-schedule")
    public ResponseEntity<?> createProcessResourceSchedule(
            @PathVariable String processId, @RequestBody ComputationalResourceSchedulingModel schedule) {
        try {
            String result = processService.addProcessResourceSchedule(schedule, processId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("processId", result));
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{processId}/resource-schedule")
    public ResponseEntity<?> updateProcessResourceSchedule(
            @PathVariable String processId, @RequestBody ComputationalResourceSchedulingModel schedule) {
        try {
            String result = processService.updateProcessResourceSchedule(schedule, processId);
            return ResponseEntity.ok(Map.of("processId", result));
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
