package com.joao.osMarmoraria.domain;

import com.joao.osMarmoraria.domain.enums.TipoAcabamento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "projeto_acabamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjetoAcabamento implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(precision = 12, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private TipoAcabamento tipo;
}
