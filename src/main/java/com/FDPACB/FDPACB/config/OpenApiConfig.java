package com.FDPACB.FDPACB.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String schemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Finance Data Processing & Access Control API")
                        .version("1.0.0")
                        .description("""
                                A finance dashboard backend supporting:
                                - JWT authentication
                                - Role-based access control (VIEWER / ANALYST / ADMIN)
                                - Financial record management (CRUD + filtering)
                                - Dashboard analytics (totals, trends, category breakdown)
                                
                                **Default credentials:**
                                | Username | Password | Role |
                                |---|---|---|
                                | admin | admin123 | ADMIN |
                                | analyst | analyst123 | ANALYST |
                                | viewer | viewer123 | VIEWER |
                                
                                Login via `POST /api/auth/login`, copy the token, click **Authorize** and enter `Bearer <token>`.
                                """)
                        .contact(new Contact().name("FDPACB").email("admin@fdpacb.dev")))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components()
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
