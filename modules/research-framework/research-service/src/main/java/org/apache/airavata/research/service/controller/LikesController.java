package org.apache.airavata.research.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.airavata.research.service.handlers.ResourceHandler;
import org.apache.airavata.research.service.model.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rf/likes")
@Tag(name = "Likes", description = "likes resources")
public class LikesController {

    @Autowired
    private ResourceHandler resourceHandler;

    @Operation(summary = "Like/unlike a resource")
    @PostMapping(value = "/resources/{id}")
    public ResponseEntity<Boolean> likeResource(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(resourceHandler.likeOrUnlikeResource(id));
    }

    @Operation(summary = "Check whether a user liked a resource")
    @GetMapping(value = "/resources/{id}")
    public ResponseEntity<Boolean> getLikedResource(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(resourceHandler.checkWhetherUserLikedResource(id));
    }

    @Operation(summary = "Check whether a user liked a resource")
    @GetMapping(value = "/resources/{id}/count")
    public ResponseEntity<Long> getResourceLikeCount(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok(resourceHandler.getResourceLikeCount(id));
    }

    @Operation(summary = "Get all liked resources of a user")
    @GetMapping(value = "/users/{userId}/resources")
    public ResponseEntity<List<Resource>> getAllLikedResources(@PathVariable(value = "userId") String id) {
        return ResponseEntity.ok(resourceHandler.getAllLikedResources(id));
    }

}
