package org.apache.airavata.k8s.api.server.controller;

import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.resources.compute.ComputeResource;
import org.apache.airavata.k8s.api.server.service.ComputeResourceService;
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
@RequestMapping(path="/compute")
public class ComputeResourceController {

    @Resource
    private ComputeResourceService computeResourceService;

    @PostMapping( path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Long createComputeResource(@RequestBody ComputeResource resource) {
        return computeResourceService.create(resource);
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ComputeResource> getAllComputeResources() {
        return this.computeResourceService.getAll();
    }

    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ComputeResource findComputeResourceById(@PathVariable("id") long id) {
        return this.computeResourceService.findById(id)
                .orElseThrow(() -> new ServerRuntimeException("Compute resource with id " + id + " cab not be found"));
    }
}
