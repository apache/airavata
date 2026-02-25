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
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.research.artifact.dto.ArtifactResponse;
import org.apache.airavata.research.artifact.dto.CreateArtifactRequest;
import org.apache.airavata.research.artifact.dto.ModifyArtifactRequest;
import org.apache.airavata.research.artifact.entity.ArtifactStarEntity;
import org.apache.airavata.research.artifact.entity.RepositoryArtifactEntity;
import org.apache.airavata.research.artifact.entity.ResearchArtifactEntity;
import org.apache.airavata.research.artifact.entity.TagEntity;
import org.apache.airavata.research.artifact.model.ArtifactState;
import org.apache.airavata.research.artifact.model.ArtifactStatus;
import org.apache.airavata.research.artifact.model.ArtifactType;
import org.apache.airavata.research.artifact.model.Privacy;
import org.apache.airavata.research.artifact.repository.ArtifactStarRepository;
import org.apache.airavata.research.artifact.repository.ResearchArtifactRepository;
import org.apache.airavata.research.artifact.repository.TagRepository;
import org.apache.airavata.iam.model.UserContext;
import org.apache.airavata.research.project.repository.ResearchProjectRepository;
import org.apache.airavata.iam.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class DefaultArtifactService implements ArtifactService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultArtifactService.class);

    private final UserService userProfileService;
    private final TagRepository tagRepository;
    private final ResearchArtifactRepository artifactRepository;
    private final ArtifactStarRepository artifactStarRepository;

    public DefaultArtifactService(
            UserService userProfileService,
            TagRepository tagRepository,
            ResearchArtifactRepository artifactRepository,
            @Qualifier("researchProjectRepository") ResearchProjectRepository projectRepository,
            ArtifactStarRepository artifactStarRepository) {
        this.userProfileService = userProfileService;
        this.tagRepository = tagRepository;
        this.artifactRepository = artifactRepository;
        this.artifactStarRepository = artifactStarRepository;
    }

    @Override
    public void initializeArtifact(ResearchArtifactEntity artifact) {
        var userSet = new HashSet<String>();
        for (String authorId : artifact.getAuthors()) {
            try {
                var fetchedUser = userProfileService.getUserProfileById(
                        UserContext.authzToken(), authorId, UserContext.gatewayId());
                userSet.add(fetchedUser.getUserId());
            } catch (Exception e) {
                LOGGER.error("Error while fetching user profile with the userId: {}", authorId, e);
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

    @Override
    public ArtifactResponse createArtifact(ResearchArtifactEntity artifact, ArtifactType type) {
        var response = new ArtifactResponse();

        initializeArtifact(artifact);
        response.setArtifact(artifactRepository.save(artifact));
        response.setType(type);

        return response;
    }

    @Override
    public void transferArtifactRequestFields(ResearchArtifactEntity artifact, CreateArtifactRequest createArtifactRequest) {
        // check that the logged in author is at least one of the authors making the request
        var currentUserId = UserContext.userId();
        boolean found = false;
        for (String authorId : createArtifactRequest.getAuthors()) {
            if (authorId.equalsIgnoreCase(currentUserId)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException(
                    "You cannot create an artifact on another author's behalf, without you being one of the authors");
        }

        artifact.setName(createArtifactRequest.getName());
        artifact.setDescription(createArtifactRequest.getDescription());
        artifact.setAuthors(createArtifactRequest.getAuthors().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet()));
        var tagsSet = new HashSet<TagEntity>();
        for (String tag : createArtifactRequest.getTags()) {
            var t = new TagEntity();
            t.setValue(tag);
            tagsSet.add(t);
        }
        artifact.setTags(tagsSet);
        artifact.setPrivacy(createArtifactRequest.getPrivacy());
        artifact.setStatus(ArtifactStatus.NONE);
        artifact.setHeaderImage(createArtifactRequest.getHeaderImage());
    }

    @Override
    public ArtifactResponse createRepositoryArtifact(CreateArtifactRequest artifactRequest, String repoUrl) {
        var repositoryArtifact = new RepositoryArtifactEntity();
        transferArtifactRequestFields(repositoryArtifact, artifactRequest);
        repositoryArtifact.setRepositoryUrl(repoUrl);
        return createArtifact(repositoryArtifact, ArtifactType.REPOSITORY);
    }

    @Override
    public ResearchArtifactEntity modifyArtifact(ModifyArtifactRequest artifactRequest) {
        var artifactOp = artifactRepository.findById(artifactRequest.getId());
        if (artifactOp.isEmpty()) {
            throw new EntityNotFoundException("Artifact not found");
        }

        var artifact = artifactOp.get();
        if (ArtifactState.DELETED.equals(artifact.getState())) {
            throw new RuntimeException(String.format("Cannot modify deleted artifact: %s", artifact.getId()));
        }

        // ensure that the user making the request is one of the current authors
        boolean found = false;
        for (String authorId : artifact.getAuthors()) {
            if (authorId.equalsIgnoreCase(UserContext.userId())) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException("Author is not authorized");
        }

        transferArtifactRequestFields(artifact, artifactRequest);
        initializeArtifact(artifact);

        artifactRepository.save(artifact);
        return artifact;
    }

    @Override
    public boolean starOrUnstarArtifact(String artifactId) {
        var artifact = getArtifactById(artifactId);
        var userId = UserContext.userId();

        var artifactStars = artifactStarRepository.findByArtifactAndUserId(artifact, userId);

        if (artifactStars.isEmpty()) {
            // user has not starred the artifact yet
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
        var artifact = getArtifactById(artifactId);
        var userId = UserContext.userId();

        return artifactStarRepository.existsByArtifactAndUserId(artifact, userId);
    }

    @Override
    public List<ResearchArtifactEntity> getAllStarredArtifacts(String userId) {
        var loggedInUser = UserContext.userId();
        if (!loggedInUser.equals(userId)) {
            throw new RuntimeException(
                    String.format("User %s is not authorized to request stars for %s", loggedInUser, userId));
        }

        var artifactStars = artifactStarRepository.findByUserIdAndArtifactState(loggedInUser, ArtifactState.ACTIVE);
        return artifactStars.stream().map(ArtifactStarEntity::getArtifact).collect(Collectors.toList());
    }

    @Override
    public long getArtifactStarCount(String artifactId) {
        var artifact = getArtifactById(artifactId);
        return artifactStarRepository.countArtifactStarByArtifact(artifact);
    }

    @Override
    public ResearchArtifactEntity getArtifactById(String id) {
        var opArtifact = artifactRepository.findByIdAndState(id, ArtifactState.ACTIVE);

        if (opArtifact.isEmpty()) {
            throw new EntityNotFoundException("Artifact not found: " + id);
        }

        var artifact = opArtifact.get();
        var isAuthenticated = UserContext.isAuthenticated();

        if (artifact.getPrivacy().equals(Privacy.PUBLIC)) {
            return artifact;
        } else if (isAuthenticated
                && artifact.getAuthors().contains(UserContext.userId().toLowerCase())) {
            return artifact;
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
            LOGGER.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        artifact.setState(ArtifactState.DELETED);
        artifactRepository.save(artifact);
        return true;
    }

    @Override
    public Page<ResearchArtifactEntity> getAllArtifacts(
            int pageNumber,
            int pageSize,
            List<Class<? extends ResearchArtifactEntity>> typeList,
            String[] tag,
            String nameSearch) {
        var isAuthenticated = UserContext.isAuthenticated();
        if (isAuthenticated) {
            return getAllArtifactsUserSignedIn(pageNumber, pageSize, typeList, tag, nameSearch, UserContext.userId());
        }
        return getAllPublicArtifacts(pageNumber, pageSize, typeList, tag, nameSearch);
    }

    @Override
    public List<TagEntity> getAllTags() {
        return tagRepository.findAll();
    }

    @Override
    public List<TagEntity> getAllTagsByPopularity() {
        return tagRepository.findDistinctByPopularity(100);
    }

    @Override
    public List<TagEntity> getAllTagsByAlphabeticalOrder() {
        return tagRepository.findAll(Sort.by(Sort.Direction.ASC, "value"));
    }

    @Override
    public List<ResearchArtifactEntity> getAllArtifactsByTypeAndName(Class<? extends ResearchArtifactEntity> type, String name) {
        return artifactRepository.findByTypeAndNameContainingIgnoreCase(type, name.toLowerCase(), UserContext.userId());
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
