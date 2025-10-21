package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "pecas")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Peca implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String tipo;
    private Double largura;
    private Double altura;
    private Double espessura;
    private String unidade;
    private Double x;
    private Double y;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id")
    @JsonBackReference("projeto-pecas")
    private Projeto projeto;

    @ElementCollection
    @CollectionTable(name = "peca_recortes", joinColumns = @JoinColumn(name = "peca_id"))
    private List<Recorte> recortes;
}
