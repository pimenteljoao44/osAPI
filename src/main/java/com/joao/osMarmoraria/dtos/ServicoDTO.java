package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.Funcionario;
import com.joao.osMarmoraria.domain.OrdemServico;
import com.joao.osMarmoraria.domain.Servico;

import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ServicoDTO implements Serializable {
    private Integer id;
    private String servicoDescricao;

    private BigDecimal servicoValor;

    private BigDecimal servicoQuantidade = BigDecimal.ZERO;

    private List<Integer> ordensDeServico;

    @ManyToOne
    private Funcionario funcionario;

    public ServicoDTO(Servico servico) {
        this.id = servico.getId();
        this.servicoDescricao = servico.getServicoDescricao();
        this.servicoValor = servico.getServicoValor();
        this.servicoQuantidade = servico.getServicoQuantidade();
        this.ordensDeServico = servico.getOrdensDeServico().stream()
                .map(OrdemServico::getId).collect(Collectors.toList());
        this.funcionario = servico.getFuncionario();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getServicoDescricao() {
        return servicoDescricao;
    }

    public void setServicoDescricao(String servicoDescricao) {
        this.servicoDescricao = servicoDescricao;
    }

    public BigDecimal getServicoValor() {
        return servicoValor;
    }

    public void setServicoValor(BigDecimal servicoValor) {
        this.servicoValor = servicoValor;
    }

    public BigDecimal getServicoQuantidade() {
        return servicoQuantidade;
    }

    public void setServicoQuantidade(BigDecimal servicoQuantidade) {
        this.servicoQuantidade = servicoQuantidade;
    }

    public List<Integer> getOrdensDeServico() {
        return ordensDeServico;
    }

    public void setOrdensDeServico(List<Integer> ordensDeServico) {
        this.ordensDeServico = ordensDeServico;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }
}
