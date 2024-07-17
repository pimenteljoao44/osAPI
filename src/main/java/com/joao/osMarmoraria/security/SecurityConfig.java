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
        http
                .cors().and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                .authorizeRequests(authorize -> authorize
                        .antMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .antMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .antMatchers(HttpMethod.POST, "/product").hasRole("ADMIN")
                        .antMatchers(HttpMethod.GET, "/funcionarios").hasRole("ADMIN")
                        .antMatchers(HttpMethod.POST, "/funcionarios").hasRole("ADMIN")
                        .antMatchers(HttpMethod.PUT, "/funcionarios/{id}").hasRole("ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/funcionarios/{id}").hasRole("ADMIN")
                        .antMatchers(HttpMethod.GET, "/clientes").permitAll()
                        .antMatchers(HttpMethod.POST, "/clientes").permitAll()
                        .antMatchers(HttpMethod.PUT, "/clientes/{id}").permitAll()
                        .antMatchers(HttpMethod.DELETE, "/clientes/{id}").permitAll()
                        .antMatchers(HttpMethod.POST, "/os").permitAll()
                        .antMatchers(HttpMethod.POST, "/usuarios").permitAll()
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
                        .antMatchers(HttpMethod.DELETE, "/pessoas/{id}").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://pimenteljoao44.github.io"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

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
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}