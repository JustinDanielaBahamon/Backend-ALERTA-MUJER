package com.alertamujer.identity.application.port.in;

import com.alertamujer.identity.domain.model.UserChanges;
import com.alertamujer.identity.domain.model.User;

/** The signed-in person queries and changes her data. */
public interface MyAccountUseCase {

    UserWithPhoto mine(Long userId);

    UserWithPhoto update(Long userId, UserChanges changes);

    /** Requires the current password to prevent changes from someone else's session. */
    void changePassword(Long userId, String current, String newOne);

    /**
     * The user herself deletes her account by typing the confirmation word. Admin accounts
     * cannot: they need the approval of another admin from the panel.
     */
    void deleteMyAccount(Long userId, String confirmation);

    /** Word that must be typed to confirm the deletion. */
    String DELETE_CONFIRMATION = "ELIMINAR";

    record UserWithPhoto(User user, String photo) {}
}
