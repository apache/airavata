package org.apache.airavata.admin_api_server.controller;

import org.apache.airavata.admin_api_server.entity.UserStarredResource;
import org.apache.airavata.admin_api_server.service.StarringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StarringController {
    
    @Autowired
    private StarringService starringService;
    
    @PostMapping("/models/{id}/star")
    public ResponseEntity<Boolean> toggleStarModel(@PathVariable Long id, 
                                                  @RequestParam String userEmail,
                                                  @RequestParam(required = false, defaultValue = "User") String userName) {
        boolean isStarred = starringService.toggleStarResource(userEmail, userName, id, UserStarredResource.ResourceType.MODEL);
        return ResponseEntity.ok(isStarred);
    }
    
    @GetMapping("/models/{id}/star")
    public ResponseEntity<Boolean> isModelStarred(@PathVariable Long id, @RequestParam String userEmail) {
        boolean isStarred = starringService.isResourceStarredByUser(userEmail, id, UserStarredResource.ResourceType.MODEL);
        return ResponseEntity.ok(isStarred);
    }
    
    @PostMapping("/datasets/{id}/star")
    public ResponseEntity<Boolean> toggleStarDataset(@PathVariable Long id, 
                                                    @RequestParam String userEmail,
                                                    @RequestParam(required = false, defaultValue = "User") String userName) {
        boolean isStarred = starringService.toggleStarResource(userEmail, userName, id, UserStarredResource.ResourceType.DATASET);
        return ResponseEntity.ok(isStarred);
    }
    
    @GetMapping("/datasets/{id}/star")
    public ResponseEntity<Boolean> isDatasetStarred(@PathVariable Long id, @RequestParam String userEmail) {
        boolean isStarred = starringService.isResourceStarredByUser(userEmail, id, UserStarredResource.ResourceType.DATASET);
        return ResponseEntity.ok(isStarred);
    }
    
    @PostMapping("/notebooks/{id}/star")
    public ResponseEntity<Boolean> toggleStarNotebook(@PathVariable Long id, 
                                                     @RequestParam String userEmail,
                                                     @RequestParam(required = false, defaultValue = "User") String userName) {
        boolean isStarred = starringService.toggleStarResource(userEmail, userName, id, UserStarredResource.ResourceType.NOTEBOOK);
        return ResponseEntity.ok(isStarred);
    }
    
    @GetMapping("/notebooks/{id}/star")
    public ResponseEntity<Boolean> isNotebookStarred(@PathVariable Long id, @RequestParam String userEmail) {
        boolean isStarred = starringService.isResourceStarredByUser(userEmail, id, UserStarredResource.ResourceType.NOTEBOOK);
        return ResponseEntity.ok(isStarred);
    }
    
    @PostMapping("/repositories/{id}/star")
    public ResponseEntity<Boolean> toggleStarRepository(@PathVariable Long id, 
                                                       @RequestParam String userEmail,
                                                       @RequestParam(required = false, defaultValue = "User") String userName) {
        boolean isStarred = starringService.toggleStarResource(userEmail, userName, id, UserStarredResource.ResourceType.REPOSITORY);
        return ResponseEntity.ok(isStarred);
    }
    
    @GetMapping("/repositories/{id}/star")
    public ResponseEntity<Boolean> isRepositoryStarred(@PathVariable Long id, @RequestParam String userEmail) {
        boolean isStarred = starringService.isResourceStarredByUser(userEmail, id, UserStarredResource.ResourceType.REPOSITORY);
        return ResponseEntity.ok(isStarred);
    }
    
    @PostMapping("/storage-resources/{id}/star")
    public ResponseEntity<Boolean> toggleStarStorageResource(@PathVariable Long id, 
                                                           @RequestParam String userEmail,
                                                           @RequestParam(required = false, defaultValue = "User") String userName) {
        boolean isStarred = starringService.toggleStarResource(userEmail, userName, id, UserStarredResource.ResourceType.STORAGE_RESOURCE);
        return ResponseEntity.ok(isStarred);
    }
    
    @GetMapping("/storage-resources/{id}/star")
    public ResponseEntity<Boolean> isStorageResourceStarred(@PathVariable Long id, @RequestParam String userEmail) {
        boolean isStarred = starringService.isResourceStarredByUser(userEmail, id, UserStarredResource.ResourceType.STORAGE_RESOURCE);
        return ResponseEntity.ok(isStarred);
    }
    
    @PostMapping("/compute-resources/{id}/star")
    public ResponseEntity<Boolean> toggleStarComputeResource(@PathVariable Long id, 
                                                           @RequestParam String userEmail,
                                                           @RequestParam(required = false, defaultValue = "User") String userName) {
        boolean isStarred = starringService.toggleStarResource(userEmail, userName, id, UserStarredResource.ResourceType.COMPUTE_RESOURCE);
        return ResponseEntity.ok(isStarred);
    }
    
    @GetMapping("/compute-resources/{id}/star")
    public ResponseEntity<Boolean> isComputeResourceStarred(@PathVariable Long id, @RequestParam String userEmail) {
        boolean isStarred = starringService.isResourceStarredByUser(userEmail, id, UserStarredResource.ResourceType.COMPUTE_RESOURCE);
        return ResponseEntity.ok(isStarred);
    }
    
    @GetMapping("/users/{userEmail}/starred")
    public ResponseEntity<List<Map<String, Object>>> getStarredResources(@PathVariable String userEmail) {
        List<Map<String, Object>> starredResources = starringService.getStarredResourcesByUser(userEmail);
        return ResponseEntity.ok(starredResources);
    }
}