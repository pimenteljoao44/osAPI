package com.joao.osMarmoraria.domain;

import com.joao.osMarmoraria.domain.enums.TipoPessoa;
import com.joao.osMarmoraria.domain.interfaces.TipoPessoaValid;
import com.joao.osMarmoraria.dtos.ClienteDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TipoPessoaValidatorClient implements ConstraintValidator<TipoPessoaValid, ClienteDTO> {

    @Override
    public boolean isValid(ClienteDTO value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (value.getTipoPessoa() == TipoPessoa.PESSOA_FISICA) {

            return value.getCnpj() == null || value.getCnpj().isEmpty();
        } else {
            return value.getCnpj() != null && !value.getCnpj().isEmpty()
                    && value.getCpf() == null || value.getCpf().isEmpty();
        }
    }
}