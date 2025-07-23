package org.apache.airavata.admin_api_server.controller;

import org.apache.airavata.admin_api_server.entity.StorageResource;
import org.apache.airavata.admin_api_server.repository.StorageResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/storage-resources")
public class StorageResourceController {

    @Autowired
    private StorageResourceRepository storageResourceRepository;

    @GetMapping
    public ResponseEntity<List<StorageResource>> getAllStorageResources() {
        List<StorageResource> storageResources = storageResourceRepository.findAll();
        return ResponseEntity.ok(storageResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StorageResource> getStorageResourceById(@PathVariable Long id) {
        Optional<StorageResource> storageResource = storageResourceRepository.findById(id);
        return storageResource.map(ResponseEntity::ok)
                             .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StorageResource> createStorageResource(@RequestBody StorageResource storageResource) {
        StorageResource savedStorageResource = storageResourceRepository.save(storageResource);
        return ResponseEntity.ok(savedStorageResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StorageResource> updateStorageResource(@PathVariable Long id, @RequestBody StorageResource storageResourceDetails) {
        Optional<StorageResource> optionalStorageResource = storageResourceRepository.findById(id);
        if (optionalStorageResource.isPresent()) {
            StorageResource storageResource = optionalStorageResource.get();
            storageResource.setName(storageResourceDetails.getName());
            storageResource.setStorage(storageResourceDetails.getStorage());
            storageResource.setStorageType(storageResourceDetails.getStorageType());
            storageResource.setStatus(storageResourceDetails.getStatus());
            storageResource.setDescription(storageResourceDetails.getDescription());
            StorageResource updatedStorageResource = storageResourceRepository.save(storageResource);
            return ResponseEntity.ok(updatedStorageResource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStorageResource(@PathVariable Long id) {
        if (storageResourceRepository.existsById(id)) {
            storageResourceRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<StorageResource>> searchStorageResources(@RequestParam String keyword) {
        try {
            System.out.println("Searching storage resources with keyword: " + keyword);
            List<StorageResource> storageResources = storageResourceRepository.findByKeyword(keyword);
            System.out.println("Found " + storageResources.size() + " storage resources");
            return ResponseEntity.ok(storageResources);
        } catch (Exception e) {
            System.err.println("Error searching storage resources: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}