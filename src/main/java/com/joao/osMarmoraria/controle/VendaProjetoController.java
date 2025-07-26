package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.dtos.VendaProjetoCreateDTO;
import com.joao.osMarmoraria.dtos.VendaProjetoDTO;
import com.joao.osMarmoraria.dtos.VendaProjetoUpdateDTO;
import com.joao.osMarmoraria.services.VendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/venda-projeto")
@CrossOrigin(origins = "*")
public class VendaProjetoController {

    @Autowired
    private VendaService vendaService;


    @PostMapping
    public ResponseEntity<VendaProjetoDTO> criarVendaProjeto(@Valid @RequestBody VendaProjetoCreateDTO createDTO) {
        try {
            VendaProjetoDTO vendaProjeto = vendaService.criarVendaProjeto(createDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(vendaProjeto);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendaProjetoDTO> buscarPorId(@PathVariable Integer id) {
        try {
            VendaProjetoDTO vendaProjeto = vendaService.buscarVendaProjetoPorId(id);
            return ResponseEntity.ok(vendaProjeto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<VendaProjetoDTO>> listarTodas(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer clienteId) {

        try {
            List<VendaProjetoDTO> vendas;

            if (clienteId != null) {
                vendas = vendaService.buscarVendasProjetosPorCliente(clienteId);
            } else if (status != null) {
                vendas = vendaService.buscarVendasProjetosPorStatus(status);
            } else {
                vendas = vendaService.listarVendasProjetos();
            }

            return ResponseEntity.ok(vendas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendaProjetoDTO> atualizar(@PathVariable Integer id,
                                                     @Valid @RequestBody VendaProjetoUpdateDTO updateDTO) {
        try {
            VendaProjetoDTO vendaAtualizada = vendaService.atualizarVendaProjeto(id, updateDTO);
            return ResponseEntity.ok(vendaAtualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== OPERAÇÕES ESPECÍFICAS ==========

    @PatchMapping("/{id}/efetivar")
    public ResponseEntity<VendaProjetoDTO> efetivarVenda(@PathVariable Integer id) {
        try {
            VendaProjetoDTO vendaEfetivada = vendaService.efetuarVendaProjeto(id);
            return ResponseEntity.ok(vendaEfetivada);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/gerar-os")
    public ResponseEntity<Map<String, String>> gerarOrdemServico(@PathVariable Integer id) {
        try {
            String resultado = vendaService.gerarOrdemServicoParaVendaProjeto(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", resultado);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno do servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/{id}/gerar-conta-receber")
    public ResponseEntity<Map<String, String>> gerarContaReceber(@PathVariable Integer id) {
        try {
            String resultado = vendaService.gerarContaReceberParaVendaProjeto(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", resultado);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno do servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ========== CONSULTAS ESPECÍFICAS ==========

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<VendaProjetoDTO>> buscarPorCliente(@PathVariable Integer clienteId) {
        try {
            List<VendaProjetoDTO> vendas = vendaService.buscarVendasProjetosPorCliente(clienteId);
            return ResponseEntity.ok(vendas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<VendaProjetoDTO>> buscarPorStatus(@PathVariable String status) {
        try {
            List<VendaProjetoDTO> vendas = vendaService.buscarVendasProjetosPorStatus(status);
            return ResponseEntity.ok(vendas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/orcamentos")
    public ResponseEntity<List<VendaProjetoDTO>> listarOrcamentos() {
        try {
            List<VendaProjetoDTO> orcamentos = vendaService.buscarVendasProjetosPorStatus("ORCAMENTO");
            return ResponseEntity.ok(orcamentos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/vendidas")
    public ResponseEntity<List<VendaProjetoDTO>> listarVendidas() {
        try {
            List<VendaProjetoDTO> vendidas = vendaService.buscarVendasProjetosPorStatus("VENDIDO");
            return ResponseEntity.ok(vendidas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== DASHBOARD E ESTATÍSTICAS ==========

    @GetMapping("/dashboard/estatisticas")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        try {
            List<VendaProjetoDTO> todasVendas = vendaService.listarVendasProjetos();

            Map<String, Object> estatisticas = new HashMap<>();

            long totalOrcamentos = todasVendas.stream()
                    .filter(v -> "ORCAMENTO".equals(v.getStatus()))
                    .count();

            long totalVendidas = todasVendas.stream()
                    .filter(v -> "VENDIDO".equals(v.getStatus()))
                    .count();

            long pendentesOS = todasVendas.stream()
                    .filter(v -> "VENDIDO".equals(v.getStatus()) && !v.getOrdemServicoGerada())
                    .count();

            long pendentesContaReceber = todasVendas.stream()
                    .filter(v -> "VENDIDO".equals(v.getStatus()) && !v.getContaReceberGerada())
                    .count();

            estatisticas.put("totalOrcamentos", totalOrcamentos);
            estatisticas.put("totalVendidas", totalVendidas);
            estatisticas.put("pendentesOS", pendentesOS);
            estatisticas.put("pendentesContaReceber", pendentesContaReceber);
            estatisticas.put("total", todasVendas.size());

            return ResponseEntity.ok(estatisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== TRATAMENTO DE ERROS ==========

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Erro interno do servidor");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

