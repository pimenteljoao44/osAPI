package com.joao.osMarmoraria.dtos;

import java.io.Serializable;

import com.joao.osMarmoraria.domain.Usuario;
import com.joao.osMarmoraria.domain.enums.NivelAcesso;

import javax.validation.constraints.NotEmpty;


public class UsuarioDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private String nome;

	@NotEmpty(message = "Campo Login é requerido")
	private String login;

	@NotEmpty(message = "Campo Senha é requerido")
	private String senha;

	@NotEmpty(message = "Campo Email é requerido")
	private String email;

	private Integer nivelAcesso;

	public UsuarioDTO() {
		super();
	}
	public UsuarioDTO(Integer id, String nome, String login, String senha, String email, Integer nivelAcesso) {
		this.id = id;
		this.nome = nome;
		this.login = login;
		this.senha = senha;
		this.email = email;
		this.nivelAcesso = nivelAcesso;
	}

	public UsuarioDTO(Usuario obj) {
		super();
		this.id = obj.getId();
		this.nome = obj.getNome();
		this.login = obj.getLogin();
		this.senha = obj.getSenha();
		this.email = obj.getEmail();
		this.nivelAcesso = obj.getNivelAcesso().getCod();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getEmail() {return email;}

	public void  setEmail(String email) {this.email = email;}

	public NivelAcesso getNivelAcesso() {
		return NivelAcesso.toEnum(this.nivelAcesso);
	}

	public void setNivelAcesso(Integer nivelAcesso) {
		this.nivelAcesso = nivelAcesso;
	}

}