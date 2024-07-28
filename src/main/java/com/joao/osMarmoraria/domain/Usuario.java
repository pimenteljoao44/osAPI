package com.joao.osMarmoraria.domain;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.joao.osMarmoraria.domain.enums.NivelAcesso;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Entity
@Cacheable(false)
public class Usuario implements UserDetails {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;


	private String nome;

	@NotEmpty(message = "Campo Login é requerido")
	private String login;

	@NotEmpty(message = "Campo Senha é requerido")
	private String senha;

	@NotEmpty(message = "Campo Email é requerido")
	private String email;


	private Integer nivelAcesso;

	public Usuario() {
		super();
	}

	public Usuario(Integer id, String nome, String login, String senha,String email, NivelAcesso nivelAcesso) {
		super();
		this.id = id;
		this.nome = nome;
		this.login = login;
		this.senha = senha;
		this.email = email;
		this.nivelAcesso = (nivelAcesso == null? 0: nivelAcesso.getCod());
	}

	public Usuario(String login, String senha,String email, Integer nivelAcesso) {
		this.login = login;
		this.senha = senha;
		this.senha = senha;
		this.nivelAcesso = nivelAcesso;
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

	public void setEmail(String email) {this.email = email;}

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

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		 if(Objects.equals(nivelAcesso, NivelAcesso.GERENTE.getCod())) return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
				new SimpleGrantedAuthority("ROLE_USER"));
			else return List.of(new SimpleGrantedAuthority("ROLE_USER"))	;
	}

	@Override
	public String getPassword() {
		return senha;
	}

	@Override
	public String getUsername() {
		return login;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}