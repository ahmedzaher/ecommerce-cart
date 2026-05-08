package com.exequt.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce Cart Checkout API")
                        .description("""
                                API for a cart checkout and mock payment system.
                                                                
                                ## Flows
                                - **Flow 1** — Create cart → Add items → Checkout → Pay → PAID
                                - **Flow 2** — Payment FAILED → Retry payment → PAID
                                - **Flow 3** — Duplicate webhook → Idempotent (no state corruption)
                                                                
                                ## Order State Machine
                                `CREATED → PENDING_PAYMENT → PAID`
                                `CREATED → CANCELLED`
                                `PENDING_PAYMENT → PAYMENT_FAILED → PENDING_PAYMENT`
                                `PAYMENT_FAILED → CANCELLED`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ExeQut")
                                .url("https://exequt.com"))
                        .license(new License()
                                .name("Private")
                                .url("https://code-quests.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local")
                ));
    }
}
