package com.joao.osMarmoraria.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.OneToMany;


@Entity
public class Funcionario extends Pessoa implements Serializable {
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	@OneToMany(mappedBy = "funcionario")
	private List <OrdemDeServico> listOs = new ArrayList<>();

	public Funcionario() {
		super();
	}

	public Funcionario(Integer id, String nome, String cpf, String telefone) {
		super(id, nome, cpf, telefone);
	}

	public List<OrdemDeServico> getListOs() {
		return listOs;
	}

	public void setListOs(List<OrdemDeServico> listOs) {
		this.listOs = listOs;
	}

}
