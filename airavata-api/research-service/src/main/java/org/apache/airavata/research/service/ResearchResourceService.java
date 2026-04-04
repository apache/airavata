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
package org.apache.airavata.research.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.airavata.config.UserContext;
import org.apache.airavata.model.user.proto.UserProfile;
import org.apache.airavata.research.model.PrivacyEnum;
import org.apache.airavata.research.model.RepositoryResourceEntity;
import org.apache.airavata.research.model.ResourceEntity;
import org.apache.airavata.research.model.ResourceStarEntity;
import org.apache.airavata.research.model.ResourceTypeEnum;
import org.apache.airavata.research.model.StateEnum;
import org.apache.airavata.research.model.StatusEnum;
import org.apache.airavata.research.model.TagEntity;
import org.apache.airavata.research.repository.ResearchProjectRepository;
import org.apache.airavata.research.repository.ResourceRepository;
import org.apache.airavata.research.repository.ResourceStarRepository;
import org.apache.airavata.research.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ResearchResourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResearchResourceService.class);

    private final AiravataService airavataService;
    private final TagRepository tagRepository;
    private final ResourceRepository resourceRepository;
    private final ResearchProjectRepository projectRepository;
    private final ResourceStarRepository resourceStarRepository;

    public ResearchResourceService(
            AiravataService airavataService,
            TagRepository tagRepository,
            ResourceRepository resourceRepository,
            ResearchProjectRepository projectRepository,
            ResourceStarRepository resourceStarRepository) {
        this.airavataService = airavataService;
        this.tagRepository = tagRepository;
        this.resourceRepository = resourceRepository;
        this.projectRepository = projectRepository;
        this.resourceStarRepository = resourceStarRepository;
    }

    public void initializeResource(ResourceEntity resource) {
        Set<String> userSet = new HashSet<>();
        for (String authorId : resource.getAuthors()) {
            try {
                UserProfile fetchedUser = airavataService.getUserProfile(authorId);
                userSet.add(fetchedUser.getUserId());
            } catch (Exception e) {
                LOGGER.error("Error while fetching user profile with the userId: {}", authorId, e);
                throw new EntityNotFoundException("Error while fetching user profile with the userId: " + authorId, e);
            }
        }

        HashSet<TagEntity> tags = new HashSet<>();
        for (TagEntity t : resource.getTags()) {
            String tagValue = t.getValue();
            TagEntity fetchedTag = tagRepository.findByValue(tagValue);
            if (fetchedTag == null) {
                fetchedTag = tagRepository.save(t);
            }
            tags.add(fetchedTag);
        }
        resource.setAuthors(userSet);
        resource.setTags(tags);
        resource.setState(StateEnum.ACTIVE);
    }

    public ResourceEntity createResource(ResourceEntity resource, ResourceTypeEnum type) {
        initializeResource(resource);
        return resourceRepository.save(resource);
    }

    public void transferResourceRequestFields(
            ResourceEntity resource,
            String name,
            String description,
            String headerImage,
            Set<String> tags,
            Set<String> authors,
            PrivacyEnum privacy) {
        // check that the logged in author is at least one of the authors making the request
        String currentUserId = UserContext.userId();
        boolean found = false;
        for (String authorId : authors) {
            if (authorId.equalsIgnoreCase(currentUserId)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException(
                    "You cannot create a resource on another author's behalf, without you being one of the authors");
        }

        resource.setName(name);
        resource.setDescription(description);
        resource.setAuthors(authors.stream().map(String::toLowerCase).collect(Collectors.toSet()));
        Set<org.apache.airavata.research.model.TagEntity> tagsSet = new HashSet<>();
        for (String tag : tags) {
            org.apache.airavata.research.model.TagEntity t = new org.apache.airavata.research.model.TagEntity();
            t.setValue(tag);
            tagsSet.add(t);
        }
        resource.setTags(tagsSet);
        resource.setPrivacy(privacy);
        resource.setStatus(StatusEnum.NONE);
        resource.setHeaderImage(headerImage);
    }

    public ResourceEntity createRepositoryResourceEntity(
            String name,
            String description,
            String headerImage,
            Set<String> tags,
            Set<String> authors,
            PrivacyEnum privacy,
            String repoUrl) {
        RepositoryResourceEntity repositoryResource = new RepositoryResourceEntity();
        transferResourceRequestFields(repositoryResource, name, description, headerImage, tags, authors, privacy);
        repositoryResource.setRepositoryUrl(repoUrl);
        return createResource(repositoryResource, ResourceTypeEnum.REPOSITORY);
    }

    public ResourceEntity modifyResource(
            String id,
            String name,
            String description,
            String headerImage,
            Set<String> tags,
            Set<String> authors,
            PrivacyEnum privacy) {
        Optional<ResourceEntity> resourceOp = resourceRepository.findById(id);
        if (resourceOp.isEmpty()) {
            throw new EntityNotFoundException("Resource not found");
        }

        ResourceEntity resource = resourceOp.get();
        if (StateEnum.DELETED.equals(resource.getState())) {
            throw new RuntimeException(String.format("Cannot modify deleted resource: %s", resource.getId()));
        }

        // ensure that the user making the request is one of the current authors
        boolean found = false;
        for (String authorId : resource.getAuthors()) {
            if (authorId.equalsIgnoreCase(UserContext.userId())) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException("Author is not authorized");
        }

        transferResourceRequestFields(resource, name, description, headerImage, tags, authors, privacy);
        initializeResource(resource);

        resourceRepository.save(resource);
        return resource;
    }

    public boolean starOrUnstarResource(String resourceId) {
        ResourceEntity resource = getResourceById(resourceId);
        String userId = UserContext.userId();

        List<ResourceStarEntity> resourceStars = resourceStarRepository.findByResourceAndUserId(resource, userId);

        if (resourceStars.isEmpty()) {
            // user has not starred the resource yet
            ResourceStarEntity resourceStar = new ResourceStarEntity();
            resourceStar.setUserId(userId);
            resourceStar.setResource(resource);
            resourceStarRepository.save(resourceStar);
        } else {
            ResourceStarEntity resourceStar = resourceStars.get(0);
            resourceStarRepository.delete(resourceStar);
        }

        return true;
    }

    public boolean checkWhetherUserStarredResource(String resourceId) {
        ResourceEntity resource = getResourceById(resourceId);
        String userId = UserContext.userId();

        return resourceStarRepository.existsByResourceAndUserId(resource, userId);
    }

    public List<ResourceEntity> getAllStarredResources(String userId) {
        String loggedInUser = UserContext.userId();
        if (!loggedInUser.equals(userId)) {
            throw new RuntimeException(
                    String.format("User %s is not authorized to request stars for %s", loggedInUser, userId));
        }

        List<ResourceStarEntity> resourceStars =
                resourceStarRepository.findByUserIdAndResourceState(loggedInUser, StateEnum.ACTIVE);
        return resourceStars.stream().map(ResourceStarEntity::getResource).collect(Collectors.toList());
    }

    public long getResourceStarCount(String resourceId) {
        ResourceEntity resource = getResourceById(resourceId);
        return resourceStarRepository.countResourceStarByResource(resource);
    }

    public ResourceEntity getResourceById(String id) {
        Optional<ResourceEntity> opResource = resourceRepository.findByIdAndState(id, StateEnum.ACTIVE);

        if (opResource.isEmpty()) {
            throw new EntityNotFoundException("Resource not found: " + id);
        }

        ResourceEntity resource = opResource.get();
        boolean isAuthenticated = UserContext.isAuthenticated();

        if (resource.getPrivacy().equals(PrivacyEnum.PUBLIC)) {
            return resource;
        } else if (isAuthenticated
                && resource.getAuthors().contains(UserContext.userId().toLowerCase())) {
            return resource;
        } else {
            throw new EntityNotFoundException("Resource not found: " + id);
        }
    }

    public boolean deleteResourceById(String id) {
        Optional<ResourceEntity> opResource = resourceRepository.findByIdAndState(id, StateEnum.ACTIVE);

        if (opResource.isEmpty()) {
            throw new EntityNotFoundException("Resource not found: " + id);
        }

        ResourceEntity resource = opResource.get();

        String userEmail = UserContext.userId();
        if (!resource.getAuthors().contains(userEmail.toLowerCase())) {
            String errorMsg = String.format(
                    "User %s not authorized to delete resource: %s (%s), type: %s",
                    userEmail, resource.getName(), id, resource.getType().toString());
            LOGGER.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        resource.setState(StateEnum.DELETED);
        resourceRepository.save(resource);
        return true;
    }

    public Page<ResourceEntity> getAllResources(
            int pageNumber,
            int pageSize,
            List<Class<? extends ResourceEntity>> typeList,
            String[] tag,
            String nameSearch) {
        boolean isAuthenticated = UserContext.isAuthenticated();
        if (isAuthenticated) {
            return getAllResourcesUserSignedIn(pageNumber, pageSize, typeList, tag, nameSearch, UserContext.userId());
        }
        return getAllPublicResources(pageNumber, pageSize, typeList, tag, nameSearch);
    }

    public List<TagEntity> getAllTags() {
        return tagRepository.findAll();
    }

    public List<TagEntity> getAllTagsByPopularity() {
        return tagRepository.findDistinctByPopularity(100);
    }

    public List<TagEntity> getAllTagsByAlphabeticalOrder() {
        return tagRepository.findAll(Sort.by(Sort.Direction.ASC, "value"));
    }

    public List<ResourceEntity> getAllResourcesByTypeAndName(Class<? extends ResourceEntity> type, String name) {
        return resourceRepository.findByTypeAndNameContainingIgnoreCase(type, name.toLowerCase(), UserContext.userId());
    }

    private Page<ResourceEntity> getAllPublicResources(
            int pageNumber,
            int pageSize,
            List<Class<? extends ResourceEntity>> typeList,
            String[] tag,
            String nameSearch) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        if (tag == null || tag.length == 0) {
            return resourceRepository.findAllByTypes(typeList, nameSearch, pageable);
        }

        return resourceRepository.findAllByTypesAndAllTags(
                typeList, tag, tag.length, nameSearch.toLowerCase(), pageable);
    }

    private Page<ResourceEntity> getAllResourcesUserSignedIn(
            int pageNumber,
            int pageSize,
            List<Class<? extends ResourceEntity>> typeList,
            String[] tag,
            String nameSearch,
            String userId) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        if (tag == null || tag.length == 0) {
            return resourceRepository.findAllByTypesForUser(typeList, nameSearch.toLowerCase(), userId, pageable);
        }

        return resourceRepository.findAllByTypesAndAllTagsForUser(
                typeList, tag, (long) tag.length, nameSearch.toLowerCase(), userId, pageable);
    }
}
