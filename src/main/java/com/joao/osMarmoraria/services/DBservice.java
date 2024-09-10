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
	private OsRepository osRepository;

	@Autowired
	private EnderecoRepository enderecoRepository;

	@Autowired
	private FornecedorRepository fornecedorRepository;

	@Autowired
	private ProdutoRepository produtoRepository;

	public void instanciaDB() {
		// Criar e persistir um novo Endereco
		Endereco endereco = new Endereco();
		endereco = enderecoRepository.save(endereco);

		// Criar e associar Pessoa e Funcionario
		Pessoa pessoa1 = new Pessoa(null, "João", endereco, "(44)99963-2007");
		Funcionario f1 = new Funcionario(null, "Cargo", new BigDecimal("2000.00"), "12345", pessoa1, new ArrayList<>(), new Date(), new Date());

		// Persistir o Funcionario
		funcionarioRepository.save(f1);

		// Criar e associar Pessoa e Cliente
		Pessoa pessoa2 = new Pessoa(null, "Betina", endereco, "(44)99963-8888");
		Cliente c1 = new Cliente(null, new ArrayList<>(), pessoa2, new Date(), new Date());

		// Persistir o Cliente
		clienteRepository.save(c1);

		// Criar Servico e Grupo
		Servico s1 = new Servico(1, "Montagem cuba", new BigDecimal(600), new BigDecimal(1), null, null);
		Grupo gpPai = new Grupo(1, "Granito", true, null);
		Grupo g1 = new Grupo(2, "Granito trabalhado", true, gpPai);

		// Criar e associar Fornecedor e Produto
		Fornecedor for1 = new Fornecedor();
		for1.setPessoa(pessoa1);
		for1 = fornecedorRepository.save(for1);

		Produto p1 = new Produto();
		p1.setProdId(1);
		p1.setNome("Granito Branco");
		p1.setPrecoCusto(new BigDecimal(300));
		p1.setAtivo(true);
		p1.setEstoque(new BigDecimal(50));
		p1.setQuantidade(new BigDecimal(2));
		p1.setGrupo(g1);
		p1.setFornecedor(for1);
		p1 = produtoRepository.save(p1);

		// Criar e associar OrdemDeServico
		OrdemDeServico os1 = new OrdemDeServico();
		os1.setId(1);
		os1.setDataAbertura(LocalDateTime.now());
		os1.setDataFechamento(null);
		os1.setDesconto(BigDecimal.ZERO);
		os1.setValorTotal(new BigDecimal(2000));
		os1.setFuncionario(f1);
		os1.setCliente(c1);
		os1.setDescricao("esta é uma descricao de o.s");
		os1.setProdutos(List.of(p1));
		os1.setServicos(List.of(s1));

		f1.getListOs().add(os1);
		c1.getListOs().add(os1);

		// Persistir a OrdemDeServico
		osRepository.save(os1);
	}

}
