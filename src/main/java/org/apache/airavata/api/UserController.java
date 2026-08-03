package org.apache.airavata.api;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.apache.airavata.iam.dto.UserResponseDto;

import java.util.List;

import org.apache.airavata.iam.dto.UserRegistrationDto;
import org.apache.airavata.iam.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserResponseDto createNewUser(@RequestBody UserRegistrationDto userRegistrationDto) {
        return userService.registerUser(userRegistrationDto);
    }

    @GetMapping("/{userId}")
    public UserResponseDto getUserById(@PathVariable String userId) {
        return userService.getUserById(userId);
    }

    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/{userId}")
    public UserResponseDto updateUser(@PathVariable String userId,
            @RequestBody UserRegistrationDto userRegistrationDto) {
        return userService.updateUser(userId, userRegistrationDto);
    }
}
