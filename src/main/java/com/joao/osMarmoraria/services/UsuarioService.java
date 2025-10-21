package com.joao.osMarmoraria.services;

import java.util.List;
import java.util.Optional;

import com.joao.osMarmoraria.domain.Funcionario;
import com.joao.osMarmoraria.domain.Projeto;
import com.joao.osMarmoraria.exceptions.DeletionRestrictedException;
import com.joao.osMarmoraria.repository.FuncionarioRepository;
import com.joao.osMarmoraria.repository.ProjetoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.joao.osMarmoraria.domain.Cliente;
import com.joao.osMarmoraria.domain.Usuario;
import com.joao.osMarmoraria.domain.enums.NivelAcesso;

import com.joao.osMarmoraria.dtos.UsuarioDTO;
import com.joao.osMarmoraria.repository.UsuarioRepository;
import com.joao.osMarmoraria.services.exceptions.DataIntegratyViolationException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;


import javax.transaction.Transactional;
import javax.validation.Valid;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private ProjetoRepository projetoRepository; // Injetando ProjetoRepository

    private Usuario findByLogin(UsuarioDTO objDTO) {
        Usuario obj = repository.findByLogin(objDTO.getLogin());
        if (obj != null) {
            return obj;
        }
        return null;
    }

    private Usuario findByEmail(UsuarioDTO objDTO) {
        Usuario obj = repository.findByEmail(objDTO.getEmail());
        if (obj != null) {
            return obj;
        }
        return null;
    }

    public Usuario findById(Integer id) {
        Optional<Usuario> obj = repository.findById(id);
        return obj.orElseThrow(() -> new ObjectNotFoundException("Usuario não encontrado!"));
    }

    public List<Usuario> findAll() {
        return repository.findAll();
    }

    public Usuario create(UsuarioDTO objDTO) {
        if (findByLogin(objDTO) != null || findByEmail(objDTO) != null) {
            throw new DataIntegratyViolationException("Usuário já cadastrado na base de dados!");
        }
        return fromDTO(objDTO);
    }

    public Usuario update(@Valid UsuarioDTO obj) {
        findById(obj.getId());
        return fromDTO(obj);
    }

    public void updatePassword(Integer id, String newPassword) {
        Usuario usuario = findById(id);
        String encryptedPassword = new BCryptPasswordEncoder().encode(newPassword);
        usuario.setSenha(encryptedPassword);
        repository.save(usuario);
    }

    public void delete(Integer id) {
        Usuario usuario = findById(id);

        if(usuario.getFuncionario() != null) {
            throw new DeletionRestrictedException("Não é possível excluir este usuário pois ele possui um funcionário vinculado. Por favor, desvincule o funcionário antes de tentar excluir o usuário.");
        }
        if (projetoRepository.existsByUsuarioCriacao_Id(id)) {
            throw new DeletionRestrictedException("Não é possível excluir este usuário pois ele criou projetos. Por favor, remova os projetos criados por este usuário antes de tentar excluí-lo.");
        }

        repository.deleteById(id);
    }

    public Funcionario findFuncionarioById(Integer id) {
        Optional<Funcionario> obj = funcionarioRepository.findById(id);
        return obj.orElseThrow(() -> new ObjectNotFoundException(
                "Objeto não encontrado! id:" + id + ",tipo: " + Cliente.class.getName()));
    }

    private Usuario fromDTO(UsuarioDTO obj) {
        String encryptedPassword = new BCryptPasswordEncoder().encode(obj.getSenha());
        Usuario newObj = new Usuario();
        newObj.setId(obj.getId());
        newObj.setNome(obj.getNome());
        newObj.setLogin(obj.getLogin());
        newObj.setSenha(encryptedPassword);
        newObj.setEmail(obj.getEmail());
        newObj.setNivelAcesso(NivelAcesso.toEnum(obj.getNivelAcesso().getCod()));
        newObj.setFuncionario(obj.getFuncionario());
        Funcionario funcionario = obj.getFuncionario() != null ? findFuncionarioById(obj.getFuncionario().getId()) : null;
        if (funcionario != null) {
            // Não é necessário salvar o funcionário aqui, apenas associá-lo ao usuário
            newObj.setFuncionario(funcionario);
        } else {
            newObj.setFuncionario(null);
        }

        return repository.save(newObj);
    }
}