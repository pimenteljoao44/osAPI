package com.joao.osMarmoraria.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados da documentação OpenAPI (Swagger UI em {@code /swagger-ui.html}).
 *
 * <p>O esquema de segurança bearer-jwt habilita o botão "Authorize" da UI:
 * cole o token devolvido por {@code POST /auth/login} e todas as chamadas
 * de teste passam a enviar o header Authorization automaticamente.</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_JWT = "bearer-jwt";

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("osAPI — ERP Marmoraria São Carlos")
                        .description("API REST do ERP: clientes, produtos, estoque, projetos, "
                                + "vendas, ordens de serviço e financeiro (contas a pagar/receber). "
                                + "Autenticação via JWT (POST /auth/login).")
                        .version("v1")
                        .contact(new Contact()
                                .name("João Vitor Pimentel")
                                .url("https://github.com/pimenteljoao44")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_JWT, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_JWT));
    }
}
