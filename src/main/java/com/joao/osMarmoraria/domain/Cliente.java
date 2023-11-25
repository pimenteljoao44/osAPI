package com.joao.osMarmoraria.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

@Entity
public class Cliente extends Pessoa{
	@OneToMany(mappedBy = "cliente")
	private List <OrdemDeServico> listOs = new ArrayList<>();

	public Cliente() {
		super();
		
	}

	public Cliente(Integer id, String nome, String cpf, String telefone) {
		super(id, nome, cpf, telefone);
		
	}

	public List<OrdemDeServico> getListOs() {
		return listOs;
	}

	public void setListOs(List<OrdemDeServico> listOs) {
		this.listOs = listOs;
	}
	

}
