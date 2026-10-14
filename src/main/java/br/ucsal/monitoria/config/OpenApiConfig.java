package br.ucsal.monitoria.config;

import br.ucsal.monitoria.security.FrontendOnlyFilter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger em /swagger-ui.html. No botao "Authorize" informe o token JWT e a chave X-Frontend-Key.
 * Em producao desligue com SPRINGDOC_API_DOCS_ENABLED=false e SPRINGDOC_SWAGGER_UI_ENABLED=false.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("Sistema de Controle de Monitoria de Alunos - API").version("1.0"))
                .components(new Components()
                        .addSecuritySchemes("jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                        .addSecuritySchemes("frontendKey", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER)
                                .name(FrontendOnlyFilter.HEADER)))
                .addSecurityItem(new SecurityRequirement().addList("jwt").addList("frontendKey"));
    }
}
