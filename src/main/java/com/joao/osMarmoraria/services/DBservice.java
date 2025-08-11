package com.joao.osMarmoraria.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.joao.osMarmoraria.domain.enums.Prioridade;
import com.joao.osMarmoraria.domain.enums.Status;
@Service
public class DBservice {
	@Autowired
	private FuncionarioRepository funcionarioRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private OrdemServicoRepository osRepository;

	@Autowired
	private EnderecoRepository enderecoRepository;

	@Autowired
	private FornecedorRepository fornecedorRepository;

	@Autowired
	private ProdutoRepository produtoRepository;

	public void instanciaDB() {

	}

}
