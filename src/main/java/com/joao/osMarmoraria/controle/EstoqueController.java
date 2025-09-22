package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.domain.EstoqueReservado;
import com.joao.osMarmoraria.domain.MovimentacaoEstoque;
import com.joao.osMarmoraria.services.EstoqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estoque")
@CrossOrigin(origins = "*")
public class EstoqueController {

    @Autowired
    private EstoqueService estoqueService;

    @GetMapping("/disponivel/{produtoId}")
    public ResponseEntity<Map<String, Object>> consultarEstoqueDisponivel(@PathVariable Integer produtoId) {
        try {
            BigDecimal estoqueDisponivel = estoqueService.calcularEstoqueDisponivel(produtoId);
            BigDecimal estoqueReservado = estoqueService.calcularEstoqueReservado(produtoId);

            Map<String, Object> response = new HashMap<>();
            response.put("produtoId", produtoId);
            response.put("estoqueDisponivel", estoqueDisponivel);
            response.put("estoqueReservado", estoqueReservado);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/historico/{produtoId}")
    public ResponseEntity<List<MovimentacaoEstoque>> obterHistoricoMovimentacao(@PathVariable Integer produtoId) {
        try {
            List<MovimentacaoEstoque> historico = estoqueService.obterHistoricoMovimentacao(produtoId);
            return ResponseEntity.ok(historico);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/reservas/{produtoId}")
    public ResponseEntity<List<EstoqueReservado>> obterReservasAtivas(@PathVariable Integer produtoId) {
        try {
            List<EstoqueReservado> reservas = estoqueService.obterReservasAtivas(produtoId);
            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/entrada")
    public ResponseEntity<Map<String, Object>> entradaEstoque(
            @RequestParam Integer produtoId,
            @RequestParam BigDecimal quantidade,
            @RequestParam(required = false) String observacao,
            @RequestParam(required = false) Integer usuarioId) {
        try {
            estoqueService.entradaEstoque(produtoId, quantidade, observacao, usuarioId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Entrada de estoque realizada com sucesso");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/saida")
    public ResponseEntity<Map<String, Object>> saidaEstoque(
            @RequestParam Integer produtoId,
            @RequestParam BigDecimal quantidade,
            @RequestParam(required = false) String observacao,
            @RequestParam(required = false) Integer usuarioId) {
        try {
            estoqueService.saidaEstoque(produtoId, quantidade, observacao, usuarioId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Saída de estoque realizada com sucesso");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/reservar")
    public ResponseEntity<Map<String, Object>> reservarMaterial(
            @RequestParam Integer vendaId,
            @RequestParam Integer projetoId) {
        try {
            estoqueService.reservarMaterialParaVenda(vendaId, projetoId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Materiais reservados com sucesso");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/liberar-reserva")
    public ResponseEntity<Map<String, Object>> liberarReserva(@RequestParam Integer vendaId) {
        try {
            estoqueService.liberarReservaVenda(vendaId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reserva liberada com sucesso");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/baixar-os")
    public ResponseEntity<Map<String, Object>> baixarEstoqueOS(@RequestParam Integer ordemServicoId) {
        try {
            estoqueService.baixarEstoqueOrdemServico(ordemServicoId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Estoque baixado com sucesso");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/limpar-expiradas")
    public ResponseEntity<Map<String, Object>> limparReservasExpiradas() {
        try {
            estoqueService.limparReservasExpiradas();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reservas expiradas limpas com sucesso");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
