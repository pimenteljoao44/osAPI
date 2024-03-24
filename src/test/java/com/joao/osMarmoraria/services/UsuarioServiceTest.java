package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.Cidade;
import com.joao.osMarmoraria.domain.Cliente;
import com.joao.osMarmoraria.domain.Estado;
import com.joao.osMarmoraria.domain.Usuario;
import com.joao.osMarmoraria.domain.enums.NivelAcesso;
import com.joao.osMarmoraria.dtos.CidadeDTO;
import com.joao.osMarmoraria.dtos.EstadoDTO;
import com.joao.osMarmoraria.dtos.UsuarioDTO;
import com.joao.osMarmoraria.repository.UsuarioRepository;
import com.joao.osMarmoraria.services.exceptions.DataIntegratyViolationException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {
    public static final String LOGIN = "joao.pimentel";
    public static final String SENHA = "123";
    public static final int NIVEL_ACESSO = 1;
    @InjectMocks
    private UsuarioService service;
    @Mock
    private UsuarioRepository repository;

    private Usuario usuario;

    private UsuarioDTO usuarioDTO;

    private Optional<Usuario> optionalUsuario;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
        startUser();
    }

    private void startUser(){
        usuario = new Usuario(LOGIN, SENHA,NIVEL_ACESSO);
        usuarioDTO = new UsuarioDTO(usuario);
        optionalUsuario = Optional.of(new Usuario(LOGIN,SENHA,NIVEL_ACESSO));
    }
    @Test
    void whenFindByIdThenReturnAnUserInstance() {
        Mockito.when(repository.findById(Mockito.anyInt())).thenReturn(optionalUsuario);
        Usuario response = service.findById(1);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(Usuario.class,response.getClass());
    }

    @Test
    void findById_ShouldThrowObjectNotFoundException_WhenUserDoesNotExist() {
        when(repository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> service.findById(anyInt()));
    }

    @Test
    void findAll_ShouldReturnListOfUsers() {
        List<Usuario> usuarios = new ArrayList<>();
        when(repository.findAll()).thenReturn(usuarios);

        List<Usuario> result = service.findAll();

        assertEquals(usuarios, result);
    }

    @Test
    void whenCreateThenReturnSucess() {
        when(repository.save(any())).thenReturn(usuario);
        Usuario response =service.create(usuarioDTO);
        assertNotNull(response);
        assertEquals(Usuario.class,response.getClass());
    }

    @Test
    void whenCreateThenReturnAnDataIntegratyViolationExcption() {
        when(repository.findByLogin(anyString())).thenReturn(usuario);
        try {
            optionalUsuario.get().setId(2);
            Usuario response =service.create(usuarioDTO);
        } catch (Exception e) {
            assertEquals(DataIntegratyViolationException.class, e.getClass());
        }
    }

    @Test
    void whenUpdateThenReturnSucess() {
        Usuario existingUser = new Usuario();
        existingUser.setId(1);
        existingUser.setNome("João Vitor");
        existingUser.setLogin("joao.pimentel");
        existingUser.setNivelAcesso(NivelAcesso.GERENTE);
        existingUser.setSenha("123");

        UsuarioDTO dto = new UsuarioDTO(existingUser);


        when(repository.findById(1)).thenReturn(Optional.of(existingUser));
        when(repository.save(any(Usuario.class))).thenReturn(existingUser);

        Usuario result = service.update( dto);

        assertNotNull(result);
        assertEquals("João Vitor", result.getNome());
    }

    @Test
    void whenUpdateThenReturnAnDataIntegratyViolationExcption() {
        when(repository.findByLogin(anyString())).thenReturn(usuario);
        try {
            optionalUsuario.get().setId(3);
            Usuario response =service.create(usuarioDTO);
        } catch (Exception e) {
            assertEquals(DataIntegratyViolationException.class, e.getClass());
        }
    }

    @Test
    void deleteWithSucess() {
        when(repository.findById(anyInt())).thenReturn(optionalUsuario);
        doNothing().when(repository).deleteById(anyInt());
        service.delete(1);
        verify(repository,times(1)).deleteById(anyInt());
    }
    @Test
    void deleteWithObjectNotFoundException(){
        when(repository.findById(anyInt())).thenThrow(new ObjectNotFoundException("Objeto Não Encontrado"));
        try {
            service.delete(1);
        } catch (Exception e) {
            assertEquals(ObjectNotFoundException.class,e.getClass());
            assertEquals("Objeto Não Encontrado",e.getMessage());
        }
    }
}