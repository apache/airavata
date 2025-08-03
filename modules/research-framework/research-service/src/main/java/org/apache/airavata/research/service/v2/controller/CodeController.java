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
package org.apache.airavata.research.service.v2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.apache.airavata.research.service.enums.PrivacyEnum;
import org.apache.airavata.research.service.enums.StateEnum;
import org.apache.airavata.research.service.v2.entity.Code;
import org.apache.airavata.research.service.v2.repository.CodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v2/rf/codes")
@Tag(name = "Code Resources V2", description = "V2 API for managing code resources (models, notebooks, repositories)")
public class CodeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeController.class);
    private static final PrivacyEnum PUBLIC_PRIVACY = PrivacyEnum.PUBLIC;
    private static final StateEnum ACTIVE_STATE = StateEnum.ACTIVE;

    private final CodeRepository codeRepository;

    public CodeController(CodeRepository codeRepository) {
        this.codeRepository = codeRepository;
    }

    @Operation(summary = "Get all codes with pagination")
    @GetMapping("/")
    @PreAuthorize("hasRole('USER') or hasRole('API_USER')")
    public ResponseEntity<Page<Code>> getCodes(
            @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "codeType", required = false) String codeType,
            @RequestParam(value = "programmingLanguage", required = false) String programmingLanguage) {
        
        LOGGER.info("Getting codes - page: {}, size: {}, keyword: {}, type: {}, language: {}", 
                   pageNumber, pageSize, keyword, codeType, programmingLanguage);
        
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());
        Page<Code> codes;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            codes = codeRepository.findByKeywordSearchAndPrivacyAndState(keyword, PUBLIC_PRIVACY, ACTIVE_STATE, pageable);
        } else {
            codes = codeRepository.findByPrivacyAndState(PUBLIC_PRIVACY, ACTIVE_STATE, pageable);
        }
        
        LOGGER.info("Found {} codes", codes.getTotalElements());
        return ResponseEntity.ok(codes);
    }

    @Operation(summary = "Get code by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('API_USER')")
    public ResponseEntity<Code> getCodeById(@PathVariable("id") String id) {
        LOGGER.info("Getting code by ID: {}", id);
        
        Optional<Code> code = codeRepository.findById(id);
        if (code.isPresent()) {
            return ResponseEntity.ok(code.get());
        } else {
            LOGGER.warn("Code not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Create new code")
    @PostMapping("/")
    public ResponseEntity<?> createCode(@Valid @RequestBody Code code, BindingResult bindingResult) {
        LOGGER.info("Creating new code: {}", code.getName());
        
        // Validation error handling
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("Validation failed");
            LOGGER.error("Validation errors: {}", errorMessage);
            return ResponseEntity.badRequest().body("Validation failed: " + errorMessage);
        }
        
        try {
            // Set default values using enums
            if (code.getPrivacy() == null) {
                code.setPrivacy(PUBLIC_PRIVACY);
            }
            if (code.getState() == null) {
                code.setState(ACTIVE_STATE);
            }
            // Note: starCount functionality handled separately in v1 star system
            
            Code savedCode = codeRepository.save(code);
            LOGGER.info("Created code with ID: {}", savedCode.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCode);
        } catch (Exception e) {
            LOGGER.error("Error creating code: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating code: " + e.getMessage());
        }
    }

    @Operation(summary = "Update code")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCode(@PathVariable("id") String id, @Valid @RequestBody Code code, BindingResult bindingResult) {
        LOGGER.info("Updating code with ID: {}", id);
        
        // Validation error handling
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("Validation failed");
            LOGGER.error("Validation errors: {}", errorMessage);
            return ResponseEntity.badRequest().body("Validation failed: " + errorMessage);
        }
        
        try {
            Optional<Code> existingCode = codeRepository.findById(id);
            if (!existingCode.isPresent()) {
                LOGGER.warn("Code not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            // Set the ID to ensure we update the correct code
            code.setId(id);
            
            // Preserve creation timestamp
            code.setCreatedAt(existingCode.get().getCreatedAt());
            
            Code updatedCode = codeRepository.save(code);
            LOGGER.info("Successfully updated code with ID: {}", id);
            
            return ResponseEntity.ok(updatedCode);
        } catch (Exception e) {
            LOGGER.error("Error updating code with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating code: " + e.getMessage());
        }
    }
    
    @Operation(summary = "Delete code")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCode(@PathVariable("id") String id) {
        LOGGER.info("Deleting code with ID: {}", id);
        
        try {
            Optional<Code> existingCode = codeRepository.findById(id);
            if (!existingCode.isPresent()) {
                LOGGER.warn("Code not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            codeRepository.deleteById(id);
            LOGGER.info("Successfully deleted code with ID: {}", id);
            return ResponseEntity.ok().body("Code deleted successfully");
        } catch (Exception e) {
            LOGGER.error("Error deleting code with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting code: " + e.getMessage());
        }
    }

    @Operation(summary = "Search codes by keyword")
    @GetMapping("/search")
    public ResponseEntity<List<Code>> searchCodes(
            @RequestParam(value = "keyword") String keyword) {
        
        LOGGER.info("Searching codes with keyword: {}", keyword);
        
        List<Code> codes = codeRepository.findByKeywordSearchAndPrivacyAndState(keyword, PUBLIC_PRIVACY, ACTIVE_STATE);
        
        LOGGER.info("Found {} codes matching keyword: {}", codes.size(), keyword);
        return ResponseEntity.ok(codes);
    }

    @Operation(summary = "Get codes by type")
    @GetMapping("/type/{codeType}")
    public ResponseEntity<List<Code>> getCodesByType(
            @PathVariable("codeType") String codeType) {
        
        LOGGER.info("Getting codes by type: {}", codeType);
        
        List<Code> codes = codeRepository.findByCodeTypeAndPrivacyAndState(codeType, PUBLIC_PRIVACY, ACTIVE_STATE);
        
        LOGGER.info("Found {} codes of type: {}", codes.size(), codeType);
        return ResponseEntity.ok(codes);
    }

    @Operation(summary = "Get codes by programming language")
    @GetMapping("/language/{programmingLanguage}")
    public ResponseEntity<List<Code>> getCodesByLanguage(
            @PathVariable("programmingLanguage") String programmingLanguage) {
        
        LOGGER.info("Getting codes by programming language: {}", programmingLanguage);
        
        List<Code> codes = codeRepository.findByProgrammingLanguageAndPrivacyAndState(programmingLanguage, PUBLIC_PRIVACY, ACTIVE_STATE);
        
        LOGGER.info("Found {} codes for language: {}", codes.size(), programmingLanguage);
        return ResponseEntity.ok(codes);
    }

    @Operation(summary = "Get codes by framework")
    @GetMapping("/framework/{framework}")
    public ResponseEntity<List<Code>> getCodesByFramework(
            @PathVariable("framework") String framework) {
        
        LOGGER.info("Getting codes by framework: {}", framework);
        
        List<Code> codes = codeRepository.findByFrameworkAndPrivacyAndState(framework, PUBLIC_PRIVACY, ACTIVE_STATE);
        
        LOGGER.info("Found {} codes for framework: {}", codes.size(), framework);
        return ResponseEntity.ok(codes);
    }

    @Operation(summary = "Get codes by tag")
    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<Code>> getCodesByTag(
            @PathVariable("tag") String tag) {
        
        LOGGER.info("Getting codes by tag: {}", tag);
        
        List<Code> codes = codeRepository.findByTagAndPrivacyAndState(tag, PUBLIC_PRIVACY, ACTIVE_STATE);
        
        LOGGER.info("Found {} codes with tag: {}", codes.size(), tag);
        return ResponseEntity.ok(codes);
    }

    @Operation(summary = "Get codes by author")
    @GetMapping("/author/{author}")
    public ResponseEntity<List<Code>> getCodesByAuthor(
            @PathVariable("author") String author) {
        
        LOGGER.info("Getting codes by author: {}", author);
        
        List<Code> codes = codeRepository.findByAuthorAndPrivacyAndState(author, PUBLIC_PRIVACY, ACTIVE_STATE);
        
        LOGGER.info("Found {} codes by author: {}", codes.size(), author);
        return ResponseEntity.ok(codes);
    }

    @Operation(summary = "Get top starred codes")
    @GetMapping("/top-starred")
    public ResponseEntity<List<Code>> getTopStarredCodes(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        LOGGER.info("Getting top {} starred codes", limit);
        
        Pageable pageable = PageRequest.of(0, limit);
        List<Code> codes = codeRepository.findTopStarredCodes(PUBLIC_PRIVACY, ACTIVE_STATE, pageable);
        
        LOGGER.info("Found {} top starred codes", codes.size());
        return ResponseEntity.ok(codes);
    }

    @Operation(summary = "Get recent codes")
    @GetMapping("/recent")
    public ResponseEntity<List<Code>> getRecentCodes(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        LOGGER.info("Getting {} recent codes", limit);
        
        Pageable pageable = PageRequest.of(0, limit);
        List<Code> codes = codeRepository.findRecentCodes(PUBLIC_PRIVACY, ACTIVE_STATE, pageable);
        
        LOGGER.info("Found {} recent codes", codes.size());
        return ResponseEntity.ok(codes);
    }

    @Operation(summary = "Star/unstar a code")
    @PostMapping("/{id}/star")
    public ResponseEntity<Boolean> starCode(@PathVariable("id") String id) {
        LOGGER.info("Toggling star for code with ID: {}", id);
        
        try {
            Optional<Code> codeOpt = codeRepository.findById(id);
            if (codeOpt.isPresent()) {
                Code code = codeOpt.get();
                
                // TODO: Implement proper v1 ResourceStar system integration
                // For now, return simple toggle response
                LOGGER.info("Star toggle requested for code: {} (simplified implementation)", id);
                return ResponseEntity.ok(true);
            } else {
                LOGGER.warn("Code not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            LOGGER.error("Error toggling code star: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Check if user starred a code")
    @GetMapping("/{id}/star")
    public ResponseEntity<Boolean> checkCodeStarred(@PathVariable("id") String id) {
        LOGGER.info("Checking if code is starred: {}", id);
        
        try {
            Optional<Code> codeOpt = codeRepository.findById(id);
            if (codeOpt.isPresent()) {
                Code code = codeOpt.get();
                // TODO: Implement proper v1 ResourceStar system integration
                LOGGER.info("Star status check for code: {} (simplified implementation)", id);
                return ResponseEntity.ok(false);
            } else {
                LOGGER.warn("Code not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            LOGGER.error("Error checking code star status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get code star count")
    @GetMapping("/{id}/stars/count")
    public ResponseEntity<Integer> getCodeStarCount(@PathVariable("id") String id) {
        LOGGER.info("Getting star count for code: {}", id);
        
        try {
            Optional<Code> codeOpt = codeRepository.findById(id);
            if (codeOpt.isPresent()) {
                // TODO: Implement proper v1 ResourceStar system integration
                return ResponseEntity.ok(0);
            } else {
                LOGGER.warn("Code not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            LOGGER.error("Error getting star count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get all starred codes")
    @GetMapping("/starred")
    public ResponseEntity<Page<Code>> getStarredCodes(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        LOGGER.info("Fetching starred codes - page: {}, size: {}", page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            // TODO: Implement proper v1 ResourceStar system integration
            // For now, return empty page
            Page<Code> starredCodes = codeRepository.findByPrivacyAndState(PUBLIC_PRIVACY, ACTIVE_STATE, pageable);
            // Filter to empty for now until proper star system is implemented
            starredCodes = Page.empty();
            LOGGER.info("Found {} starred codes", starredCodes.getTotalElements());
            return ResponseEntity.ok(starredCodes);
        } catch (Exception e) {
            LOGGER.error("Error fetching starred codes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}