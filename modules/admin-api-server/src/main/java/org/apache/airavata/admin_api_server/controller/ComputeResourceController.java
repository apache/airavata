package org.apache.airavata.admin_api_server.controller;

import org.apache.airavata.admin_api_server.entity.ComputeResource;
import org.apache.airavata.admin_api_server.repository.ComputeResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/compute-resources")
public class ComputeResourceController {

    @Autowired
    private ComputeResourceRepository computeResourceRepository;

    @GetMapping
    public ResponseEntity<List<ComputeResource>> getAllComputeResources() {
        List<ComputeResource> computeResources = computeResourceRepository.findAll();
        return ResponseEntity.ok(computeResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComputeResource> getComputeResourceById(@PathVariable Long id) {
        Optional<ComputeResource> computeResource = computeResourceRepository.findById(id);
        return computeResource.map(ResponseEntity::ok)
                             .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ComputeResource> createComputeResource(@RequestBody ComputeResource computeResource) {
        ComputeResource savedComputeResource = computeResourceRepository.save(computeResource);
        return ResponseEntity.ok(savedComputeResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComputeResource> updateComputeResource(@PathVariable Long id, @RequestBody ComputeResource computeResourceDetails) {
        Optional<ComputeResource> optionalComputeResource = computeResourceRepository.findById(id);
        if (optionalComputeResource.isPresent()) {
            ComputeResource computeResource = optionalComputeResource.get();
            computeResource.setName(computeResourceDetails.getName());
            computeResource.setCompute(computeResourceDetails.getCompute());
            computeResource.setComputeType(computeResourceDetails.getComputeType());
            computeResource.setStatus(computeResourceDetails.getStatus());
            computeResource.setDescription(computeResourceDetails.getDescription());
            computeResource.setSchedulerType(computeResourceDetails.getSchedulerType());
            computeResource.setDataMovementProtocol(computeResourceDetails.getDataMovementProtocol());
            computeResource.setQueues(computeResourceDetails.getQueues());
            ComputeResource updatedComputeResource = computeResourceRepository.save(computeResource);
            return ResponseEntity.ok(updatedComputeResource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComputeResource(@PathVariable Long id) {
        if (computeResourceRepository.existsById(id)) {
            computeResourceRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ComputeResource>> searchComputeResources(@RequestParam String keyword) {
        try {
            System.out.println("Searching compute resources with keyword: " + keyword);
            List<ComputeResource> computeResources = computeResourceRepository.findByKeyword(keyword);
            System.out.println("Found " + computeResources.size() + " compute resources");
            return ResponseEntity.ok(computeResources);
        } catch (Exception e) {
            System.err.println("Error searching compute resources: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}