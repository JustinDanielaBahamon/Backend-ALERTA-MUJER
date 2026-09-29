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

        if (updates.containsKey("nombre")) {
            user.setNombre((String) updates.get("nombre"));
        }
        if (updates.containsKey("telefono")) {
            user.setTelefono((String) updates.get("telefono"));
        }
        if (updates.containsKey("avatarColor")) {
            user.setAvatarColor((String) updates.get("avatarColor"));
        }
        if (updates.containsKey("contactoEmergencia")) {
            user.setContactoEmergencia((String) updates.get("contactoEmergencia"));
        }

        return userRepository.save(user);
    }
}
