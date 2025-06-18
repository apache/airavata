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
package org.apache.airavata.research.service.handlers;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.research.service.AiravataService;
import org.apache.airavata.research.service.dto.CreateResourceRequest;
import org.apache.airavata.research.service.dto.ModifyResourceRequest;
import org.apache.airavata.research.service.dto.ResourceResponse;
import org.apache.airavata.research.service.enums.ResourceTypeEnum;
import org.apache.airavata.research.service.enums.StateEnum;
import org.apache.airavata.research.service.enums.StatusEnum;
import org.apache.airavata.research.service.model.UserContext;
import org.apache.airavata.research.service.model.entity.RepositoryResource;
import org.apache.airavata.research.service.model.entity.Resource;
import org.apache.airavata.research.service.model.entity.Tag;
import org.apache.airavata.research.service.model.repo.ProjectRepository;
import org.apache.airavata.research.service.model.repo.ResourceRepository;
import org.apache.airavata.research.service.model.repo.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ResourceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceHandler.class);

    private final AiravataService airavataService;
    private final TagRepository tagRepository;
    private final ResourceRepository resourceRepository;
    private final ProjectRepository projectRepository;

    public ResourceHandler(
            AiravataService airavataService,
            TagRepository tagRepository,
            ResourceRepository resourceRepository,
            ProjectRepository projectRepository) {
        this.airavataService = airavataService;
        this.tagRepository = tagRepository;
        this.resourceRepository = resourceRepository;
        this.projectRepository = projectRepository;
    }

    public void initializeResource(Resource resource) {
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

        HashSet<Tag> tags = new HashSet<>();
        for (Tag t : resource.getTags()) {
            String tagValue = t.getValue();
            Tag fetchedTag = tagRepository.findByValue(tagValue);
            if (fetchedTag == null) {
                fetchedTag = tagRepository.save(t);
            }
            tags.add(fetchedTag);
        }
        resource.setAuthors(userSet);
        resource.setTags(tags);
        resource.setState(StateEnum.ACTIVE);
    }

    public ResourceResponse createResource(Resource resource, ResourceTypeEnum type) {
        ResourceResponse response = new ResourceResponse();

        initializeResource(resource);
        response.setResource(resourceRepository.save(resource));
        response.setType(type);

        return response;
    }

    public void transferResourceRequestFields(Resource resource, CreateResourceRequest createResourceRequest) {
        // check that the logged in author is at least one of the authors making the request
        String currentUserId = UserContext.userId();
        boolean found = false;
        for (String authorId : createResourceRequest.getAuthors()) {
            if (authorId.equalsIgnoreCase(currentUserId)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException(
                    "You cannot create a resource on another author's behalf, without you being one of the authors");
        }

        resource.setName(createResourceRequest.getName());
        resource.setDescription(createResourceRequest.getDescription());
        resource.setAuthors(createResourceRequest.getAuthors().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet()));
        Set<org.apache.airavata.research.service.model.entity.Tag> tagsSet = new HashSet<>();
        for (String tag : createResourceRequest.getTags()) {
            org.apache.airavata.research.service.model.entity.Tag t =
                    new org.apache.airavata.research.service.model.entity.Tag();
            t.setValue(tag);
            tagsSet.add(t);
        }
        resource.setTags(tagsSet);
        resource.setPrivacy(createResourceRequest.getPrivacy());
        resource.setStatus(StatusEnum.NONE);
        resource.setHeaderImage(createResourceRequest.getHeaderImage());
    }

    public ResourceResponse createRepositoryResource(CreateResourceRequest resourceRequest, String repoUrl) {
        RepositoryResource repositoryResource = new RepositoryResource();
        transferResourceRequestFields(repositoryResource, resourceRequest);
        repositoryResource.setRepositoryUrl(repoUrl);
        return createResource(repositoryResource, ResourceTypeEnum.REPOSITORY);
    }

    public Resource modifyResource(ModifyResourceRequest resourceRequest) {
        Optional<Resource> resourceOp = resourceRepository.findById(resourceRequest.getId());
        if (resourceOp.isEmpty()) {
            throw new EntityNotFoundException("Resource not found");
        }

        Resource resource = resourceOp.get();
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

        transferResourceRequestFields(resource, resourceRequest);
        initializeResource(resource);

        resourceRepository.save(resource);
        return resource;
    }

    public Resource getResourceById(String id) {
        // Your logic to fetch the resource by ID
        Optional<Resource> opResource = resourceRepository.findByIdAndState(id, StateEnum.ACTIVE);

        if (opResource.isEmpty()) {
            throw new EntityNotFoundException("Resource not found: " + id);
        }

        return opResource.get();
    }

    public boolean deleteResourceById(String id) {
        Optional<Resource> opResource = resourceRepository.findByIdAndState(id, StateEnum.ACTIVE);

        if (opResource.isEmpty()) {
            throw new EntityNotFoundException("Resource not found: " + id);
        }

        Resource resource = opResource.get();

        String userEmail = UserContext.userId();
        if (!resource.getAuthors().contains(userEmail.toLowerCase())) {
            String errorMsg = String.format(
                    "User %s not authorized to delete resource: %s (%s), type: %s",
                    userEmail, resource.getName(), id, resource.getType().toString());
            LOGGER.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        resource.setState(StateEnum.DELETED);
        resourceRepository.delete(resource);
        return true;
    }

    public Page<Resource> getAllResources(
            int pageNumber, int pageSize, List<Class<? extends Resource>> typeList, String[] tag, String nameSearch) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        if (tag == null || tag.length == 0) {
            return resourceRepository.findAllByTypes(typeList, nameSearch, pageable);
        }

        return resourceRepository.findAllByTypesAndAllTags(
                typeList, tag, tag.length, nameSearch.toLowerCase(), pageable);
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public List<Tag> getAllTagsByPopularity() {
        return tagRepository.findDistinctByPopularity(100);
    }

    public List<Resource> getAllResourcesByTypeAndName(Class<? extends Resource> type, String name) {

        return resourceRepository.findByTypeAndNameContainingIgnoreCase(type, name.toLowerCase());
    }
}
