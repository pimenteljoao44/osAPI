package com.joao.osMarmoraria.dtos;

import com.joao.osMarmoraria.domain.Endereco;
import lombok.Data;

@Data
public class EnderecoDTO {
    private String rua;
    private String numero;
    private String complemento;
    private String bairro;
    private CidadeInputDTO cidade;
    private EstadoInputDTO estado;

    public EnderecoDTO(){

    }

    public EnderecoDTO(Endereco endereco) {
        this.rua = endereco.getRua();
        this.numero = endereco.getNumero();
        this.complemento = endereco.getComplemento();
        this.bairro = endereco.getBairro();
        this.cidade = new CidadeInputDTO(endereco.getCidade());
        this.estado = new EstadoInputDTO(endereco.getEstado());
    }
}
