package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.Usuario;
import com.joao.osMarmoraria.dtos.UsuarioDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    UsuarioRepository usuarioRepository;
    @Test
    @DisplayName("Shoud get user sucessfuly from database")
    void findByLoginSucess() {
        Usuario obj = new Usuario("joao.votor","joao123","joao.vitor@gmail.com",1);
        UsuarioDTO data = new UsuarioDTO(obj);
        this.createUser(data);
        Usuario result =  this.usuarioRepository.findByLogin(obj.getLogin());
        assertThat(result != null).isTrue();
    }

    @Test
    @DisplayName("Shoud not get user sucessfuly from database")
    void findByLoginError() {
        Usuario obj = new Usuario();
        Usuario result =  this.usuarioRepository.findByLogin(obj.getLogin());
        assertThat(result == null).isTrue();
    }

    private Usuario createUser(UsuarioDTO data){
        Usuario newUser = new Usuario(data.getId(),data.getNome(), data.getLogin(), data.getSenha(),data.getEmail(),data.getNivelAcesso());
        this.entityManager.persist(newUser);
        return newUser;
    }
}