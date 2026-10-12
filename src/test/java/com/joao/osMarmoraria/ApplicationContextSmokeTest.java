package com.joao.osMarmoraria;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Teste de fumaça: sobe o contexto Spring completo com banco H2 em memória.
 *
 * <p>É o teste mais barato com maior poder de detecção do projeto. Falha se:
 * um bean não puder ser construído (dependência ausente ou ciclo), uma
 * propriedade obrigatória não existir, ou um mapeamento JPA estiver inválido
 * (o {@code ddl-auto=create-drop} força a geração do schema inteiro a partir
 * das entidades).</p>
 *
 * <p>Serve de rede de segurança para as fases de refatoração: qualquer mudança
 * estrutural que quebre a montagem da aplicação é detectada aqui, antes de
 * chegar a runtime.</p>
 */
@SpringBootTest
@ActiveProfiles("h2")
class ApplicationContextSmokeTest {

    @Test
    void contextoDaAplicacaoSobeCompleto() {
        // A asserção é implícita: se o contexto não subir, o teste falha.
    }
}
