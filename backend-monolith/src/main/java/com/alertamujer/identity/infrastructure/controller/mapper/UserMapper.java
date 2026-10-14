package com.alertamujer.identity.infrastructure.controller.mapper;

import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.controller.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    
    @Mapping(target = "roleId", expression = "java(user.getRole() != null ? user.getRole().getId() : null)")
    @Mapping(target = "roleName", expression = "java(getRoleName(user))")
    @Mapping(target = "birthdate", expression = "java(user.getBirthdate() != null ? user.getBirthdate().toString() : null)")
    UserResponse toResponse(User user);
    
    default String getRoleName(User user) {
        if (user == null || user.getRole() == null) return null;
        return user.getRole().getId() == 2L ? "ADMINISTRATOR" : "USER";
    }
}