package com.joao.osMarmoraria.controle.exceptions;

import com.joao.osMarmoraria.exceptions.DeletionRestrictedException;
import com.joao.osMarmoraria.services.exceptions.ContasPagarJaGeradasException;
import com.joao.osMarmoraria.services.exceptions.ContasReceberJaGeradasException;
import com.joao.osMarmoraria.services.exceptions.DataIntegratyViolationException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import com.joao.osMarmoraria.services.exceptions.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Fixa o contrato de erro da API: cada família de exceção tem um status HTTP
 * previsível e todas saem no envelope {@link StandardError}. Se um mapeamento
 * mudar sem intenção, este teste quebra antes de o cliente da API descobrir.
 */
class ResourceExceptionHandlerTest {

    private final ResourceExceptionHandler handler = new ResourceExceptionHandler();

    @Test
    void recursoNaoEncontradoResponde404ComEnvelopePadrao() {
        ResponseEntity<StandardError> resposta =
                handler.recursoNaoEncontrado(new ObjectNotFoundException("Venda não encontrada"));

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(404, resposta.getBody().getStatus());
        assertEquals("Venda não encontrada", resposta.getBody().getError());
    }

    @Test
    void regraDeNegocioResponde409() {
        ResponseEntity<StandardError> resposta =
                handler.regraDeNegocioViolada(new RegraDeNegocioException("Operação conflita com o estado atual"));

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(409, resposta.getBody().getStatus());
    }

    @Test
    void filhasDeRegraDeNegocioCaemNoMesmoMapeamento() {
        // O handler captura o tipo-base: toda subclasse nova já nasce mapeada
        // para 409 sem precisar de um @ExceptionHandler próprio (Open/Closed).
        ResponseEntity<StandardError> contasReceber =
                handler.regraDeNegocioViolada(new ContasReceberJaGeradasException(7));
        ResponseEntity<StandardError> contasPagar =
                handler.regraDeNegocioViolada(new ContasPagarJaGeradasException(3));
        ResponseEntity<StandardError> exclusao =
                handler.regraDeNegocioViolada(new DeletionRestrictedException("Usuário possui vínculos"));

        assertEquals(HttpStatus.CONFLICT, contasReceber.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, contasPagar.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, exclusao.getStatusCode());
        assertEquals("Contas a receber já foram geradas para a venda 7",
                contasReceber.getBody().getError());
    }

    @Test
    void violacaoDeIntegridadeResponde400() {
        ResponseEntity<StandardError> resposta =
                handler.violacaoDeIntegridade(new DataIntegratyViolationException("Usuário já cadastrado"));

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(400, resposta.getBody().getStatus());
    }
}
