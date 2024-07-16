package com.joao.osMarmoraria.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.joao.osMarmoraria.domain.enums.Prioridade;
import com.joao.osMarmoraria.domain.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;


import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
public class OrdemDeServico {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@JsonFormat(pattern="dd/MM/yyyy HH:mm")
	private LocalDateTime dataAbertura;

	private LocalDateTime dataFechamento;

	private Integer prioridade;

	private Integer status;

	@ManyToOne
	@JoinColumn(name ="funcionario_id")
	private Funcionario funcionario;

	@ManyToOne
	@JoinColumn(name ="cliente_id")
	private Cliente cliente;

	private String observacoes;

	private String descricao;

	private BigDecimal valorTotal = BigDecimal.ZERO;

	private BigDecimal desconto = BigDecimal.ZERO;
	@OneToMany(mappedBy = "ordemDeServico")
	private List<Produto> produtos;

	@ManyToMany
	@JoinTable(
			name = "ordem_servico_servico",
			joinColumns = @JoinColumn(name = "os_id"),
			inverseJoinColumns = @JoinColumn(name = "servico_id")
	)
	private List<Servico> servicos;



	public OrdemDeServico() {
		super();
		this.setDataAbertura(LocalDateTime.now());
		this.setPrioridade(Prioridade.BAIXA);
		this.setStatus(Status.ABERTO);
	}



	public void addItem(Produto item) throws Exception {
		item.setOrdemDeServico(this);
		if (!produtos.contains(item)) {
			item.setPreco(item.getPreco());
			produtos.add(item);
			calculaTotal();
		} else {
			throw new Exception("O produto "
					+ item.getNome()
					+ " já está adicionado na ordem de serviço");
		}
	}

	public void addServico(Servico item) throws Exception {
		if (!servicos.contains(item)) {
			item.setServicoValor(item.getServicoValor());
			servicos.add(item);
			calculaTotal();
		} else {
			throw new Exception("O servico "
					+ item.getServicoDescricao()
					+ " já está adicionado na ordem de serviço");
		}
	}

	public void removerServico(Servico item) {
		if (servicos.contains(item)) {
			servicos.remove(item);
			calculaTotal();
		}
	}

	public void removeItem(Produto item) {
		produtos.remove(item);
		calculaTotal();
		item.estornarEstoque(item.getQuantidade());
	}

	public BigDecimal calculaTotal() {
		if (desconto == null) {
			desconto = BigDecimal.ZERO;
		}

		valorTotal = BigDecimal.ZERO;

		if (produtos != null && !produtos.isEmpty()) {
			for (Produto p : produtos) {
				valorTotal = valorTotal.add(p.getPreco().multiply(p.getQuantidade())).add(calculaServicos());
			}
		}

		return valorTotal = valorTotal.subtract(desconto);
	}
	public BigDecimal calculaServicos() {
		if (desconto == null || desconto.compareTo(valorTotal) >= 0) {
			desconto = BigDecimal.ZERO;
		}
		valorTotal = BigDecimal.ZERO;
		for (Servico s : servicos) {
			valorTotal = valorTotal.add(s.getServicoValor().multiply(s.getServicoQuantidade()));
		}
		valorTotal = valorTotal.subtract(desconto);

		return valorTotal;
	}

	public Prioridade getPrioridade() {
		return Prioridade.toEnum(this.prioridade);
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}

	public void setPrioridade(Prioridade prioridade) {
		this.prioridade = prioridade.getCod();
	}

	public Status getStatus() {
		return Status.toEnum(this.status);
	}

	public void setStatus(Status status) {
		this.status = status.getCod();
	}

	@Override
	public String toString() {
		return "OrdemDeServico{" +
				"id=" + id +
				", dataAbertura=" + dataAbertura +
				", dataFechamento=" + dataFechamento +
				", prioridade=" + prioridade +
				", status=" + status +
				", funcionario=" + funcionario +
				", cliente=" + cliente +
				", observacoes='" + observacoes + '\'' +
				", descricao='" + descricao + '\'' +
				", valorTotal=" + valorTotal +
				", desconto=" + desconto +
				", produtos=" + produtos +
				", servicos=" + servicos +
				'}';
	}
}
