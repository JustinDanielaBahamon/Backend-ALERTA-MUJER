package com.alertamujer.admin.adapter.out.web;

import com.alertamujer.admin.application.port.out.RequestContextPort;
import com.alertamujer.shared.web.RequestInfo;
import org.springframework.stereotype.Component;

/** Output adapter: reads the IP, device and source of the current HTTP request. */
@Component
class RequestContextAdapter implements RequestContextPort {

    @Override
    public String ip() {
        return RequestInfo.ip();
    }

    @Override
    public String device() {
        return RequestInfo.device();
    }

    @Override
    public String source() {
        return RequestInfo.source();
    }
}
