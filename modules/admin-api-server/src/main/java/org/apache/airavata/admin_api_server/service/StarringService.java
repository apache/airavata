package org.apache.airavata.admin_api_server.service;

import org.apache.airavata.admin_api_server.entity.*;
import org.apache.airavata.admin_api_server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StarringService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserStarredResourceRepository starredResourceRepository;
    
    @Autowired
    private ModelRepository modelRepository;
    
    @Autowired
    private DatasetRepository datasetRepository;
    
    @Autowired
    private NotebookRepository notebookRepository;
    
    @Autowired
    private RepositoryRepository repositoryRepository;
    
    @Autowired
    private StorageResourceRepository storageResourceRepository;
    
    @Autowired
    private ComputeResourceRepository computeResourceRepository;
    
    public User getOrCreateUser(String email, String name) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        User newUser = new User(email, name);
        return userRepository.save(newUser);
    }
    
    @Transactional
    public boolean toggleStarResource(String userEmail, String userName, Long resourceId, UserStarredResource.ResourceType resourceType) {
        User user = getOrCreateUser(userEmail, userName);
        
        Optional<UserStarredResource> existingStar = starredResourceRepository
            .findByUserAndResourceIdAndResourceType(user, resourceId, resourceType);
        
        if (existingStar.isPresent()) {
            // Unstar - remove the record
            starredResourceRepository.delete(existingStar.get());
            return false; // Now unstarred
        } else {
            // Star - create new record
            UserStarredResource newStar = new UserStarredResource(user, resourceId, resourceType);
            starredResourceRepository.save(newStar);
            return true; // Now starred
        }
    }
    
    public boolean isResourceStarredByUser(String userEmail, Long resourceId, UserStarredResource.ResourceType resourceType) {
        Optional<User> user = userRepository.findByEmail(userEmail);
        if (user.isEmpty()) {
            return false;
        }
        
        return starredResourceRepository.existsByUserAndResourceIdAndResourceType(
            user.get(), resourceId, resourceType);
    }
    
    public List<Map<String, Object>> getStarredResourcesByUser(String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        User user = userOpt.get();
        List<UserStarredResource> starredResources = starredResourceRepository.findByUser(user);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (UserStarredResource starredResource : starredResources) {
            Map<String, Object> resourceData = new HashMap<>();
            Object actualResource = null;
            
            switch (starredResource.getResourceType()) {
                case MODEL:
                    actualResource = modelRepository.findById(starredResource.getResourceId()).orElse(null);
                    break;
                case DATASET:
                    actualResource = datasetRepository.findById(starredResource.getResourceId()).orElse(null);
                    break;
                case NOTEBOOK:
                    actualResource = notebookRepository.findById(starredResource.getResourceId()).orElse(null);
                    break;
                case REPOSITORY:
                    actualResource = repositoryRepository.findById(starredResource.getResourceId()).orElse(null);
                    break;
                case STORAGE_RESOURCE:
                    actualResource = storageResourceRepository.findById(starredResource.getResourceId()).orElse(null);
                    break;
                case COMPUTE_RESOURCE:
                    actualResource = computeResourceRepository.findById(starredResource.getResourceId()).orElse(null);
                    break;
            }
            
            if (actualResource != null) {
                resourceData.put("resource", actualResource);
                resourceData.put("type", starredResource.getResourceType().toString().toLowerCase());
                result.add(resourceData);
            }
        }
        
        return result;
    }
}