package com.alertamujer.devices.api;

import java.util.List;

/**
 * Someone signed in to an account from a new phone. Carries the push tokens of the user's other
 * phones to warn them, in case it was not her.
 *
 * @param phone name of the new phone ("Galaxy de Ana", "Samsung A54"...)
 */
public record NewPhoneSignedInEvent(String phone, List<String> otherTokens) {}
