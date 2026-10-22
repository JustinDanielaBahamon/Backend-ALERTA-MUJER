package com.alertamujer.devices.application.port.out;

import java.util.List;

/** Output port: notices to the rest of the system. */
public interface DeviceEventsPort {

    /** Someone signed in to the account from a new phone; Alert Service warns the other phones. */
    void newPhone(String phoneName, List<String> otherPhoneTokens);
}
