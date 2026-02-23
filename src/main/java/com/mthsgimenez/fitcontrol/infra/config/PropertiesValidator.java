package com.mthsgimenez.fitcontrol.infra.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class PropertiesValidator {

    @Valid
    private Jwt jwt;

    @Getter
    @Setter
    public static class Jwt {
        @NotBlank(message = "Must set JWT_SECRET env var")
        private String secret;

        @PostConstruct
        public void validate() {
            if (secret == null || secret.isBlank()) {
                throw new IllegalStateException("Must set JWT_SECRET env var");
            }

            int length = secret.length();

            boolean isHex = secret.matches("[0-9a-fA-F]+") && length == 64;
            boolean isPlain = length == 32;

            if (!isHex && !isPlain) {
                throw new IllegalStateException(
                        "JWT_SECRET must be 256 bits long"
                );
            }
        }
    }
}