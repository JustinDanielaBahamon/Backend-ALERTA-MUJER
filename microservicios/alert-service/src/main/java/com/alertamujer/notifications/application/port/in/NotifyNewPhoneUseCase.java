package com.alertamujer.notifications.application.port.in;

import java.util.List;

/** Avisa a los otros celulares de la usuaria que se entró a su cuenta desde un celular nuevo. */
public interface NotifyNewPhoneUseCase {

    void notify(List<String> otherPhoneTokens, String phone);
}
