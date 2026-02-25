package org.apache.airavata.restapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.research.artifact.dto.ArtifactResponse;
import org.apache.airavata.research.artifact.dto.CreateArtifactRequest;
import org.apache.airavata.research.artifact.dto.ModifyArtifactRequest;
import org.apache.airavata.research.artifact.entity.DatasetArtifactEntity;
import org.apache.airavata.research.artifact.entity.ModelArtifactEntity;
import org.apache.airavata.research.artifact.entity.NotebookArtifactEntity;
import org.apache.airavata.research.artifact.entity.RepositoryArtifactEntity;
import org.apache.airavata.research.artifact.entity.ResearchArtifactEntity;
import org.apache.airavata.research.artifact.entity.TagEntity;
import org.apache.airavata.research.artifact.model.ArtifactType;
import org.apache.airavata.research.artifact.service.ArtifactService;
import org.apache.airavata.research.project.entity.ResearchProjectEntity;
import org.apache.airavata.research.project.service.ResearchProjectService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("researchArtifactController")
@RequestMapping("/api/v1/research/artifacts")
@Tag(name = "Research Artifacts", description = "Datasets, notebooks, repositories, models")
public class ResearchArtifactController {

    private final ArtifactService artifactService;
    private final ResearchProjectService projectService;

    public ResearchArtifactController(ArtifactService artifactService, ResearchProjectService projectService) {
        this.artifactService = artifactService;
        this.projectService = projectService;
    }

    @PostMapping("/dataset")
    public ResponseEntity<ArtifactResponse> createDatasetArtifact(@RequestBody DatasetArtifactEntity datasetArtifact) {
        var response = artifactService.createArtifact(datasetArtifact, ArtifactType.DATASET);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/notebook")
    public ResponseEntity<ArtifactResponse> createNotebookArtifact(
            @RequestBody NotebookArtifactEntity notebookArtifact) {
        var response = artifactService.createArtifact(notebookArtifact, ArtifactType.REPOSITORY);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/repository")
    public ResponseEntity<ArtifactResponse> createRepositoryArtifact(
            @RequestBody CreateArtifactRequest artifactRequest,
            @RequestParam(value = "githubUrl") String repositoryUrl) {
        var response = artifactService.createRepositoryArtifact(artifactRequest, repositoryUrl);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/repository")
    public ResponseEntity<ResearchArtifactEntity> modifyRepositoryArtifact(
            @RequestBody ModifyArtifactRequest artifactRequest) {
        var response = artifactService.modifyArtifact(artifactRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/model")
    public ResponseEntity<ArtifactResponse> createModelArtifact(@RequestBody ModelArtifactEntity modelArtifact) {
        var response = artifactService.createArtifact(modelArtifact, ArtifactType.REPOSITORY);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all tags")
    @GetMapping(value = "/public/tags/all")
    public ResponseEntity<List<TagEntity>> getTags() {
        return ResponseEntity.ok(artifactService.getAllTagsByAlphabeticalOrder());
    }

    @Operation(summary = "Get dataset, notebook, repository, or model")
    @GetMapping(value = "/public/{id}")
    public ResponseEntity<ResearchArtifactEntity> getArtifact(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(artifactService.getArtifactById(id));
    }

    @Operation(summary = "Delete dataset, notebook, repository, or model")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Boolean> deleteArtifact(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(artifactService.deleteArtifactById(id));
    }

    @Operation(summary = "Get all artifacts")
    @GetMapping("/public")
    public ResponseEntity<Page<ResearchArtifactEntity>> getAllArtifacts(
            @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "nameSearch") String nameSearch,
            @RequestParam(value = "type") ArtifactType[] types,
            @RequestParam(value = "tag", required = false) String[] tags) {
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

        var response = artifactService.getAllArtifacts(pageNumber, pageSize, typeList, tags, nameSearch);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get artifact by name")
    @GetMapping("/search")
    public ResponseEntity<List<ResearchArtifactEntity>> searchArtifact(
            @RequestParam(value = "type") ArtifactType type,
            @RequestParam(value = "name", required = false) String name) {

        var artifacts = artifactService.getAllArtifactsByTypeAndName(getArtifactType(type), name);
        return ResponseEntity.ok(artifacts);
    }

    @Operation(summary = "Get projects associated with an artifact")
    @GetMapping(value = "/public/{id}/projects")
    public ResponseEntity<List<ResearchProjectEntity>> getProjectsFromArtifactId(
            @PathVariable(value = "id") String id) {
        var artifact = artifactService.getArtifactById(id);
        List<ResearchProjectEntity> projects;
        if (artifact.getClass() == RepositoryArtifactEntity.class) {
            projects = projectService.findProjectsWithRepository((RepositoryArtifactEntity) artifact);
        } else if (artifact.getClass() == DatasetArtifactEntity.class) {
            projects = projectService.findProjectsContainingDataset((DatasetArtifactEntity) artifact);
        } else {
            throw new RuntimeException(
                    "Projects are only associated with repositories and datasets, and id: " + id + " is not either.");
        }

        return ResponseEntity.ok(projects);
    }

    @Operation(summary = "Star/unstar an artifact")
    @PostMapping(value = "/{id}/star")
    public ResponseEntity<Boolean> starOrUnstarArtifact(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(artifactService.starOrUnstarArtifact(id));
    }

    @Operation(summary = "Check whether a user starred an artifact")
    @GetMapping(value = "/{id}/star")
    public ResponseEntity<Boolean> checkWhetherUserStarredArtifact(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(artifactService.checkWhetherUserStarredArtifact(id));
    }

    @Operation(summary = "Get artifact star count")
    @GetMapping(value = "/artifacts/{id}/count")
    public ResponseEntity<Long> getArtifactStarCount(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(artifactService.getArtifactStarCount(id));
    }

    @Operation(summary = "Get all starred artifacts of a user")
    @GetMapping(value = "/{userId}/stars")
    public ResponseEntity<List<ResearchArtifactEntity>> getAllStarredArtifacts(
            @PathVariable(value = "userId") String id) {
        return ResponseEntity.ok(artifactService.getAllStarredArtifacts(id));
    }

    private Class<? extends ResearchArtifactEntity> getArtifactType(ArtifactType artifactType) {
        return switch (artifactType) {
            case REPOSITORY -> RepositoryArtifactEntity.class;
            case DATASET -> DatasetArtifactEntity.class;
            default -> null;
        };
    }
}
