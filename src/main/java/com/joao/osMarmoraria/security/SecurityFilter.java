package com.joao.osMarmoraria.security;

import com.joao.osMarmoraria.repository.UsuarioRepository;
import com.joao.osMarmoraria.services.TokenService;
import com.joao.osMarmoraria.services.exceptions.TokenInvalidoException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.GrantedAuthority; // Importar GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Importar SimpleGrantedAuthority

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection; // Importar Collection
import java.util.stream.Collectors; // Importar Collectors

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = recoverToken(request);
        if (token != null) {
            try {
                autenticar(token);
            } catch (TokenInvalidoException e) {
                // Segue sem autenticação: o entry point responde 401, e o motivo
                // real (expirado? assinatura?) fica registrado para diagnóstico.
                log.debug("Token rejeitado: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    private void autenticar(String token) {
        String login = tokenService.validateToken(token);
        UserDetails user = userRepository.findByLogin(login);

        if (user != null) {
            // Garante que as roles tenham o prefixo "ROLE_"
            Collection<? extends GrantedAuthority> authorities = user.getAuthorities().stream()
                .map(authority -> {
                    String roleName = authority.getAuthority();
                    if (!roleName.startsWith("ROLE_")) {
                        return new SimpleGrantedAuthority("ROLE_" + roleName);
                    }
                    return authority;
                })
                .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    private String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}
