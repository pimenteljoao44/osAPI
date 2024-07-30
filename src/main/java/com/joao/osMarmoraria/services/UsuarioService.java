package com.joao.osMarmoraria.services;

import java.util.List;
import java.util.Optional;

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

	private Usuario findByLogin(UsuarioDTO objDTO) {
		Usuario obj = repository.findByLogin(objDTO.getLogin());
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
		if (findByLogin(objDTO) != null) {
			throw new DataIntegratyViolationException("Usuário já cadastrado na base de dados!");
		}
		return fromDTO(objDTO);
	}

	public Usuario update(@Valid UsuarioDTO obj) {
		findById(obj.getId());
		return fromDTO(obj);
	}

	public void updatePassword(Integer id, String newPassword) {
		try {
			System.out.println("Iniciando atualização de senha para o usuário com ID: " + id);

			Usuario usuario = findById(id);
			System.out.println("Usuário encontrado: " + usuario);

			String encryptedPassword = new BCryptPasswordEncoder().encode(newPassword);
			usuario.setSenha(encryptedPassword);

			repository.save(usuario);
			System.out.println("Senha atualizada com sucesso para o usuário com ID: " + id);
		} catch (Exception e) {
			System.out.println("Erro ao atualizar a senha para o usuário com ID: " + id);
			e.printStackTrace();
			throw e;  // Re-lançar a exceção para que o controlador possa capturá-la
		}
	}

	public void delete(Integer id) {
		repository.deleteById(id);
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

		return repository.save(newObj);
	}
}