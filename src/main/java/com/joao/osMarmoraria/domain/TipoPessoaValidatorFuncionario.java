package com.joao.osMarmoraria.domain;

import com.joao.osMarmoraria.domain.enums.TipoPessoa;
import com.joao.osMarmoraria.domain.interfaces.TipoPessoaValid;
import com.joao.osMarmoraria.dtos.ClienteDTO;
import com.joao.osMarmoraria.dtos.FuncionarioDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TipoPessoaValidatorFuncionario implements ConstraintValidator<TipoPessoaValid, FuncionarioDTO> {

    @Override
    public boolean isValid(FuncionarioDTO value, ConstraintValidatorContext context) {
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
