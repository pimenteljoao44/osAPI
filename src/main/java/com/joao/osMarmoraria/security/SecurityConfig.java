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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable().sessionManagement().sessionCreationPolicy(
                SessionCreationPolicy.IF_REQUIRED)
                .and().authorizeRequests(authorize ->
                        authorize.antMatchers(HttpMethod.POST, "/auth/login").permitAll()
                                .antMatchers(HttpMethod.POST, "/auth/recovery").permitAll()
                                .antMatchers(HttpMethod.POST, "/auth/register").permitAll()
                                .antMatchers(HttpMethod.GET, "/localidades/**").permitAll()
                                .antMatchers(HttpMethod.GET, "/produto").permitAll()
                                .antMatchers(HttpMethod.POST, "/produto").permitAll()
                                .antMatchers(HttpMethod.PUT, "/produto").permitAll()
                                .antMatchers(HttpMethod.DELETE, "/produto").permitAll()
                                .antMatchers(HttpMethod.GET, "/grupo").permitAll()
                                .antMatchers(HttpMethod.POST, "/grupo").permitAll()
                                .antMatchers(HttpMethod.PUT, "/grupo/{id}").permitAll()
                                .antMatchers(HttpMethod.DELETE, "/grupo/{id}").permitAll()
                                .antMatchers(HttpMethod.GET, "/funcionarios").permitAll()
                                .antMatchers(HttpMethod.GET, "/funcionarios/{id}").permitAll()
                                .antMatchers(HttpMethod.POST, "/funcionarios").permitAll()
                                .antMatchers(HttpMethod.PUT, "/funcionarios/{id}").permitAll()
                                .antMatchers(HttpMethod.DELETE, "/funcionarios/{id}").permitAll()
                                .antMatchers(HttpMethod.GET, "/clientes").permitAll()
                                .antMatchers(HttpMethod.GET, "/clientes/{id}").permitAll()
                                .antMatchers(HttpMethod.POST, "/clientes").permitAll()
                                .antMatchers(HttpMethod.PUT, "/clientes/{id}").permitAll()
                                .antMatchers(HttpMethod.DELETE, "/clientes/{id}").permitAll()
                                .antMatchers(HttpMethod.GET, "/fornecedores").permitAll()
                                .antMatchers(HttpMethod.GET, "/fornecedores/{id}").permitAll()
                                .antMatchers(HttpMethod.POST, "/fornecedores").permitAll()
                                .antMatchers(HttpMethod.PUT, "/fornecedores/{id}").permitAll()
                                .antMatchers(HttpMethod.DELETE, "/fornecedores{id}").permitAll()
                                .antMatchers(HttpMethod.POST, "/os").permitAll()
                                .antMatchers(HttpMethod.PUT, "/os/{id}").permitAll()
                                .antMatchers(HttpMethod.PUT, "/os/{id/concluir-os}").permitAll()
                                .antMatchers(HttpMethod.PUT, "/os/{id}/iniciar-os}").permitAll()
                                .antMatchers(HttpMethod.GET, "/usuarios").permitAll()
                                .antMatchers(HttpMethod.POST, "/usuarios").permitAll()
                                .antMatchers(HttpMethod.PUT, "/usuarios/{id}").permitAll()
                                .antMatchers(HttpMethod.DELETE, "/usuarios/{id}").permitAll()
                                .antMatchers(HttpMethod.PUT, "/usuarios/{id}/update-password").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/estado").permitAll()
                                .antMatchers(HttpMethod.POST, "/api/estado").permitAll()
                                .antMatchers(HttpMethod.PUT, "/api/estado/{id}").permitAll()
                                .antMatchers(HttpMethod.DELETE, "/api/estado/{id}").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/cidade").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/cidade/{id}").permitAll()
                                .antMatchers(HttpMethod.POST, "/api/cidade").permitAll()
                                .antMatchers(HttpMethod.PUT, "/api/cidade/{id}").permitAll()
                                .antMatchers(HttpMethod.DELETE, "/api/cidade/{id}").permitAll()
                                .antMatchers(HttpMethod.GET, "/pessoas").permitAll()
                                .antMatchers(HttpMethod.POST, "/pessoas").permitAll()
                                .antMatchers(HttpMethod.PUT, "/pessoas/{id}").permitAll()
                                .antMatchers(HttpMethod.DELETE, "/pessoas/{id}").permitAll().antMatchers(HttpMethod.GET, "/venda").permitAll()
                                .antMatchers(HttpMethod.POST, "/venda").permitAll()
                                .antMatchers(HttpMethod.PUT, "/venda/{id}").permitAll()
                                .antMatchers(HttpMethod.DELETE, "/venda/{id}").permitAll()
                                .antMatchers(HttpMethod.GET, "/venda/itens").permitAll()
                                .antMatchers(HttpMethod.POST, "/venda/{id}/addItem").permitAll()
                                .antMatchers(HttpMethod.DELETE, "/venda/{id}/removeItem/{itemId}").permitAll()
                                .antMatchers(HttpMethod.GET, "/compra").permitAll()
                                .antMatchers(HttpMethod.POST, "/compra").permitAll()
                                .antMatchers(HttpMethod.PUT, "/compra/{id}").permitAll()
                                .antMatchers(HttpMethod.DELETE, "/compra/{id}").permitAll()
                                .antMatchers(HttpMethod.GET, "/compra/itens").permitAll()
                                .antMatchers(HttpMethod.POST, "/compra/{id}/addItem")
                                .permitAll().antMatchers(HttpMethod.DELETE, "/compra/{id}/removeItem/{itemId}").permitAll()
                                .antMatchers(HttpMethod.POST, "/relatorios/gerar/relatorioDeVendasResumido").permitAll()
                                .antMatchers(HttpMethod.GET, "/projetos-personalizados").permitAll()
                                .antMatchers(HttpMethod.GET, "/projetos-personalizados/{id}").permitAll()
                                .antMatchers(HttpMethod.POST, "/projetos-personalizados").permitAll()
                                .antMatchers(HttpMethod.PUT, "/projetos-personalizados/{id}").permitAll()
                                .antMatchers(HttpMethod.PATCH, "/projetos-personalizados/{id}/status").permitAll()
                                .antMatchers(HttpMethod.DELETE, "/projetos-personalizados/{id}").permitAll()
                                .antMatchers(HttpMethod.POST, "/projetos-personalizados/calcular-orcamento").permitAll()
                                .antMatchers(HttpMethod.POST, "/projetos-personalizados/materiais-sugeridos").permitAll()
                                .antMatchers(HttpMethod.GET, "/projetos-personalizados/relatorio/periodo").permitAll()

                                // Endpoints de tipos e status (se existirem)
                                .antMatchers(HttpMethod.GET, "/projetos-personalizados/tipos").permitAll()
                                .antMatchers(HttpMethod.GET, "/projetos-personalizados/status").permitAll()
                                // Configuração para Contas a Receber
                                .antMatchers(HttpMethod.GET, "/api/contas-receber").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/contas-receber/{id}").permitAll()
                                .antMatchers(HttpMethod.POST, "/api/contas-receber/venda").permitAll()
                                .antMatchers(HttpMethod.POST, "/api/contas-receber/projeto").permitAll()
                                .antMatchers(HttpMethod.PUT, "/api/contas-receber/{id}").permitAll()
                                .antMatchers(HttpMethod.DELETE, "/api/contas-receber/{id}").permitAll()
                                .antMatchers(HttpMethod.PATCH, "/api/contas-receber/{id}/receber").permitAll()
                                .antMatchers(HttpMethod.PATCH, "/api/contas-receber/{id}/cancelar").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/contas-receber/status/{status}").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/contas-receber/cliente/{clienteId}").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/contas-receber/vencidas").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/contas-receber/vencendo").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/contas-receber/total/{status}").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/contas-receber/total-vencido").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/contas-receber/total-recebido").permitAll()

                                // Configuração para Contas a Pagar
                                .antMatchers(HttpMethod.GET, "/api/contas-pagar").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/contas-pagar/{id}").permitAll()
                                .antMatchers(HttpMethod.POST, "/api/contas-pagar").permitAll()
                                .antMatchers(HttpMethod.PUT, "/api/contas-pagar/{id}").permitAll()
                                .antMatchers(HttpMethod.DELETE, "/api/contas-pagar/{id}").permitAll()
                                .antMatchers(HttpMethod.PATCH, "/api/contas-pagar/{id}/pagar").permitAll()
                                .antMatchers(HttpMethod.PATCH, "/api/contas-pagar/{id}/cancelar").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/contas-pagar/status/{status}").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/contas-pagar/fornecedor/{fornecedorId}").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/contas-pagar/vencidas").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/contas-pagar/vencendo").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/contas-pagar/total/{status}").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/contas-pagar/total-vencido").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/parcelas/dashboard/resumo").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/parcelas/vencidas").permitAll()
                                .antMatchers(HttpMethod.GET, "/api/parcelas/proximas-vencer").permitAll()
                                .antMatchers("/error").permitAll()
                                .anyRequest().authenticated()).addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}