package com.alertamujer.notifications.application.port.in;

import java.util.List;

/** Warns the user's other phones that someone signed in to her account from a new phone. */
public interface NotifyNewPhoneUseCase {

    void notify(List<String> otherPhoneTokens, String phone);
}
