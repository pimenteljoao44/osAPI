package com.joao.osMarmoraria.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração de segurança da aplicação — Spring Security 6 (Spring Boot 3).
 *
 * <p>ATENÇÃO (débito técnico a resolver na Fase 3 — Hardening):
 * o comportamento atual replica o do sistema original, em que praticamente
 * todos os endpoints estão como {@code permitAll()}. Isso é inseguro para
 * produção e será corrigido em etapa futura (RBAC por role / recurso).
 * A migração para Spring Security 6 foi feita preservando o comportamento
 * funcional para não quebrar o sistema em uso.</p>
 *
 * @see <a href="https://docs.spring.io/spring-security/reference/6.2/migration/servlet/config.html">Migração Spring Security 6</a>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;

    /** Origens permitidas para CORS — configuráveis via {@code app.cors.allowed-origins}. */
    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String[] allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ===== Autenticação pública =====
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/recovery").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()

                // ===== Localidades (consulta pública) =====
                .requestMatchers(HttpMethod.GET, "/localidades/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/estado", "/api/estado/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/cidade", "/api/cidade/**").permitAll()

                // ===== OpenAPI / Swagger UI / Actuator (Fase 1.4) =====
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health/**",
                    "/actuator/info"
                ).permitAll()

                // ===== Erros =====
                .requestMatchers("/error").permitAll()

                // ===== TODO (Fase 3 — Hardening): trocar por hasRole/hasAuthority =====
                // Produtos, Grupos, Funcionários, Clientes, Fornecedores
                .requestMatchers("/produto", "/produto/**").permitAll()
                .requestMatchers("/grupo", "/grupo/**").permitAll()
                .requestMatchers("/funcionarios", "/funcionarios/**").permitAll()
                .requestMatchers("/clientes", "/clientes/**").permitAll()
                .requestMatchers("/fornecedores", "/fornecedores/**").permitAll()
                .requestMatchers("/pessoas", "/pessoas/**").permitAll()

                // Usuários
                .requestMatchers("/usuarios", "/usuarios/**").permitAll()

                // Ordens de Serviço, Vendas, Compras
                .requestMatchers("/os", "/os/**").permitAll()
                .requestMatchers("/venda", "/venda/**").permitAll()
                .requestMatchers("/compra", "/compra/**").permitAll()

                // Localidades administrativas (write)
                .requestMatchers(HttpMethod.POST,   "/api/estado", "/api/estado/**").permitAll()
                .requestMatchers(HttpMethod.PUT,    "/api/estado/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/estado/**").permitAll()
                .requestMatchers(HttpMethod.POST,   "/api/cidade", "/api/cidade/**").permitAll()
                .requestMatchers(HttpMethod.PUT,    "/api/cidade/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/cidade/**").permitAll()

                // Projetos personalizados
                .requestMatchers("/projetos-personalizados", "/projetos-personalizados/**").permitAll()

                // Contas a receber / pagar / parcelas
                .requestMatchers("/api/contas-receber", "/api/contas-receber/**").permitAll()
                .requestMatchers("/api/contas-pagar",  "/api/contas-pagar/**").permitAll()
                .requestMatchers("/api/parcelas/**").permitAll()

                // Relatórios
                .requestMatchers(HttpMethod.POST, "/relatorios/**").permitAll()

                // Tudo o mais exige autenticação
                .anyRequest().authenticated()
            )
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
