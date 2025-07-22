package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.Usuario;
import com.joao.osMarmoraria.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AuthorizationServiceTest {
    public static final String LOGIN = "joao.pimentel";
    public static final String SENHA = "123";
    public static final String EMAIL = "joao.pimentel@gmail.com";
    public static final int NIVEL_ACESSO = 1;

    @InjectMocks
    private AuthorizationService service;
    @Mock
    private UsuarioRepository repository;
    private Usuario usuario;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
        startUser();
    }

    private void startUser() {
        usuario = new Usuario(LOGIN, SENHA,EMAIL, NIVEL_ACESSO);
    }

    @Test
    void whenLoadByUsernameThenReturnAnUserInstance() {
        when(repository.findByLogin(anyString())).thenReturn(usuario);

        UserDetails userDetails = service.loadUserByUsername(LOGIN);

        assertNotNull(userDetails);

        assertEquals(LOGIN, userDetails.getUsername());
    }

    @Test
    void whenLoadByUsernameThenThrowUsernameNotFoundException() {
        when(repository.findByLogin(anyString())).thenReturn(null);
        try {
            UserDetails response = service.loadUserByUsername("hdsihdpad");
        } catch (Exception e) {
            assertEquals(UsernameNotFoundException.class,e.getClass());
        }
    }
}