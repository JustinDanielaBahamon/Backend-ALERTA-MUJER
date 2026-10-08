package com.alertamujer.identity.infrastructure.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String telephone;
    private Long roleId;
    private String roleName;
    private String documentNumber;
    private String documentType;
    private String birthdate;
    private LocalDateTime createdAt;
    
    public String getFullName() {
        if (firstName == null && lastName == null) return "";
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}