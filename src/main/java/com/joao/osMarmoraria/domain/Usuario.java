package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.joao.osMarmoraria.domain.enums.NivelAcesso;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList; // Import ArrayList

@Entity
@Cacheable(false)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @OneToOne(cascade = {CascadeType.ALL, CascadeType.REMOVE})
    @JoinColumn(name = "funcionario_id", nullable = true)
    @JsonBackReference("funcionario-usuario")
    private Funcionario funcionario;

    private Integer nivelAcesso;

    public Usuario() {
        super();
        // Garante um padrão seguro para novos usuários
        this.nivelAcesso = NivelAcesso.FUNCIONARIO.getCod();
    }

    public Usuario(Integer id, String nome, String login, String senha, String email, NivelAcesso nivelAcesso) {
        super();
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
        this.email = email;
        // Lógica segura: se o nível for nulo, assume o padrão FUNCIONARIO
        this.nivelAcesso = (nivelAcesso == null) ? NivelAcesso.FUNCIONARIO.getCod() : nivelAcesso.getCod();
    }

    public Usuario(String login, String senha, String email, Integer nivelAcesso) {
        this.login = login;
        this.senha = senha;
        this.email = email; // Correção: atribuindo email em vez de senha duas vezes
        this.nivelAcesso = nivelAcesso;
    }

    // ... Getters e Setters ...

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        NivelAcesso nivel = NivelAcesso.toEnum(this.nivelAcesso);
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (nivel == null) {
            // Fallback de segurança: se o nível não estiver definido, concede o mínimo (FUNCIONARIO)
            authorities.add(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"));
        } else {
            switch (nivel) {
                case GERENTE:
                    authorities.add(new SimpleGrantedAuthority("ROLE_GERENTE"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN")); // Assumindo que GERENTE também tem privilégios de ADMIN
                    break;
                case FUNCIONARIO:
                    authorities.add(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"));
                    break;
                default:
                    // Caso um novo NivelAcesso seja criado e não tratado aqui, concede o mínimo.
                    authorities.add(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"));
                    break;
            }
        }
        return authorities;
    }

    // ... Restante da classe (getPassword, getUsername, etc.) ...
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
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
