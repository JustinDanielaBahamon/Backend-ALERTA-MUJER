package com.alertamujer.admin.adapter.out.web;

import com.alertamujer.admin.application.port.out.RequestContextPort;
import com.alertamujer.shared.web.RequestInfo;
import org.springframework.stereotype.Component;

/** Adaptador de salida: lee la IP, el dispositivo y el origen de la petición HTTP en curso. */
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
