package org.apache.airavata.admin_api_server.controller;

import org.apache.airavata.admin_api_server.entity.Dataset;
import org.apache.airavata.admin_api_server.repository.DatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    @Autowired
    private DatasetRepository datasetRepository;

    @GetMapping
    public ResponseEntity<List<Dataset>> getAllDatasets() {
        List<Dataset> datasets = datasetRepository.findAll();
        return ResponseEntity.ok(datasets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dataset> getDatasetById(@PathVariable Long id) {
        Optional<Dataset> dataset = datasetRepository.findById(id);
        return dataset.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Dataset>> getDatasetsByCategory(@PathVariable String category) {
        List<Dataset> datasets = datasetRepository.findByCategory(category);
        return ResponseEntity.ok(datasets);
    }

    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<Dataset>> getDatasetsByTag(@PathVariable String tag) {
        List<Dataset> datasets = datasetRepository.findByTag(tag);
        return ResponseEntity.ok(datasets);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Dataset>> searchDatasets(@RequestParam String keyword) {
        try {
            System.out.println("Searching datasets with keyword: " + keyword);
            List<Dataset> datasets = datasetRepository.findByKeyword(keyword);
            System.out.println("Found " + datasets.size() + " datasets");
            return ResponseEntity.ok(datasets);
        } catch (Exception e) {
            System.err.println("Error searching datasets: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Dataset> createDataset(@RequestBody Dataset dataset) {
        Dataset savedDataset = datasetRepository.save(dataset);
        return ResponseEntity.ok(savedDataset);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Dataset> updateDataset(@PathVariable Long id, @RequestBody Dataset datasetDetails) {
        Optional<Dataset> optionalDataset = datasetRepository.findById(id);
        if (optionalDataset.isPresent()) {
            Dataset dataset = optionalDataset.get();
            dataset.setTitle(datasetDetails.getTitle());
            dataset.setDescription(datasetDetails.getDescription());
            dataset.setTags(datasetDetails.getTags());
            dataset.setAuthors(datasetDetails.getAuthors());
            dataset.setCategory(datasetDetails.getCategory());
            dataset.setStarCount(datasetDetails.getStarCount());
            Dataset updatedDataset = datasetRepository.save(dataset);
            return ResponseEntity.ok(updatedDataset);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDataset(@PathVariable Long id) {
        if (datasetRepository.existsById(id)) {
            datasetRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}