package org.apache.airavata.k8s.api.server.controller;

import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.server.resources.process.ProcessResource;
import org.apache.airavata.k8s.api.server.service.ProcessService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@RestController
@RequestMapping(path="/process")
public class ProcessController {

    @Resource
    private ProcessService processService;

    @PostMapping( path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public long createProcess(@RequestBody ProcessResource resource) {
        return processService.create(resource);
    }

    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProcessResource findProcessById(@PathVariable("id") long id) {
        return this.processService.findById(id)
                .orElseThrow(() -> new ServerRuntimeException("Process with id " + id + " not found"));
    }
}
