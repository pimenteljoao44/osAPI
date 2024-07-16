package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Cliente implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer cliId;

	@OneToMany(mappedBy = "cliente", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
	private List<OrdemDeServico> listOs = new ArrayList<>();

	@OneToOne(cascade = {CascadeType.ALL, CascadeType.REMOVE}, orphanRemoval = true)
	@JoinColumn(name = "pessoa_id", nullable = false)
	private Pessoa pessoa;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "America/Sao_Paulo")
	private Date dataCriacao;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "America/Sao_Paulo")
	private Date dataAtualizacao;

	public Cliente() {
	}

	public Cliente(Integer cliId, List<OrdemDeServico> listOs, Pessoa pessoa, Date dataCriacao, Date dataAtualizacao) {
		this.cliId = cliId;
		this.listOs = listOs;
		this.pessoa = pessoa;
		this.dataCriacao = dataCriacao;
		this.dataAtualizacao = dataAtualizacao;
	}

    public Cliente(Integer cliente) {
    }

    public Integer getCliId() {
		return cliId;
	}

	public void setCliId(Integer cliId) {
		this.cliId = cliId;
	}

	public List<OrdemDeServico> getListOs() {
		return listOs;
	}

	public void setListOs(List<OrdemDeServico> listOs) {
		this.listOs = listOs;
	}

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
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
