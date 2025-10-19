package com.joao.osMarmoraria.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    SecurityFilter securityFilter;

    @Autowired
    CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    CustomAccessDeniedHandler accessDeniedHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable().sessionManagement().sessionCreationPolicy(
                        SessionCreationPolicy.IF_REQUIRED)
                .and().authorizeRequests(authorize ->
                        authorize.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .antMatchers(HttpMethod.POST, "/auth/login").permitAll()
                                .antMatchers(HttpMethod.POST, "/auth/recovery").permitAll()
                                .antMatchers(HttpMethod.POST, "/auth/register").hasRole("ADMIN")
                                .antMatchers(HttpMethod.GET, "/localidades/**").permitAll()

                                // Produtos - funcionários podem visualizar, apenas admins podem modificar
                                .antMatchers(HttpMethod.GET, "/produto").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.POST, "/produto").hasRole("ADMIN")
                                .antMatchers(HttpMethod.PUT, "/produto").hasRole("ADMIN")
                                .antMatchers(HttpMethod.DELETE, "/produto").hasRole("ADMIN")

                                // Grupos - funcionários podem visualizar, apenas admins podem modificar
                                .antMatchers(HttpMethod.GET, "/grupo").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.POST, "/grupo").hasRole("ADMIN")
                                .antMatchers(HttpMethod.PUT, "/grupo/{id}").hasRole("ADMIN")
                                .antMatchers(HttpMethod.DELETE, "/grupo/{id}").hasRole("ADMIN")

                                // Funcionários - apenas admins podem gerenciar
                                .antMatchers(HttpMethod.GET, "/funcionarios").hasRole("ADMIN")
                                .antMatchers(HttpMethod.GET, "/funcionarios/{id}").hasRole("ADMIN")
                                .antMatchers(HttpMethod.POST, "/funcionarios").hasRole("ADMIN")
                                .antMatchers(HttpMethod.PUT, "/funcionarios/{id}").hasRole("ADMIN")
                                .antMatchers(HttpMethod.DELETE, "/funcionarios/{id}").hasRole("ADMIN")

                                // Clientes - funcionários podem visualizar e criar, apenas admins podem deletar
                                .antMatchers(HttpMethod.GET, "/clientes").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.GET, "/clientes/{id}").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.POST, "/clientes").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.PUT, "/clientes/{id}").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.DELETE, "/clientes/{id}").hasRole("ADMIN")

                                // Fornecedores - apenas admins podem gerenciar
                                .antMatchers(HttpMethod.GET, "/fornecedores").hasRole("ADMIN")
                                .antMatchers(HttpMethod.GET, "/fornecedores/{id}").hasRole("ADMIN")
                                .antMatchers(HttpMethod.POST, "/fornecedores").hasRole("ADMIN")
                                .antMatchers(HttpMethod.PUT, "/fornecedores/{id}").hasRole("ADMIN")
                                .antMatchers(HttpMethod.DELETE, "/fornecedores/{id}").hasRole("ADMIN")

                                // Ordens de Serviço - funcionários podem visualizar, apenas admins podem aprovar
                                .antMatchers(HttpMethod.GET, "/api/os").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.POST, "/api/os").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.PUT, "/api/os/{id}").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.PATCH, "/api/os/{id}/aprovar").hasRole("ADMIN")
                                .antMatchers(HttpMethod.PATCH, "/api/os/{id}/aprovar-e-agendar").hasRole("ADMIN")
                                .antMatchers(HttpMethod.PATCH, "/api/os/{id}/**").hasAnyRole("ADMIN", "USER")

                                // Usuários - apenas admins podem gerenciar
                                .antMatchers(HttpMethod.GET, "/usuarios").hasRole("ADMIN")
                                .antMatchers(HttpMethod.POST, "/usuarios").hasRole("ADMIN")
                                .antMatchers(HttpMethod.PUT, "/usuarios/{id}").hasRole("ADMIN")
                                .antMatchers(HttpMethod.DELETE, "/usuarios/{id}").hasRole("ADMIN")
                                .antMatchers(HttpMethod.PUT, "/usuarios/{id}/update-password").hasRole("ADMIN")

                                // Estados e Cidades - funcionários podem visualizar, apenas admins podem modificar
                                .antMatchers(HttpMethod.GET, "/api/estado").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.POST, "/api/estado").hasRole("ADMIN")
                                .antMatchers(HttpMethod.PUT, "/api/estado/{id}").hasRole("ADMIN")
                                .antMatchers(HttpMethod.DELETE, "/api/estado/{id}").hasRole("ADMIN")
                                .antMatchers(HttpMethod.GET, "/api/cidade").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.GET, "/api/cidade/{id}").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.POST, "/api/cidade").hasRole("ADMIN")
                                .antMatchers(HttpMethod.PUT, "/api/cidade/{id}").hasRole("ADMIN")
                                .antMatchers(HttpMethod.DELETE, "/api/cidade/{id}").hasRole("ADMIN")

                                // Pessoas - funcionários podem visualizar e criar, apenas admins podem deletar
                                .antMatchers(HttpMethod.GET, "/pessoas").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.POST, "/pessoas").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.PUT, "/pessoas/{id}").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.DELETE, "/pessoas/{id}").hasRole("ADMIN")

                                // Vendas - funcionários podem visualizar e criar, apenas admins podem deletar
                                .antMatchers(HttpMethod.GET, "/venda").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.POST, "/venda").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.PUT, "/venda/{id}").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.DELETE, "/venda/{id}").hasRole("ADMIN")
                                .antMatchers(HttpMethod.GET, "/venda/itens").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.POST, "/venda/{id}/addItem").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.DELETE, "/venda/{id}/removeItem/{itemId}").hasAnyRole("ADMIN", "USER")

                                // Vendas Unificadas - funcionários podem visualizar e criar, apenas admins podem deletar
                                .antMatchers(HttpMethod.GET, "/api/venda-unificada/**").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.POST, "/api/venda-unificada/**").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.PUT, "/api/venda-unificada/**").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.PATCH, "/api/venda-unificada/**").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.DELETE, "/api/venda-unificada/**").hasRole("ADMIN")

                                // Compras - apenas admins podem gerenciar
                                .antMatchers(HttpMethod.GET, "/compra").hasRole("ADMIN")
                                .antMatchers(HttpMethod.POST, "/compra").hasRole("ADMIN")
                                .antMatchers(HttpMethod.PUT, "/compra/{id}").hasRole("ADMIN")
                                .antMatchers(HttpMethod.DELETE, "/compra/{id}").hasRole("ADMIN")
                                .antMatchers(HttpMethod.GET, "/compra/itens").hasRole("ADMIN")
                                .antMatchers(HttpMethod.POST, "/compra/{id}/addItem").hasRole("ADMIN")
                                .antMatchers(HttpMethod.DELETE, "/compra/{id}/removeItem/{itemId}").hasRole("ADMIN")

                                // Relatórios - apenas admins podem gerar
                                .antMatchers(HttpMethod.POST, "/relatorios/gerar/relatorioDeVendasResumido").hasRole("ADMIN")

                                // Projetos - funcionários podem visualizar, apenas admins podem aprovar/rejeitar
                                .antMatchers(HttpMethod.GET, "/projetos-personalizados").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.GET, "/projetos-personalizados/{id}").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.POST, "/projetos-personalizados").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.PUT, "/projetos-personalizados/{id}").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.PATCH, "/projetos-personalizados/{id}/aprovar").hasRole("ADMIN")
                                .antMatchers(HttpMethod.PATCH, "/projetos-personalizados/{id}/rejeitar").hasRole("ADMIN")
                                .antMatchers(HttpMethod.DELETE, "/projetos-personalizados/{id}").hasRole("ADMIN")
                                .antMatchers(HttpMethod.POST, "/projetos-personalizados/calcular-orcamento").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.POST, "/projetos-personalizados/materiais-sugeridos").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.GET, "/projetos-personalizados/relatorio/periodo").hasRole("ADMIN")
                                .antMatchers(HttpMethod.GET, "/projetos-personalizados/cliente/{id}").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.GET, "/projetos-personalizados/tipos").hasAnyRole("ADMIN", "USER")
                                .antMatchers(HttpMethod.GET, "/projetos-personalizados/status").hasAnyRole("ADMIN", "USER")

                                // Contas a Pagar/Receber - funcionários podem gerenciar
                                .antMatchers("/api/contas-pagar/**").hasAnyRole("ADMIN", "USER")
                                .antMatchers("/api/contas-receber/**").hasAnyRole("ADMIN", "USER")

                                .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);
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
