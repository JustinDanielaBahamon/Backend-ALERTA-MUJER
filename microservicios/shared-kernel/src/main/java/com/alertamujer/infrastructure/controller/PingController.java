package com.alertamujer.infrastructure.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
public class PingController {

    @GetMapping("/ping")
    public String ping() {
        return "pong - AlertaMujer backend is running";
    }
}