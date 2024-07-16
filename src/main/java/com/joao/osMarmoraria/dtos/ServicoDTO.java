package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.Funcionario;
import com.joao.osMarmoraria.domain.OrdemDeServico;
import com.joao.osMarmoraria.domain.Servico;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
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

    public ServicoDTO(Servico obj) {
        this.id = obj.getId();
        this.servicoDescricao = obj.getServicoDescricao();
        this.servicoValor = obj.getServicoValor();
        this.servicoQuantidade = obj.getServicoQuantidade();
        this.ordensDeServico = obj.getOrdensDeServico().stream()
                .map(OrdemDeServico::getId)
                .collect(Collectors.toList());
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
