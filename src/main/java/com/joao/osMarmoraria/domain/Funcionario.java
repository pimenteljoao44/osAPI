package com.joao.osMarmoraria.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;


@Entity
@Table(name = "funcionario")
public class Funcionario implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "funcionario_id")
	private Integer id;

	@Column(name = "func_cargo")
	private String cargo;

	@Column(name = "func_salario")
	private BigDecimal salario;

	@Column(name = "func_ctps")
	private String ctps;

	@OneToOne
	@JoinColumn(name = "pessoa_id")
	private Pessoa pessoa;
	@JsonIgnore
	@OneToMany(mappedBy = "funcionario")
	private List<OrdemDeServico> listOs = new ArrayList<>();

	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "America/Sao_Paulo")
	private Date dataCriacao;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "America/Sao_Paulo")
	private Date dataAtualizacao;

	public Funcionario() {
	}

	public Funcionario(Integer id, String cargo, BigDecimal salario, String ctps, Pessoa pessoa, List<OrdemDeServico> listOs, Date dataCriacao, Date dataAtualizacao) {
		this.id = id;
		this.cargo = cargo;
		this.salario = salario;
		this.ctps = ctps;
		this.pessoa = pessoa;
		this.listOs = listOs;
		this.dataCriacao = dataCriacao;
		this.dataAtualizacao = dataAtualizacao;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCargo() {
		return cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

	public BigDecimal getSalario() {
		return salario;
	}

	public void setSalario(BigDecimal salario) {
		this.salario = salario;
	}

	public String getCtps() {
		return ctps;
	}

	public void setCtps(String ctps) {
		this.ctps = ctps;
	}

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	public List<OrdemDeServico> getListOs() {
		return listOs;
	}

	public void setListOs(List<OrdemDeServico> listOs) {
		this.listOs = listOs;
	}

	public Date getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(Date dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}
}
