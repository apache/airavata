package org.apache.airavata.admin_api_server.controller;

import org.apache.airavata.admin_api_server.entity.Repository;
import org.apache.airavata.admin_api_server.repository.RepositoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/repositories")
public class RepositoryController {

    @Autowired
    private RepositoryRepository repositoryRepository;

    @GetMapping
    public ResponseEntity<List<Repository>> getAllRepositories() {
        List<Repository> repositories = repositoryRepository.findAll();
        return ResponseEntity.ok(repositories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Repository> getRepositoryById(@PathVariable Long id) {
        Optional<Repository> repository = repositoryRepository.findById(id);
        return repository.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Repository> createRepository(@RequestBody Repository repository) {
        Repository savedRepository = repositoryRepository.save(repository);
        return ResponseEntity.ok(savedRepository);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Repository> updateRepository(@PathVariable Long id, @RequestBody Repository repositoryDetails) {
        Optional<Repository> optionalRepository = repositoryRepository.findById(id);
        if (optionalRepository.isPresent()) {
            Repository repository = optionalRepository.get();
            repository.setTitle(repositoryDetails.getTitle());
            repository.setDescription(repositoryDetails.getDescription());
            repository.setTags(repositoryDetails.getTags());
            repository.setAuthors(repositoryDetails.getAuthors());
            repository.setCategory(repositoryDetails.getCategory());
            repository.setStarCount(repositoryDetails.getStarCount());
            Repository updatedRepository = repositoryRepository.save(repository);
            return ResponseEntity.ok(updatedRepository);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepository(@PathVariable Long id) {
        if (repositoryRepository.existsById(id)) {
            repositoryRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/search")
    public ResponseEntity<List<Repository>> searchRepositories(@RequestParam String keyword) {
        try {
            System.out.println("Searching repositories with keyword: " + keyword);
            List<Repository> repositories = repositoryRepository.findByKeyword(keyword);
            System.out.println("Found " + repositories.size() + " repositories");
            return ResponseEntity.ok(repositories);
        } catch (Exception e) {
            System.err.println("Error searching repositories: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}