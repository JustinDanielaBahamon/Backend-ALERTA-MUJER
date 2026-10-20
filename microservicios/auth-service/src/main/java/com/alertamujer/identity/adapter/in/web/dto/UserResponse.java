package com.alertamujer.identity.adapter.in.web.dto;

import com.alertamujer.identity.domain.model.User;
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
    /** Foto de perfil (data URI) o null. */
    private String profilePhoto;
    private LocalDateTime createdAt;
    
    /** Persona con su foto (vive en user_profile; null si no se pide o no tiene). */
    public static UserResponse of(User u, String foto) {
        return UserResponse.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .telephone(u.getTelephone())
                .roleId(u.getRoleId())
                .roleName(u.getRoleId() == null ? null : u.esAdmin() ? "ADMINISTRATOR" : "USER")
                .documentNumber(u.getDocumentNumber())
                .documentType(u.getDocumentType())
                .birthdate(u.getBirthdate() == null ? null : u.getBirthdate().toString())
                .profilePhoto(foto)
                .createdAt(u.getCreatedAt())
                .build();
    }

    public String getFullName() {
        if (firstName == null && lastName == null) return "";
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}
