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
package org.apache.airavata.research.artifact.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.iam.model.UserContext;
import org.apache.airavata.iam.service.UserService;
import org.apache.airavata.research.artifact.entity.ArtifactStarEntity;
import org.apache.airavata.research.artifact.entity.DatasetArtifactEntity;
import org.apache.airavata.research.artifact.entity.ModelArtifactEntity;
import org.apache.airavata.research.artifact.entity.NotebookArtifactEntity;
import org.apache.airavata.research.artifact.entity.RepositoryArtifactEntity;
import org.apache.airavata.research.artifact.entity.ResearchArtifactEntity;
import org.apache.airavata.research.artifact.entity.TagEntity;
import org.apache.airavata.research.artifact.mapper.ArtifactMapper;
import org.apache.airavata.research.artifact.model.ArtifactState;
import org.apache.airavata.research.artifact.model.ArtifactStatus;
import org.apache.airavata.research.artifact.model.ArtifactType;
import org.apache.airavata.research.artifact.model.CreateArtifactRequest;
import org.apache.airavata.research.artifact.model.ModifyArtifactRequest;
import org.apache.airavata.research.artifact.model.Privacy;
import org.apache.airavata.research.artifact.model.ResearchArtifact;
import org.apache.airavata.research.artifact.model.Tag;
import org.apache.airavata.research.artifact.repository.ArtifactStarRepository;
import org.apache.airavata.research.artifact.repository.ResearchArtifactRepository;
import org.apache.airavata.research.artifact.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DefaultArtifactService implements ArtifactService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultArtifactService.class);

    private final UserService userProfileService;
    private final TagRepository tagRepository;
    private final ResearchArtifactRepository artifactRepository;
    private final ArtifactStarRepository artifactStarRepository;
    private final ArtifactMapper mapper;

    public DefaultArtifactService(
            UserService userProfileService,
            TagRepository tagRepository,
            ResearchArtifactRepository artifactRepository,
            ArtifactStarRepository artifactStarRepository,
            ArtifactMapper mapper) {
        this.userProfileService = userProfileService;
        this.tagRepository = tagRepository;
        this.artifactRepository = artifactRepository;
        this.artifactStarRepository = artifactStarRepository;
        this.mapper = mapper;
    }

    @Override
    public ResearchArtifact createArtifact(CreateArtifactRequest request, ArtifactType type) {
        var entity = createEntityFromRequest(request, type);
        transferRequestFields(entity, request);
        initializeArtifact(entity);
        var saved = artifactRepository.save(entity);
        return mapper.toModel(saved);
    }

    @Override
    public ResearchArtifact modifyArtifact(ModifyArtifactRequest request) {
        var artifactOp = artifactRepository.findById(request.getId());
        if (artifactOp.isEmpty()) {
            throw new EntityNotFoundException("Artifact not found");
        }

        var artifact = artifactOp.get();
        if (ArtifactState.DELETED.equals(artifact.getState())) {
            throw new IllegalStateException(String.format("Cannot modify deleted artifact: %s", artifact.getId()));
        }

        boolean found = false;
        for (String authorId : artifact.getAuthors()) {
            if (authorId.equalsIgnoreCase(UserContext.userId())) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Author is not authorized");
        }

        transferRequestFields(artifact, request);
        initializeArtifact(artifact);
        artifactRepository.save(artifact);
        return mapper.toModel(artifact);
    }

    @Override
    public boolean starOrUnstarArtifact(String artifactId) {
        var artifact = findActiveArtifact(artifactId);
        var userId = UserContext.userId();

        var artifactStars = artifactStarRepository.findByArtifactAndUserId(artifact, userId);

        if (artifactStars.isEmpty()) {
            var artifactStar = new ArtifactStarEntity();
            artifactStar.setUserId(userId);
            artifactStar.setArtifact(artifact);
            artifactStarRepository.save(artifactStar);
        } else {
            var artifactStar = artifactStars.get(0);
            artifactStarRepository.delete(artifactStar);
        }

        return true;
    }

    @Override
    public boolean checkWhetherUserStarredArtifact(String artifactId) {
        var artifact = findActiveArtifact(artifactId);
        var userId = UserContext.userId();
        return artifactStarRepository.existsByArtifactAndUserId(artifact, userId);
    }

    @Override
    public List<ResearchArtifact> getAllStarredArtifacts(String userId) {
        var loggedInUser = UserContext.userId();
        if (!loggedInUser.equals(userId)) {
            throw new IllegalArgumentException(
                    String.format("User %s is not authorized to request stars for %s", loggedInUser, userId));
        }

        var artifactStars = artifactStarRepository.findByUserIdAndArtifactState(loggedInUser, ArtifactState.ACTIVE);
        return artifactStars.stream()
                .map(ArtifactStarEntity::getArtifact)
                .map(mapper::toModel)
                .toList();
    }

    @Override
    public long getArtifactStarCount(String artifactId) {
        var artifact = findActiveArtifact(artifactId);
        return artifactStarRepository.countArtifactStarByArtifact(artifact);
    }

    @Override
    public ResearchArtifact getArtifactById(String id) {
        var artifact = findActiveArtifact(id);
        var isAuthenticated = UserContext.isAuthenticated();

        if (artifact.getPrivacy().equals(Privacy.PUBLIC)) {
            return mapper.toModel(artifact);
        } else if (isAuthenticated
                && artifact.getAuthors().contains(UserContext.userId().toLowerCase())) {
            return mapper.toModel(artifact);
        } else {
            throw new EntityNotFoundException("Artifact not found: " + id);
        }
    }

    @Override
    public boolean deleteArtifactById(String id) {
        var opArtifact = artifactRepository.findByIdAndState(id, ArtifactState.ACTIVE);

        if (opArtifact.isEmpty()) {
            throw new EntityNotFoundException("Artifact not found: " + id);
        }

        var artifact = opArtifact.get();

        var userEmail = UserContext.userId();
        if (!artifact.getAuthors().contains(userEmail.toLowerCase())) {
            String errorMsg = String.format(
                    "User %s not authorized to delete artifact: %s (%s), type: %s",
                    userEmail, artifact.getName(), id, artifact.getType().toString());
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        artifact.setState(ArtifactState.DELETED);
        artifactRepository.save(artifact);
        return true;
    }

    @Override
    public Page<ResearchArtifact> getAllArtifacts(
            int pageNumber, int pageSize, List<ArtifactType> types, String[] tag, String nameSearch) {
        var typeList = resolveEntityTypes(types);
        var isAuthenticated = UserContext.isAuthenticated();

        Page<ResearchArtifactEntity> page;
        if (isAuthenticated) {
            page = getAllArtifactsUserSignedIn(pageNumber, pageSize, typeList, tag, nameSearch, UserContext.userId());
        } else {
            page = getAllPublicArtifacts(pageNumber, pageSize, typeList, tag, nameSearch);
        }
        return page.map(mapper::toModel);
    }

    @Override
    public List<Tag> getAllTags() {
        return mapper.toTagList(tagRepository.findAll());
    }

    @Override
    public List<Tag> getAllTagsByPopularity() {
        return mapper.toTagList(tagRepository.findDistinctByPopularity(100));
    }

    @Override
    public List<Tag> getAllTagsByAlphabeticalOrder() {
        return mapper.toTagList(tagRepository.findAll(Sort.by(Sort.Direction.ASC, "value")));
    }

    @Override
    public List<ResearchArtifact> getAllArtifactsByTypeAndName(ArtifactType type, String name) {
        var entityType = resolveEntityType(type);
        return mapper.toModelList(artifactRepository.findByTypeAndNameContainingIgnoreCase(
                entityType, name.toLowerCase(), UserContext.userId()));
    }

    // --- Private helpers ---

    private ResearchArtifactEntity findActiveArtifact(String id) {
        var opArtifact = artifactRepository.findByIdAndState(id, ArtifactState.ACTIVE);
        if (opArtifact.isEmpty()) {
            throw new EntityNotFoundException("Artifact not found: " + id);
        }
        return opArtifact.get();
    }

    private ResearchArtifactEntity createEntityFromRequest(CreateArtifactRequest request, ArtifactType type) {
        return switch (type) {
            case REPOSITORY -> {
                var entity = new RepositoryArtifactEntity();
                if (request.getRepositoryUrl() != null) {
                    entity.setRepositoryUrl(request.getRepositoryUrl());
                }
                yield entity;
            }
            case DATASET -> {
                var entity = new DatasetArtifactEntity();
                if (request.getDatasetUrl() != null) {
                    entity.setDatasetUrl(request.getDatasetUrl());
                }
                yield entity;
            }
        };
    }

    private void transferRequestFields(ResearchArtifactEntity artifact, CreateArtifactRequest request) {
        var currentUserId = UserContext.userId();
        boolean found = false;
        for (String authorId : request.getAuthors()) {
            if (authorId.equalsIgnoreCase(currentUserId)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalArgumentException(
                    "You cannot create an artifact on another author's behalf, without you being one of the authors");
        }

        artifact.setName(request.getName());
        artifact.setDescription(request.getDescription());
        artifact.setAuthors(
                request.getAuthors().stream().map(String::toLowerCase).collect(Collectors.toSet()));
        var tagsSet = new HashSet<TagEntity>();
        for (String tag : request.getTags()) {
            var t = new TagEntity();
            t.setValue(tag);
            tagsSet.add(t);
        }
        artifact.setTags(tagsSet);
        artifact.setPrivacy(request.getPrivacy());
        artifact.setStatus(ArtifactStatus.NONE);
        artifact.setHeaderImage(request.getHeaderImage());
    }

    private void initializeArtifact(ResearchArtifactEntity artifact) {
        var userSet = new HashSet<String>();
        for (String authorId : artifact.getAuthors()) {
            try {
                var fetchedUser = userProfileService.getUserProfileById(
                        UserContext.authzToken(), authorId, UserContext.gatewayId());
                userSet.add(fetchedUser.getUserId());
            } catch (Exception e) {
                logger.error("Error while fetching user profile with the userId: {}", authorId, e);
                throw new EntityNotFoundException("Error while fetching user profile with the userId: " + authorId, e);
            }
        }

        var tags = new HashSet<TagEntity>();
        for (TagEntity t : artifact.getTags()) {
            var tagValue = t.getValue();
            var fetchedTag = tagRepository.findByValue(tagValue);
            if (fetchedTag == null) {
                fetchedTag = tagRepository.save(t);
            }
            tags.add(fetchedTag);
        }
        artifact.setAuthors(userSet);
        artifact.setTags(tags);
        artifact.setState(ArtifactState.ACTIVE);
    }

    private List<Class<? extends ResearchArtifactEntity>> resolveEntityTypes(List<ArtifactType> types) {
        var typeList = new ArrayList<Class<? extends ResearchArtifactEntity>>();
        for (ArtifactType artifactType : types) {
            if (artifactType == ArtifactType.REPOSITORY) {
                typeList.add(RepositoryArtifactEntity.class);
                typeList.add(NotebookArtifactEntity.class);
                typeList.add(ModelArtifactEntity.class);
            } else if (artifactType == ArtifactType.DATASET) {
                typeList.add(DatasetArtifactEntity.class);
            }
        }
        return typeList;
    }

    private Class<? extends ResearchArtifactEntity> resolveEntityType(ArtifactType type) {
        return switch (type) {
            case REPOSITORY -> RepositoryArtifactEntity.class;
            case DATASET -> DatasetArtifactEntity.class;
        };
    }

    private Page<ResearchArtifactEntity> getAllPublicArtifacts(
            int pageNumber,
            int pageSize,
            List<Class<? extends ResearchArtifactEntity>> typeList,
            String[] tag,
            String nameSearch) {
        var pageable = PageRequest.of(pageNumber, pageSize);
        if (tag == null || tag.length == 0) {
            return artifactRepository.findAllByTypes(typeList, nameSearch, pageable);
        }

        return artifactRepository.findAllByTypesAndAllTags(
                typeList, tag, tag.length, nameSearch.toLowerCase(), pageable);
    }

    private Page<ResearchArtifactEntity> getAllArtifactsUserSignedIn(
            int pageNumber,
            int pageSize,
            List<Class<? extends ResearchArtifactEntity>> typeList,
            String[] tag,
            String nameSearch,
            String userId) {
        var pageable = PageRequest.of(pageNumber, pageSize);

        if (tag == null || tag.length == 0) {
            return artifactRepository.findAllByTypesForUser(typeList, nameSearch.toLowerCase(), userId, pageable);
        }

        return artifactRepository.findAllByTypesAndAllTagsForUser(
                typeList, tag, (long) tag.length, nameSearch.toLowerCase(), userId, pageable);
    }
}
