package com.joao.osMarmoraria.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.joao.osMarmoraria.domain.OrdemDeServico;
import com.joao.osMarmoraria.domain.enums.Prioridade;
import com.joao.osMarmoraria.domain.enums.Status;

import javax.validation.constraints.NotEmpty;

public class OsDTO implements Serializable {

	private static final long serialVersionUID = 1L;

private Integer id;
    @JsonFormat(pattern="dd/MM/yyyy HH:mm")
    private LocalDateTime dataAbertura;
    
    @JsonFormat(pattern="dd/MM/yyyy HH:mm")
	private LocalDateTime dataFechamento;
    
	private Integer prioridade;
	private Integer status;
	private Integer funcionario;
	private Integer cliente;
	
	@NotEmpty(message = "O campo observações é requerido!")
	private String observacoes;
	
	
	public OsDTO() {
		super();
	}


	public OsDTO(OrdemDeServico obj) {
		super();
		this.id = obj.getId();
		this.dataAbertura = obj.getDataAbertura();
		this.dataFechamento = obj.getDataFechamento();
		this.prioridade = obj.getPrioridade().getCod();
		this.status = obj.getStatus().getCod();
		this.funcionario = obj.getFuncionario().getId();
		this.cliente = obj.getCliente().getId();
		this.observacoes = obj.getObservacoes();
	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public LocalDateTime getDataAbertura() {
		return dataAbertura;
	}


	public void setDataAbertura(LocalDateTime dataAbertura) {
		this.dataAbertura = dataAbertura;
	}


	public LocalDateTime getDataFechamento() {
		return dataFechamento;
	}


	public void setDataFechamento(LocalDateTime dataFechamento) {
		this.dataFechamento = dataFechamento;
	}


	public Prioridade getPrioridade() {
		return Prioridade.toEnum(this.prioridade);
	}


	public void setPrioridade(Integer prioridade) {
		this.prioridade = prioridade;
	}


	public Status getStatus() {
		return Status.toEnum(this.status);
	}


	public void setStatus(Integer status) {
		this.status = status;
	}


	public Integer getFuncionario() {
		return funcionario;
	}


	public void setFuncionario(Integer funcionario) {
		this.funcionario = funcionario;
	}


	public Integer getCliente() {
		return cliente;
	}


	public void setCliente(Integer cliente) {
		this.cliente = cliente;
	}


	public String getObservacoes() {
		return observacoes;
	}


	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}
	
	
}
