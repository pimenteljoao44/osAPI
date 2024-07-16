package com.joao.osMarmoraria.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String servicoDescricao;

    private BigDecimal servicoValor;

    private BigDecimal servicoQuantidade = BigDecimal.ZERO;

    @ManyToMany(mappedBy = "servicos")
    private List<OrdemDeServico> ordensDeServico;

    @ManyToOne
    private Funcionario funcionario;

    public Servico() {
    }

    @Override
    public String toString() {
        return "Servico{" +
                "id=" + id +
                ", servicoDescricao='" + servicoDescricao + '\'' +
                ", servicoValor=" + servicoValor +
                ", servicoQuantidade=" + servicoQuantidade +
                ", ordensDeServico=" + ordensDeServico +
                ", funcionario=" + funcionario +
                '}';
    }
}
