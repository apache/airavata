package org.apache.airavata.iam.mapper;

import org.apache.airavata.iam.dto.UserRegistrationDto;
import org.apache.airavata.iam.dto.UserResponseDto;
import org.apache.airavata.iam.model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "status", ignore = true, defaultValue = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true, defaultExpression = "java(System.currentTimeMillis())")
    @Mapping(target = "roles", ignore = true, defaultValue = "new java.util.ArrayList<>()")
    UserEntity toEntity(UserRegistrationDto userRegistrationDto);

    UserResponseDto toResponseDto(UserEntity userEntity);

}
