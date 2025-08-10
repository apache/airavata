/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.research.service.handlers;

import jakarta.persistence.EntityNotFoundException;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.research.service.AiravataService;
import org.apache.airavata.research.service.dto.CreateResourceRequest;
import org.apache.airavata.research.service.dto.ModifyResourceRequest;
import org.apache.airavata.research.service.dto.ResourceResponse;
import org.apache.airavata.research.service.enums.PrivacyEnum;
import org.apache.airavata.research.service.enums.ResourceTypeEnum;
import org.apache.airavata.research.service.enums.StateEnum;
import org.apache.airavata.research.service.enums.StatusEnum;
import org.apache.airavata.research.service.model.UserContext;
import org.apache.airavata.research.service.model.entity.RepositoryResource;
import org.apache.airavata.research.service.model.entity.Resource;
import org.apache.airavata.research.service.model.entity.ResourceAuthor;
import org.apache.airavata.research.service.model.entity.ResourceStar;
import org.apache.airavata.research.service.model.entity.ResourceVerificationActivity;
import org.apache.airavata.research.service.model.entity.Tag;
import org.apache.airavata.research.service.model.repo.ProjectRepository;
import org.apache.airavata.research.service.model.repo.ResourceRepository;
import org.apache.airavata.research.service.model.repo.ResourceStarRepository;
import org.apache.airavata.research.service.model.repo.ResourceVerificationActivityRepository;
import org.apache.airavata.research.service.model.repo.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ResourceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceHandler.class);

    private final AiravataService airavataService;
    private final TagRepository tagRepository;
    private final ResourceRepository resourceRepository;
    private final ProjectRepository projectRepository;
    private final ResourceStarRepository resourceStarRepository;
    private final ResourceVerificationActivityRepository verificationActivityRepository;

    @Value("#{'${airavata.research-portal.admin-emails}'.split(',')}")
    private Set<String> cybershuttleAdminEmails;

    public ResourceHandler(
            AiravataService airavataService,
            TagRepository tagRepository,
            ResourceRepository resourceRepository,
            ProjectRepository projectRepository,
            ResourceStarRepository resourceStarRepository,
            ResourceVerificationActivityRepository verificationActivityRepository
    ) {
        this.airavataService = airavataService;
        this.tagRepository = tagRepository;
        this.resourceRepository = resourceRepository;
        this.projectRepository = projectRepository;
        this.resourceStarRepository = resourceStarRepository;
        this.verificationActivityRepository = verificationActivityRepository;
    }

    public void initializeResource(Resource resource) {
        Set<ResourceAuthor> userSet = new HashSet<>();
        for (ResourceAuthor author : resource.getAuthors()) {
            try {
                UserProfile fetchedUser = airavataService.getUserProfile(author.getAuthorId());
                ResourceAuthor newAuthor = new ResourceAuthor();
                newAuthor.setAuthorId(fetchedUser.getUserId());
                newAuthor.setRole(author.getRole());
                userSet.add(newAuthor);
            } catch (Exception e) {
                LOGGER.error("Error while fetching user profile with the userId: {}", author.getAuthorId(), e);
                throw new EntityNotFoundException(
                        "Error while fetching user profile with the userId: " + author.getAuthorId(), e);
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
        for (ResourceAuthor author : createResourceRequest.getAuthors()) {
            author.setAuthorId(author.getAuthorId().toLowerCase());
            if (author.getAuthorId().equalsIgnoreCase(currentUserId)) {
                found = true;
            }
        }
        if (!found) {
            throw new RuntimeException(
                    "You cannot create a resource on another author's behalf, without you being one of the authors");
        }

        resource.setName(createResourceRequest.getName());
        resource.setDescription(createResourceRequest.getDescription());
        resource.setAuthors(createResourceRequest.getAuthors());
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
        for (ResourceAuthor author : resource.getAuthors()) {
            if (author.getAuthorId().equalsIgnoreCase(UserContext.userId())) {
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

    public boolean starOrUnstarResource(String resourceId) {
        Resource resource = getResourceById(resourceId);
        String userId = UserContext.userId();

        List<ResourceStar> resourceStars = resourceStarRepository.findByResourceAndUserId(resource, userId);

        if (resourceStars.isEmpty()) {
            // user has not starred the resource yet
            ResourceStar resourceStar = new ResourceStar();
            resourceStar.setUserId(userId);
            resourceStar.setResource(resource);
            resourceStarRepository.save(resourceStar);
        } else {
            ResourceStar resourceStar = resourceStars.get(0);
            resourceStarRepository.delete(resourceStar);
        }

        return true;
    }

    public boolean checkWhetherUserStarredResource(String resourceId) {
        Resource resource = getResourceById(resourceId);
        String userId = UserContext.userId();

        return resourceStarRepository.existsByResourceAndUserId(resource, userId);
    }

    public List<Resource> getAllStarredResources(String userId) {
        String loggedInUser = UserContext.userId();
        if (!loggedInUser.equals(userId)) {
            throw new RuntimeException(
                    String.format("User %s is not authorized to request stars for %s", loggedInUser, userId));
        }

        List<ResourceStar> resourceStars =
                resourceStarRepository.findByUserIdAndResourceState(loggedInUser, StateEnum.ACTIVE);
        return resourceStars.stream().map(ResourceStar::getResource).collect(Collectors.toList());
    }

    public long getResourceStarCount(String resourceId) {
        Resource resource = getResourceById(resourceId);
        return resourceStarRepository.countResourceStarByResource(resource);
    }

    public Resource getResourceById(String id) {
        Optional<Resource> opResource = resourceRepository.findByIdAndState(id, StateEnum.ACTIVE);

        if (opResource.isEmpty()) {
            throw new EntityNotFoundException("Resource not found: " + id);
        }

        Resource resource = opResource.get();
        boolean isAuthenticated = UserContext.isAuthenticated();

        if (resource.getPrivacy().equals(PrivacyEnum.PUBLIC)) {
            return resource;
        } else if (isAuthenticated
                && resource
                .getAuthors()
                .stream()
                .map(ResourceAuthor::getAuthorId)
                .anyMatch(authorId -> authorId.equals(UserContext.userId().toLowerCase()))) {
            return resource;
        } else {
            throw new EntityNotFoundException("Resource not found: " + id);
        }
    }

    public boolean deleteResourceById(String id) {
        Optional<Resource> opResource = resourceRepository.findByIdAndState(id, StateEnum.ACTIVE);

        if (opResource.isEmpty()) {
            throw new EntityNotFoundException("Resource not found: " + id);
        }

        Resource resource = opResource.get();

        String userEmail = UserContext.userId();
        if (!resource
                .getAuthors()
                .stream()
                .map(ResourceAuthor::getAuthorId)
                .anyMatch(authorId -> authorId.equals(UserContext.userId().toLowerCase()))
        ) {
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

    public Page<Resource> getAllResources(
            int pageNumber, int pageSize, List<Class<? extends Resource>> typeList, String[] tag, String nameSearch) {
        boolean isAuthenticated = UserContext.isAuthenticated();
        if (isAuthenticated) {
            return getAllResourcesUserSignedIn(pageNumber, pageSize, typeList, tag, nameSearch, UserContext.userId());
        }
        return getAllPublicResources(pageNumber, pageSize, typeList, tag, nameSearch);
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public List<Tag> getAllTagsByPopularity() {
        return tagRepository.findDistinctByPopularity(100);
    }

    public List<Tag> getAllTagsByAlphabeticalOrder() {
        return tagRepository.findAll(Sort.by(Sort.Direction.ASC, "value"));
    }

    public List<Resource> getAllResourcesByTypeAndName(Class<? extends Resource> type, String name) {
        return resourceRepository.findByTypeAndNameContainingIgnoreCase(type, name.toLowerCase(), UserContext.userId());
    }

    public Resource submitResourceForVerification(String id) {
        Resource resource = getResourceById(id);
        String userId = UserContext.userId();

        if (!isResourceAuthor(resource, userId)) {
            throw new IllegalArgumentException(String.format("User %s is not authorized to request verification for resource %s", userId, id));
        }

        resource.setStatus(StatusEnum.PENDING);
        resourceRepository.save(resource);

        ResourceVerificationActivity activity = new ResourceVerificationActivity();
        activity.setResource(resource);
        activity.setUserId(userId);
        activity.setStatus(StatusEnum.PENDING);
        verificationActivityRepository.save(activity);

        return resource;
    }

    public List<ResourceVerificationActivity> getResourceVerificationActivities(String id) {
        Resource resource = getResourceById(id);
        String userId = UserContext.userId();

        if (!isResourceAuthor(resource, userId) && !cybershuttleAdminEmails.contains(userId.toLowerCase())) {
            throw new IllegalArgumentException(String.format("User %s is not authorized to pull verification activities for resource %s", userId, id));
        }

        return verificationActivityRepository.findAllByResourceOrderByUpdatedAtDesc(resource);
    }

    public List<Resource> getAllResourcesWithStatus(List<StatusEnum> includeStatus) {
        return resourceRepository.findAllByStatusInOrderByCreatedAtDesc(
                includeStatus
        );
    }

    private Page<Resource> getAllPublicResources(
            int pageNumber, int pageSize, List<Class<? extends Resource>> typeList, String[] tag, String nameSearch) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        if (tag == null || tag.length == 0) {
            return resourceRepository.findAllByTypes(typeList, nameSearch, pageable);
        }

        return resourceRepository.findAllByTypesAndAllTags(
                typeList, tag, tag.length, nameSearch.toLowerCase(), pageable);
    }

    private Page<Resource> getAllResourcesUserSignedIn(
            int pageNumber,
            int pageSize,
            List<Class<? extends Resource>> typeList,
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

    private boolean isResourceAuthor(Resource resource, String userId) {
        return resource.getAuthors()
                .stream()
                .map(ResourceAuthor::getAuthorId)
                .anyMatch(userId::equals);
    }
}
