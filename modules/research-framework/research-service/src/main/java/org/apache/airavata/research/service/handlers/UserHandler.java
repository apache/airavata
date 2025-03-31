/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.research.service.handlers;

import jakarta.persistence.EntityNotFoundException;
import org.apache.airavata.research.CreateUserRequest;
import org.apache.airavata.research.service.model.entity.User;
import org.apache.airavata.research.service.model.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserHandler.class);

    private final UserRepository userRepository;

    public UserHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(CreateUserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        User user = new User();
        user.setUsername(userRequest.getUserName());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setAvatar(userRequest.getAvatar());

        return userRepository.save(user);
    }


    public User getUserFromId(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found with id: " + userId)
        );
    }

    public User getUserFromEmail(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new RuntimeException("User not found with email: " + email);
        }
        return userRepository.findByEmail(email);
    }

    public User findUserByUsername(String username) {
        return userRepository.findById(username).orElseThrow(() -> {
            LOGGER.error("Unable to find a User with a username: " + username);
            return new EntityNotFoundException("Unable to find a User with a username: " + username);
        });
    }

    public User initializeOrGetUser(String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    LOGGER.debug("User {} is already registered.", username);
                    return user;
                })
                .orElseGet(() -> {
                    // TODO: Initialize this using the Airavata user profile if needed.
                    User newUser = new User(username, "CHANGE_ME", "CHANGE_ME", username);
                    newUser = userRepository.save(newUser);
                    LOGGER.info("Initialized new user with username {}.", username);
                    return newUser;
                });
    }
}
