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
 * Metadados e esquema de segurança da documentação OpenAPI/Swagger
 * (disponível em {@code /swagger-ui.html}, JSON em {@code /v3/api-docs}).
 *
 * <p>Declara o esquema {@code bearer-jwt}: o botão "Authorize" da UI aceita
 * o token devolvido por {@code POST /auth/login} e passa a enviá-lo em
 * {@code Authorization: Bearer <token>} nas chamadas de teste feitas pela
 * própria interface — sem isso, todo endpoint autenticado responderia 401
 * ao ser testado direto pelo Swagger UI.</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearer-jwt";

    @Bean
    public OpenAPI osMarmorariaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("osMarmoraria API")
                        .description("ERP para marmoraria: clientes, fornecedores, produtos, " +
                                "projetos sob encomenda, ordens de serviço, vendas, compras e " +
                                "financeiro (contas a pagar/receber).")
                        .version("v1")
                        .contact(new Contact().name("João Vitor Pimentel")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
