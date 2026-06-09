package com.joao.osMarmoraria.services;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.repository.*;
import org.springframework.stereotype.Service;

import com.joao.osMarmoraria.domain.enums.Prioridade;
import com.joao.osMarmoraria.domain.enums.Status;
@Service
@RequiredArgsConstructor
public class DBservice {
	private final FuncionarioRepository funcionarioRepository;

	private final ClienteRepository clienteRepository;

	private final OrdemServicoRepository osRepository;

	private final EnderecoRepository enderecoRepository;

	private final FornecedorRepository fornecedorRepository;

	private final ProdutoRepository produtoRepository;

	public void instanciaDB() {

	}

}
