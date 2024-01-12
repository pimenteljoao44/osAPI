package com.joao.osMarmoraria.domain;

import java.io.Serializable;
import java.util.Objects;

import com.joao.osMarmoraria.domain.enums.NivelAcesso;
import com.joao.osMarmoraria.domain.enums.Status;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;

@Entity
@Cacheable(false)
public class Usuario implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	
	private String nome;

	@NotEmpty(message = "Campo Login é requerido")
	private String login;

	@NotEmpty(message = "Campo Senha é requerido")
	private String senha;


	private Integer nivelAcesso;

	public Usuario() {
		super();
	}

	public Usuario(Integer id, String nome, String login, String senha, NivelAcesso nivelAcesso) {
		super();
		this.id = id;
		this.nome = nome;
		this.login = login;
		this.senha = senha;
		this.nivelAcesso = (nivelAcesso == null? 0: nivelAcesso.getCod());
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

	public NivelAcesso getNivelAcesso() {
		return NivelAcesso.toEnum(this.nivelAcesso);
	}

	public void setNivelAcesso(NivelAcesso nivelAcesso) {
		this.nivelAcesso = nivelAcesso.getCod();
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, login, nivelAcesso, nome, senha);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		return Objects.equals(id, other.id) && Objects.equals(login, other.login) && nivelAcesso == other.nivelAcesso
				&& Objects.equals(nome, other.nome) && Objects.equals(senha, other.senha);
	}

   

}
