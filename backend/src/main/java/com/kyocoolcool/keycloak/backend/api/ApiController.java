package com.kyocoolcool.keycloak.backend.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
@Component
public class ApiController {

    private final HelloService helloService;
    private final ConfigProperty configProperty;

    public ApiController(ConfigProperty configProperty, HelloService helloService) {
        this.helloService = helloService;
        this.configProperty = configProperty;
    }

    @GetMapping("/hello")
    public String getMessage() {
        log.info(helloService.getMessage());
        log.info(configProperty.getMessage());
        return helloService.getMessage();
    }
}
