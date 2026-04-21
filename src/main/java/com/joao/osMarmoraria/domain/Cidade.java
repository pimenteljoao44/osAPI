package com.joao.osMarmoraria.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "cidade")
@Data
@AllArgsConstructor
public class Cidade {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String nome;

    private String uf;

    public Cidade() {

    }

    public void setId(Object id) {
        if (id instanceof String) {
            this.id = Integer.parseInt((String) id);
        } else if (id instanceof Integer) {
            this.id = (Integer) id;
        }
    }


}
