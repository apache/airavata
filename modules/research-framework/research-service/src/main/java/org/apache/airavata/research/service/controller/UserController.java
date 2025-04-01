package org.apache.airavata.research.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.airavata.research.service.handlers.UserHandler;

import org.apache.airavata.research.service.model.entity.User;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import org.springframework.web.bind.annotation.RequestBody;


import org.apache.airavata.research.CreateUserRequest;

@RestController
@RequestMapping("/api/v1/rf/user-management")
@Tag(name = "User Management", description = "Endpoints for managing users")
public class UserController {
    @Autowired
    private UserHandler userService;

    @Operation(
            summary = "Create a new user",
            description = "Adds a new user to the system"
    )
    @PostMapping(value = "/users/{email}")
    public ResponseEntity<Object> addUser(
            @PathVariable("email") String email,
            @RequestParam(value="first name") String firstName,
            @RequestParam(value="last name") String lastName,
            @RequestParam(value="avatar") String avatar
    ) {
        try {
            CreateUserRequest userRequest = CreateUserRequest.newBuilder()
                    .setUserName(email)
                    .setEmail(email)
                    .setFirstName(firstName)
                    .setLastName(lastName)
                    .setAvatar(avatar)
                    .build();
            User savedUser = userService.createUser(userRequest);
            return ResponseEntity.ok(savedUser);
        } catch(Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Get user profile",
            description = "Get user profile"
    )
    @GetMapping(value = "/users/{userIdOrEmail}")
    public ResponseEntity<Object> getUser(
            @PathVariable("userIdOrEmail") String userIdOrEmail
    ) {
        try {
            User user = null;
            if (userIdOrEmail.contains("@")) {
                user = userService.getUserFromEmail(userIdOrEmail);
            } else {
                user = userService.getUserFromId(userIdOrEmail);
            }
            return ResponseEntity.ok(user);
        } catch(Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
