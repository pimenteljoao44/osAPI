package com.joao.osMarmoraria.domain.interfaces;

import com.joao.osMarmoraria.domain.TipoPessoaValidatorClient;
import com.joao.osMarmoraria.domain.TipoPessoaValidatorFuncionario;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {TipoPessoaValidatorClient.class, TipoPessoaValidatorFuncionario.class})
@Documented
public @interface TipoPessoaValid {
    String message() default "Tipo de pessoa inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
