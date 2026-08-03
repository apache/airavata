package org.apache.airavata.iam.service;

import java.util.List;

import org.apache.airavata.iam.dto.UserRegistrationDto;
import org.apache.airavata.iam.dto.UserResponseDto;
import org.apache.airavata.iam.mapper.UserMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.apache.airavata.iam.model.UserEntity;
import org.apache.airavata.iam.model.enums.UserStatus;
import org.apache.airavata.iam.repository.UserRepository;

@Service
public class UserService {

    private UserMapper userMapper;
    private UserRepository userRepository;

    public UserService(UserMapper userMapper, UserRepository userRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    public UserResponseDto registerUser(UserRegistrationDto userRegistrationDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean hasSuperAdminRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("SUPER_ADMIN"));

        if (!hasSuperAdminRole) {
            throw new AccessDeniedException("Only Super Admin can register users");
        }

        UserEntity userEntity = userMapper.toEntity(userRegistrationDto);
        userEntity.setStatus(UserStatus.ACTIVE); // Default status
        userEntity.setCreatedAt(System.currentTimeMillis()); // Set creation timestamp
        userRepository.save(userEntity);

        UserResponseDto responseDto = userMapper.toResponseDto(userEntity);
        return responseDto;
    }

    public UserResponseDto getUserById(String userId) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean hasSuperAdminRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("SUPER_ADMIN") || auth.getAuthority().equals("ADMIN"));

        boolean isSelf = authentication.getName().equals(userId);

        if (!hasSuperAdminRole && !isSelf) {
            throw new AccessDeniedException("Access denied: You can only access your own user information");
        }

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found with ID: " + userId));
        return userMapper.toResponseDto(userEntity);
    }

    public java.util.List<UserResponseDto> getAllUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean hasSuperAdminRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("SUPER_ADMIN")
                        || auth.getAuthority().equals("ADMIN"));

        if (!hasSuperAdminRole) {
            throw new AccessDeniedException("Only Super Admin can access all users");
        }

        List<UserEntity> userEntities = new java.util.ArrayList<>();
        userRepository.findAll().forEach(userEntities::add);
        return userEntities.stream()
                .map(userMapper::toResponseDto)
                .collect(java.util.stream.Collectors.toList());
    }

    public UserResponseDto updateUser(String userId, UserRegistrationDto userRegistrationDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean hasSuperAdminRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("SUPER_ADMIN"));

        boolean isSelf = authentication.getName().equals(userId);

        if (!hasSuperAdminRole && !isSelf) {
            throw new AccessDeniedException("Access denied: You can only update your own user information");
        }

        UserEntity existingUserEntity = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Update fields from the DTO
        existingUserEntity.setEmail(userRegistrationDto.getEmail());
        existingUserEntity.setFirstName(userRegistrationDto.getFirstName());
        existingUserEntity.setLastName(userRegistrationDto.getLastName());

        // Save the updated entity
        userRepository.save(existingUserEntity);

        return userMapper.toResponseDto(existingUserEntity);
    }

}
