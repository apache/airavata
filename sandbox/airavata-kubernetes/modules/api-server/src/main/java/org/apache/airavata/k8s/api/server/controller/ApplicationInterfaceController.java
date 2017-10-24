package org.apache.airavata.k8s.api.server.controller;

import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.server.resources.application.ApplicationDeploymentResource;
import org.apache.airavata.k8s.api.server.resources.application.ApplicationIfaceResource;
import org.apache.airavata.k8s.api.server.service.ApplicationIfaceService;
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
@RequestMapping(path="/appiface")
public class ApplicationInterfaceController {

    @Resource
    private ApplicationIfaceService ifaceService;

    @PostMapping( path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Long createApplicationModule(@RequestBody ApplicationIfaceResource resource) {
        return ifaceService.create(resource);
    }

    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApplicationIfaceResource findAppModuleById(@PathVariable("id") long id) {
        return this.ifaceService.findById(id)
                .orElseThrow(() -> new ServerRuntimeException("App interface with id " + id + " can not be found"));
    }
}
