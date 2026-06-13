package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.Cliente;
import com.joao.osMarmoraria.domain.Pessoa;
import com.joao.osMarmoraria.domain.Produto;
import com.joao.osMarmoraria.domain.Usuario;
import com.joao.osMarmoraria.domain.enums.NivelAcesso;
import com.joao.osMarmoraria.domain.enums.TipoProjeto;
import com.joao.osMarmoraria.domain.enums.UnidadeDeMedida;
import com.joao.osMarmoraria.dtos.ProjetoDTO;
import com.joao.osMarmoraria.dtos.ProjetoItemDTO;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de integração de {@link ProjetoService#criarProjeto}, exercitando o
 * fluxo ponta a ponta contra o H2 (perfil "h2").
 *
 * <p>Existe por causa de uma regressão real: o {@code ProjetoMapper} passou a
 * ler o produto pela associação carregada ({@code item.getProduto()}) em vez de
 * re-buscá-lo. Como a associação {@code produto} de {@link com.joao.osMarmoraria.domain.ProjetoItem}
 * é read-only e {@code salvarItens} só gravava o {@code produtoId} cru, dentro
 * da mesma transação da criação o item recém-salvo ficava no cache de 1º nível
 * com {@code produto == null} — o {@code LEFT JOIN FETCH} não repõe a associação
 * de uma entidade já gerenciada. Resultado: NPE/erro ao montar o DTO.</p>
 *
 * <p>Um teste de unidade com mocks NÃO pegaria isso: o bug vive no ciclo de
 * vida do Hibernate (persistence context), não na lógica pura. Daí a integração.</p>
 */
@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class ProjetoServiceIntegrationTest {

    @Autowired
    private ProjetoService projetoService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void criarProjeto_devolveItemComNomeDoProdutoPreenchido() {
        Integer clienteId = seedCliente("Cliente Teste");
        Integer usuarioId = seedUsuario("operador");
        Integer produtoId = seedProduto("Granito Preto São Gabriel");

        ProjetoDTO salvo = projetoService.criarProjeto(
                novoProjetoComItem(clienteId, usuarioId, produtoId));

        assertNotNull(salvo.getId(), "o projeto deveria ter sido persistido");
        assertEquals(1, salvo.getItens().size());

        ProjetoItemDTO item = salvo.getItens().get(0);
        assertEquals(produtoId, item.getProdutoId());
        // O coração do teste: o nome do produto tem que voltar preenchido.
        // Antes do fix, a associação read-only vinha nula e isto quebrava.
        assertEquals("Granito Preto São Gabriel", item.getProdutoNome());
    }

    @Test
    void criarProjeto_comProdutoInexistente_falhaRapidoAntesDePersistir() {
        Integer clienteId = seedCliente("Cliente Teste");
        Integer usuarioId = seedUsuario("operador");
        Integer produtoInexistente = 999_999;

        ObjectNotFoundException ex = assertThrows(ObjectNotFoundException.class,
                () -> projetoService.criarProjeto(
                        novoProjetoComItem(clienteId, usuarioId, produtoInexistente)));

        assertTrue(ex.getMessage().contains(String.valueOf(produtoInexistente)),
                "a mensagem deve identificar o produto inválido");
    }

    // ---- helpers de seed (grafo mínimo válido) ----

    private Integer seedCliente(String nome) {
        Pessoa pessoa = new Pessoa();
        pessoa.setNome(nome);
        em.persist(pessoa); // cascade de Cliente é MERGE/REMOVE, não PERSIST

        Cliente cliente = new Cliente();
        cliente.setPessoa(pessoa);
        em.persist(cliente);
        return cliente.getCliId();
    }

    private Integer seedUsuario(String login) {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuário " + login);
        usuario.setLogin(login);
        usuario.setSenha("senha-irrelevante");
        usuario.setEmail(login + "@teste.com");
        usuario.setNivelAcesso(NivelAcesso.FUNCIONARIO);
        em.persist(usuario);
        return usuario.getId();
    }

    private Integer seedProduto(String nome) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setUnidadeDeMedida(UnidadeDeMedida.METRO_QUADRADO);
        produto.setPrecoCusto(new BigDecimal("100.00"));
        produto.setPrecoVenda(new BigDecimal("150.00"));
        em.persist(produto);
        return produto.getProdId();
    }

    private ProjetoDTO novoProjetoComItem(Integer clienteId, Integer usuarioId, Integer produtoId) {
        ProjetoItemDTO item = new ProjetoItemDTO();
        item.setProdutoId(produtoId);
        item.setQuantidade(new BigDecimal("2.000"));
        item.setValorUnitario(new BigDecimal("150.00"));

        ProjetoDTO dto = new ProjetoDTO();
        dto.setNome("Projeto Bancada " + System.nanoTime()); // evita colisão de nome+cliente
        dto.setClienteId(clienteId);
        dto.setUsuarioCriacao(usuarioId);
        dto.setTipoProjeto(TipoProjeto.BANCADA);
        // O cliente (frontend) envia o total já calculado; o service o persiste
        // antes de recalcular, e a entidade Projeto valida valorTotal > 0.
        dto.setValorTotal(new BigDecimal("300.00"));
        dto.setItens(List.of(item));
        return dto;
    }
}
