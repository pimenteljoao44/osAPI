package com.joao.osMarmoraria.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
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

    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // Para evitar loop no toString()
    private List<OrdemServico> ordensDeServico = new ArrayList<>();

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
