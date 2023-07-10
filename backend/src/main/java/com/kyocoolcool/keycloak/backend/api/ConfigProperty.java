package com.kyocoolcool.keycloak.backend.api;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 陳金昌 Chris Chen
 * @version 1.0 2021/12/30 3:41 PM
 */
@Component
@ConfigurationProperties(prefix = "my", ignoreUnknownFields = true)
public class ConfigProperty {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
