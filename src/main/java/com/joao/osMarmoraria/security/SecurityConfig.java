package com.joao.osMarmoraria.security;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.http.HttpStatus; // Importar HttpStatus

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    SecurityFilter securityFilter;

    @Autowired
    CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable().sessionManagement().sessionCreationPolicy(
                        SessionCreationPolicy.IF_REQUIRED)
                .and().authorizeRequests(authorize ->
                        authorize.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .antMatchers(HttpMethod.POST, "/auth/login").permitAll()
                                .antMatchers(HttpMethod.POST, "/auth/recovery").permitAll()
                                .antMatchers(HttpMethod.POST, "/auth/register").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.GET, "/localidades/**").permitAll()

                                // Produtos
                                .antMatchers(HttpMethod.GET, "/produto").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.POST, "/produto").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.PUT, "/produto").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.DELETE, "/produto").hasAnyRole("ADMIN", "GERENTE")

                                // Grupos
                                .antMatchers(HttpMethod.GET, "/grupo").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.POST, "/grupo").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.PUT, "/grupo/{id}").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.DELETE, "/grupo/{id}").hasAnyRole("ADMIN", "GERENTE")

                                // Funcionários
                                .antMatchers(HttpMethod.GET, "/funcionarios").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.GET, "/funcionarios/{id}").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.POST, "/funcionarios").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.PUT, "/funcionarios/{id}").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.DELETE, "/funcionarios/{id}").hasAnyRole("ADMIN", "GERENTE")

                                // Clientes
                                .antMatchers(HttpMethod.GET, "/clientes").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.POST, "/clientes").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.PUT, "/clientes/{id}").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.DELETE, "/clientes/{id}").hasAnyRole("ADMIN", "GERENTE")

                                // Fornecedores
                                .antMatchers(HttpMethod.GET, "/fornecedores").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.GET, "/fornecedores/{id}").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.POST, "/fornecedores").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.PUT, "/fornecedores/{id}").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.DELETE, "/fornecedores/{id}").hasAnyRole("ADMIN", "GERENTE")

                                // Ordens de Serviço
                                .antMatchers(HttpMethod.GET, "/api/os").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.POST, "/api/os").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.PUT, "/api/os/{id}").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.PATCH, "/api/os/{id}/aprovar").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.PATCH, "/api/os/{id}/aprovar-e-agendar").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.PATCH, "/api/os/{id}/**").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")

                                // Usuários
                                .antMatchers(HttpMethod.GET, "/usuarios").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.POST, "/usuarios").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.PUT, "/usuarios/{id}").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.DELETE, "/usuarios/{id}").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.PUT, "/usuarios/{id}/update-password").hasAnyRole("ADMIN", "GERENTE")

                                // Estados e Cidades
                                .antMatchers(HttpMethod.GET, "/api/estado").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.POST, "/api/estado").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.PUT, "/api/estado/{id}").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.DELETE, "/api/estado/{id}").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.GET, "/api/cidade").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.GET, "/api/cidade/{id}").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.POST, "/api/cidade").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.PUT, "/api/cidade/{id}").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.DELETE, "/api/cidade/{id}").hasAnyRole("ADMIN", "GERENTE")

                                // Pessoas
                                .antMatchers(HttpMethod.GET, "/pessoas").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.POST, "/pessoas").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.PUT, "/pessoas/{id}").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.DELETE, "/pessoas/{id}").hasAnyRole("ADMIN", "GERENTE")

                                // Vendas
                                .antMatchers(HttpMethod.GET, "/venda").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.POST, "/venda").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.PUT, "/venda/{id}").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.DELETE, "/venda/{id}").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.GET, "/venda/itens").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.POST, "/venda/{id}/addItem").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.DELETE, "/venda/{id}/removeItem/{itemId}").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")

                                // Vendas Unificadas
                                .antMatchers(HttpMethod.GET, "/api/venda-unificada/**").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.POST, "/api/venda-unificada/**").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.PUT, "/api/venda-unificada/**").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.PATCH, "/api/venda-unificada/**").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.DELETE, "/api/venda-unificada/**").hasAnyRole("ADMIN", "GERENTE")

                                // Compras
                                .antMatchers(HttpMethod.GET, "/compra").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.POST, "/compra").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.PUT, "/compra/{id}").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.DELETE, "/compra/{id}").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.GET, "/compra/itens").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.POST, "/compra/{id}/addItem").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.DELETE, "/compra/{id}/removeItem/{itemId}").hasAnyRole("ADMIN", "GERENTE")

                                // Relatórios
                                .antMatchers(HttpMethod.POST, "/relatorios/gerar/relatorioDeVendasResumido").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.GET, "/relatorios/vendas-cliente-periodo").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.GET, "/relatorios/compras-fornecedor-periodo").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.GET, "/relatorios/contas-pagar").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.GET, "/relatorios/contas-receber").hasAnyRole("ADMIN", "GERENTE")

                                // Projetos
                                .antMatchers(HttpMethod.GET, "/projetos-personalizados").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.POST, "/projetos-personalizados").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.PUT, "/projetos-personalizados/{id}").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.PATCH, "/projetos-personalizados/{id}/aprovar").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.PATCH, "/projetos-personalizados/{id}/rejeitar").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.DELETE, "/projetos-personalizados/{id}").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.POST, "/projetos-personalizados/calcular-orcamento").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.POST, "/projetos-personalizados/materiais-sugeridos").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.GET, "/projetos-personalizados/relatorio/periodo").hasAnyRole("ADMIN", "GERENTE")
                                .antMatchers(HttpMethod.GET, "/projetos-personalizados/cliente/{id}").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.GET, "/projetos-personalizados/tipos").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers(HttpMethod.GET, "/projetos-personalizados/status").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")

                                // Contas a Pagar/Receber
                                .antMatchers("/api/contas-pagar/**").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")
                                .antMatchers("/api/contas-receber/**").hasAnyRole("ADMIN", "GERENTE", "FUNCIONARIO")

                                .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                        })
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
