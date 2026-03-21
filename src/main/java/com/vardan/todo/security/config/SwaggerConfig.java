package com.vardan.todo.security.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI Configuration
 * ================================
 *
 * This class configures the auto-generated API documentation page.
 *
 * After starting your app, visit:
 *   http://localhost:8080/swagger-ui.html
 *
 * WHAT THIS CONFIGURES:
 * ---------------------
 * 1. API title, description, version, and contact info
 *    → Shows at the top of the Swagger page
 *
 * 2. JWT Bearer Token security scheme
 *    → Adds an "Authorize" button to the Swagger page
 *    → You can paste your JWT token there and then test
 *       protected endpoints directly from the browser
 *    → Without this, you'd get 401 errors when testing
 *       any endpoint that requires authentication
 *
 * HOW THE SECURITY SCHEME WORKS:
 * ------------------------------
 * When you click "Authorize" and paste your token,
 * Swagger automatically adds "Authorization: Bearer <your-token>"
 * to every request — exactly like setting up Bearer Token in Postman.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API Information — displayed at the top of the Swagger page
                .info(new Info()
                        .title("TODO Application API")
                        .description("REST API for a Todo application with JWT authentication, "
                                + "categories, search & filter, and pagination. "
                                + "Built with Spring Boot, Spring Security, MySQL, and Redis.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Vardan Barseghyan")
                                .url("https://github.com/vardanbarseghyan/TODO")))

                // Security — tells Swagger that your API uses JWT Bearer tokens
                // This adds the "Authorize" button at the top of the page
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))

                // Define what "Bearer Authentication" means
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .name("Bearer Authentication")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")        // The scheme is "bearer"
                                        .bearerFormat("JWT")     // The format is JWT
                                        .description("Enter your JWT access token. "
                                                + "Get it by calling POST /api/v1/auth/login")));
    }
}