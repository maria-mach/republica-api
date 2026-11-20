package br.ifsp.republica.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_DE_SEGURANCA = "bearerAuth";

    @Bean
    OpenAPI openAPI() {
        SecurityScheme esquemaBearer = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");

        return new OpenAPI()
            .info(new Info().title("API de Despesas da República").version("1.0"))
            .components(new Components().addSecuritySchemes(ESQUEMA_DE_SEGURANCA, esquemaBearer))
            .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_DE_SEGURANCA));
    }
}
