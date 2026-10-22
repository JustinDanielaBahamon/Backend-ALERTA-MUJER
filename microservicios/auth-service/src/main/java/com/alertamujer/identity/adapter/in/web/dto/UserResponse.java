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
    /** Record of acceptance of the terms (null on accounts older than this record). */
    private LocalDateTime termsAcceptedAt;
    private String termsVersion;
    
    /** Person with her photo (lives in user_profile; null if not requested or missing). */
    public static UserResponse of(User u, String photo) {
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
                .profilePhoto(photo)
                .createdAt(u.getCreatedAt())
                .termsAcceptedAt(u.getTermsAcceptedAt())
                .termsVersion(u.getTermsVersion())
                .build();
    }

    public String getFullName() {
        if (firstName == null && lastName == null) return "";
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}
