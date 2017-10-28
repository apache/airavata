package org.apache.airavata.k8s.api.server.controller;

import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.resources.application.ApplicationDeploymentResource;
import org.apache.airavata.k8s.api.server.service.ApplicationDeploymentService;
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
@RequestMapping(path="/appdep")
public class ApplicationDeploymentController {

    @Resource
    private ApplicationDeploymentService applicationDeploymentService;

    @PostMapping( path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Long createApplicationModule(@RequestBody ApplicationDeploymentResource resource) {
        return applicationDeploymentService.create(resource);
    }

    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApplicationDeploymentResource findAppModuleById(@PathVariable("id") long id) {
        return this.applicationDeploymentService.findById(id)
                .orElseThrow(() -> new ServerRuntimeException("App deployment with id " + id + " can not be found"));
    }
}
