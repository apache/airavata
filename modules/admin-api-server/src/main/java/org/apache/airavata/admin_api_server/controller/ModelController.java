package org.apache.airavata.admin_api_server.controller;

import org.apache.airavata.admin_api_server.entity.Model;
import org.apache.airavata.admin_api_server.repository.ModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    @Autowired
    private ModelRepository modelRepository;

    @GetMapping
    public ResponseEntity<List<Model>> getAllModels() {
        List<Model> models = modelRepository.findAll();
        return ResponseEntity.ok(models);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Model> getModelById(@PathVariable Long id) {
        Optional<Model> model = modelRepository.findById(id);
        return model.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Model>> getModelsByCategory(@PathVariable String category) {
        List<Model> models = modelRepository.findByCategory(category);
        return ResponseEntity.ok(models);
    }

    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<Model>> getModelsByTag(@PathVariable String tag) {
        List<Model> models = modelRepository.findByTag(tag);
        return ResponseEntity.ok(models);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Model>> searchModels(@RequestParam String keyword) {
        try {
            System.out.println("Searching models with keyword: " + keyword);
            List<Model> models = modelRepository.findByKeyword(keyword);
            System.out.println("Found " + models.size() + " models");
            return ResponseEntity.ok(models);
        } catch (Exception e) {
            System.err.println("Error searching models: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Model> createModel(@RequestBody Model model) {
        try {
            // Set default values
            if (model.getStarCount() == null) {
                model.setStarCount(0);
            }
            
            // Basic validation
            if (model.getTitle() == null || model.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            System.out.println("Creating new model: " + model.getTitle());
            Model savedModel = modelRepository.save(model);
            System.out.println("Model created with ID: " + savedModel.getId());
            return ResponseEntity.ok(savedModel);
        } catch (Exception e) {
            System.err.println("Error creating model: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Model> updateModel(@PathVariable Long id, @RequestBody Model modelDetails) {
        Optional<Model> optionalModel = modelRepository.findById(id);
        if (optionalModel.isPresent()) {
            Model model = optionalModel.get();
            model.setTitle(modelDetails.getTitle());
            model.setDescription(modelDetails.getDescription());
            model.setTags(modelDetails.getTags());
            model.setAuthors(modelDetails.getAuthors());
            model.setCategory(modelDetails.getCategory());
            model.setStarCount(modelDetails.getStarCount());
            Model updatedModel = modelRepository.save(model);
            return ResponseEntity.ok(updatedModel);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModel(@PathVariable Long id) {
        if (modelRepository.existsById(id)) {
            modelRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}