package org.apache.airavata.research.service.handlers;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.airavata.research.service.model.repo.UserRepository;

import org.apache.airavata.research.service.model.entity.User;

import org.apache.airavata.research.CreateUserRequest;

import java.util.Date;

@Service
public class UserHandler {
    @Autowired
    private UserRepository userRepository;


    public User createUser(CreateUserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        User user = new User();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        Date date = new Date(); // same date for createdAt and updatedAt
        user.setCreatedAt(date);
        user.setUpdatedAt(date);
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

}
