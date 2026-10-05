package com.alertamujer.identity.application.service;

import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        return userRepository.save(user);
    }
}
