package com.lovedev.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.name:LoveDev API}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Bearer Authentication";

        return new OpenAPI()
                .info(new Info()
                        .title(appName + " Documentation")
                        .version(appVersion)
                        .description("""
                                ## LoveDev REST API Documentation
                                
                                Complete REST API with Spring Boot, Spring Security, JWT, and Keycloak integration.
                                
                                ### Features:
                                - 🔐 JWT Authentication & Authorization
                                - 👤 User Management (CRUD)
                                - 🔑 Role-Based Access Control
                                - 📧 Email Verification
                                - 🔄 Password Reset
                                - 📊 Audit Logging
                                - 🔍 Search & Pagination
                                
                                ### How to use:
                                1. Register a new user or login with existing credentials
                                2. Copy the access token from the response
                                3. Click "Authorize" button and paste: `Bearer <your-token>`
                                4. Now you can test all protected endpoints
                                
                                ### Default Admin:
                                - Email: admin@lovedev.me
                                - Password: admin@123
                                """)
                        .contact(new Contact()
                                .name("LoveDev Team")
                                .email("support@lovedev.com")
                                .url("https://lovedev.me"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server"),
                        new Server()
                                .url("https://api.lovedev.me")
                                .description("Production Server")))
                .tags(List.of(
                        new Tag()
                                .name("Authentication")
                                .description("Authentication and registration endpoints"),
                        new Tag()
                                .name("User")
                                .description("User profile management endpoints"),
                        new Tag()
                                .name("Admin")
                                .description("Admin user management endpoints (Admin only)")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .description("""
                                                Enter JWT token in the format: **your-jwt-token**
                                                
                                                Don't include 'Bearer' prefix - it will be added automatically.
                                                
                                                To get a token:
                                                1. Use POST /api/v1/auth/login
                                                2. Copy the 'accessToken' from response
                                                3. Paste it here
                                                """)));
    }
}