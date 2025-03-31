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

import org.apache.airavata.research.service.ResponseTypes.ResourceResponse;
import org.apache.airavata.research.service.enums.ResourceTypeEnum;
import org.apache.airavata.research.service.model.entity.*;
import org.apache.airavata.research.service.model.repo.ResourceRepository;
import org.apache.airavata.research.service.model.repo.TagRepository;
import org.apache.airavata.research.service.model.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@org.springframework.stereotype.Service
public class ProjectHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectHandler.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    ResourceRepository resourceRepository;

    public void initializeResource(Resource resource) {
        Set<User> userSet = new HashSet<>();
        for (User u: resource.getAuthors()) {
            Optional<User> fetchedUser = userRepository.findById(u.getId());
            if (fetchedUser.isEmpty()) {
                throw new RuntimeException("User not found: " + u.getId());
            }
            userSet.add(fetchedUser.get());
        }

        HashSet<Tag> tags = new HashSet<>();
        for (Tag t: resource.getTags()) {
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

    public ResourceResponse resourceToResponse(Resource resource) {
        ResourceResponse response = new ResourceResponse();
        response.setResource(resource);

        if (resource instanceof DatasetResource) {
            response.setType(ResourceTypeEnum.DATASET);
        } else if (resource instanceof NotebookResource) {
            response.setType(ResourceTypeEnum.NOTEBOOK);
        } else if (resource instanceof RepositoryResource) {
            response.setType(ResourceTypeEnum.REPOSITORY);
        } else if (resource instanceof ModelResource) {
            response.setType(ResourceTypeEnum.MODEL);
        } else {
            throw new RuntimeException("Unknown resource type: " + resource.getClass().getName());
        }

        return response;
    }

    public ResourceResponse getResourceById(String id)
    {
        // Your logic to fetch the resource by ID
        Optional<Resource> opResource = resourceRepository.findById(id);

        if (opResource.isEmpty()) {
            throw new RuntimeException("Resource not found: " + id);
        }

        Resource resource = opResource.get();
        return resourceToResponse(resource);
    }

    public Page<ResourceResponse> getAllResources(int pageNumber, int pageSize, List<Class<? extends Resource>> typeList) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Resource> resources = resourceRepository.findAllByTypes(typeList, pageable);
        return resources.map(this::resourceToResponse);
    }

}
