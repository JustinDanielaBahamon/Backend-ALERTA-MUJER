package com.alertamujer.identity.application.service;

import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import com.alertamujer.shared.web.FotoDataUri;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public UserService(UserRepository userRepository, UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    /** Foto de perfil de la usuaria (data URI) o null. */
    public String getProfilePhoto(Long userId) {
        return userProfileRepository.findByUserId(userId).map(UserProfile::getProfilePhotoUrl).orElse(null);
    }

    public User getMe(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateMe(Long userId, Map<String, Object> updates) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updates.containsKey("firstName")) {
            user.setFirstName((String) updates.get("firstName"));
        }
        if (updates.containsKey("lastName")) {
            user.setLastName((String) updates.get("lastName"));
        }
        if (updates.containsKey("telephone")) {
            user.setTelephone((String) updates.get("telephone"));
        }
        if (updates.containsKey("documentNumber")) {
            user.setDocumentNumber((String) updates.get("documentNumber"));
        }
        if (updates.containsKey("documentType")) {
            user.setDocumentType((String) updates.get("documentType"));
        }
        if (updates.containsKey("birthdate") && updates.get("birthdate") != null) {
            Object raw = updates.get("birthdate");
            try {
                user.setBirthdate(java.time.LocalDate.parse(String.valueOf(raw)));
            } catch (Exception ignored) {
                // Formato inválido: se ignora el valor de birthdate
            }
        }

        // Foto de perfil: "" o null la quitan
        if (updates.containsKey("profilePhoto")) {
            Object foto = updates.get("profilePhoto");
            String valida = FotoDataUri.validar(foto == null ? null : String.valueOf(foto));
            userProfileRepository.findByUserId(userId).ifPresent(perfil -> {
                perfil.setProfilePhotoUrl(valida);
                userProfileRepository.save(perfil);
            });
        }

        return userRepository.save(user);
    }
}
