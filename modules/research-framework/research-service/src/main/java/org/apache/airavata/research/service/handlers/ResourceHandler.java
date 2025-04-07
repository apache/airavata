/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.research.service.handlers;

import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.research.service.AiravataService;
import org.apache.airavata.research.service.ResponseTypes.ResourceResponse;
import org.apache.airavata.research.service.enums.ResourceTypeEnum;
import org.apache.airavata.research.service.model.entity.Resource;
import org.apache.airavata.research.service.model.entity.Tag;
import org.apache.airavata.research.service.model.repo.ResourceRepository;
import org.apache.airavata.research.service.model.repo.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ResourceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceHandler.class);

    private final AiravataService airavataService;
    private final TagRepository tagRepository;
    private final ResourceRepository resourceRepository;

    public ResourceHandler(AiravataService airavataService, TagRepository tagRepository, ResourceRepository resourceRepository) {
        this.airavataService = airavataService;
        this.tagRepository = tagRepository;
        this.resourceRepository = resourceRepository;
    }

    public void initializeResource(Resource resource) {
        Set<String> userSet = new HashSet<>();
        for (String authorId : resource.getAuthors()) {
            try {
                UserProfile fetchedUser = airavataService.getUserProfile(authorId);
                userSet.add(fetchedUser.getUserId());

            } catch (Exception e) {
                LOGGER.error("Error while fetching user profile with the userId: {}", authorId, e);
                throw new RuntimeException("Error while fetching user profile with the userId: " + authorId, e);
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
    }

    public ResourceResponse createResource(Resource resource, ResourceTypeEnum type) {
        ResourceResponse response = new ResourceResponse();

        initializeResource(resource);
        response.setResource(resourceRepository.save(resource));
        response.setType(type);

        return response;
    }

    public Resource getResourceById(String id) {
        // Your logic to fetch the resource by ID
        Optional<Resource> opResource = resourceRepository.findById(id);

        if (opResource.isEmpty()) {
            throw new RuntimeException("Resource not found: " + id);
        }

        return opResource.get();
    }

    public Page<Resource> getAllResources(int pageNumber, int pageSize, List<Class<? extends Resource>> typeList, String[] tag) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        if (tag == null || tag.length == 0) {
            return resourceRepository.findAllByTypes(typeList, pageable);
        }

        return resourceRepository.findAllByTypesAndAllTags(typeList, tag, tag.length, pageable);
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    // Helper methods
//    public ResourceResponse resourceToResponse(Resource resource) {
//        ResourceResponse response = new ResourceResponse();
//        response.setResource(resource);
//        response.setType(resource.getType());
//        return response;
//    }
}
