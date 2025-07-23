package org.apache.airavata.admin_api_server.controller;

import org.apache.airavata.admin_api_server.entity.Notebook;
import org.apache.airavata.admin_api_server.repository.NotebookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notebooks")
public class NotebookController {

    @Autowired
    private NotebookRepository notebookRepository;

    @GetMapping
    public ResponseEntity<List<Notebook>> getAllNotebooks() {
        List<Notebook> notebooks = notebookRepository.findAll();
        return ResponseEntity.ok(notebooks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notebook> getNotebookById(@PathVariable Long id) {
        Optional<Notebook> notebook = notebookRepository.findById(id);
        return notebook.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Notebook> createNotebook(@RequestBody Notebook notebook) {
        Notebook savedNotebook = notebookRepository.save(notebook);
        return ResponseEntity.ok(savedNotebook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notebook> updateNotebook(@PathVariable Long id, @RequestBody Notebook notebookDetails) {
        Optional<Notebook> optionalNotebook = notebookRepository.findById(id);
        if (optionalNotebook.isPresent()) {
            Notebook notebook = optionalNotebook.get();
            notebook.setTitle(notebookDetails.getTitle());
            notebook.setDescription(notebookDetails.getDescription());
            notebook.setTags(notebookDetails.getTags());
            notebook.setAuthors(notebookDetails.getAuthors());
            notebook.setCategory(notebookDetails.getCategory());
            notebook.setStarCount(notebookDetails.getStarCount());
            Notebook updatedNotebook = notebookRepository.save(notebook);
            return ResponseEntity.ok(updatedNotebook);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotebook(@PathVariable Long id) {
        if (notebookRepository.existsById(id)) {
            notebookRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/search")
    public ResponseEntity<List<Notebook>> searchNotebooks(@RequestParam String keyword) {
        try {
            System.out.println("Searching notebooks with keyword: " + keyword);
            List<Notebook> notebooks = notebookRepository.findByKeyword(keyword);
            System.out.println("Found " + notebooks.size() + " notebooks");
            return ResponseEntity.ok(notebooks);
        } catch (Exception e) {
            System.err.println("Error searching notebooks: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}