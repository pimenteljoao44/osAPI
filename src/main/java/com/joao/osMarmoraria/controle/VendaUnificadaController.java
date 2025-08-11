package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.dtos.*;
import com.joao.osMarmoraria.services.VendaUnificadaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping(value = "/api/venda-unificada")
public class VendaUnificadaController {

    @Autowired
    private VendaUnificadaService vendaUnificadaService;

    // ========== OPERAÇÕES CRUD ==========

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<VendaUnificadaDTO> criarVenda(@Valid @RequestBody VendaUnificadaCreateDTO createDTO) {
        try {
            VendaUnificadaDTO venda = vendaUnificadaService.criarVenda(createDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(venda);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<VendaUnificadaDTO> buscarPorId(@PathVariable Integer id) {
        try {
            VendaUnificadaDTO venda = vendaUnificadaService.buscarPorId(id);
            return ResponseEntity.ok(venda);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<VendaUnificadaDTO>> listarTodas(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) String status) {

        try {
            List<VendaUnificadaDTO> vendas = vendaUnificadaService.listarVendas(tipo, clienteId, status);
            return ResponseEntity.ok(vendas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<VendaUnificadaDTO> atualizar(@PathVariable Integer id,
                                                       @Valid @RequestBody VendaUnificadaUpdateDTO updateDTO) {
        try {
            VendaUnificadaDTO vendaAtualizada = vendaUnificadaService.atualizarVenda(id, updateDTO);
            return ResponseEntity.ok(vendaAtualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== OPERAÇÕES ESPECÍFICAS ==========

    @PatchMapping("/{id}/efetivar")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<VendaUnificadaDTO> efetivarVenda(@PathVariable Integer id) {
        try {
            VendaUnificadaDTO vendaEfetivada = vendaUnificadaService.efetivarVenda(id);
            return ResponseEntity.ok(vendaEfetivada);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/gerar-conta-receber")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Map<String, String>> gerarContaReceber(@PathVariable Integer id) {
        try {
            String resultado = vendaUnificadaService.gerarContaReceber(id);
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

    @PostMapping("/{id}/gerar-ordem-servico")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Map<String, String>> gerarOrdemServico(@PathVariable Integer id) {
        try {
            String resultado = vendaUnificadaService.gerarOrdemServico(id);
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

    // ========== OPERAÇÕES DE ITENS (PARA VENDAS DE PRODUTO) ==========

    @PostMapping("/{id}/adicionar-item")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<VendaUnificadaDTO> adicionarItem(@PathVariable Integer id,
                                                           @Valid @RequestBody ItemVendaCreateDTO itemDTO) {
        try {
            VendaUnificadaDTO vendaAtualizada = vendaUnificadaService.adicionarItem(id, itemDTO);
            return ResponseEntity.ok(vendaAtualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}/remover-item/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<VendaUnificadaDTO> removerItem(@PathVariable Integer id,
                                                         @PathVariable Integer itemId) {
        try {
            VendaUnificadaDTO vendaAtualizada = vendaUnificadaService.removerItem(id, itemId);
            return ResponseEntity.ok(vendaAtualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/atualizar-item/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<VendaUnificadaDTO> atualizarItem(@PathVariable Integer id,
                                                           @PathVariable Integer itemId,
                                                           @Valid @RequestBody ItemVendaUpdateDTO itemDTO) {
        try {
            VendaUnificadaDTO vendaAtualizada = vendaUnificadaService.atualizarItem(id, itemId, itemDTO);
            return ResponseEntity.ok(vendaAtualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== CONSULTAS ESPECÍFICAS ==========

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<VendaUnificadaDTO>> buscarPorCliente(@PathVariable Integer clienteId) {
        try {
            List<VendaUnificadaDTO> vendas = vendaUnificadaService.buscarPorCliente(clienteId);
            return ResponseEntity.ok(vendas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/produtos")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<VendaUnificadaDTO>> listarVendasProdutos() {
        try {
            List<VendaUnificadaDTO> vendas = vendaUnificadaService.listarVendasProdutos();
            return ResponseEntity.ok(vendas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/projetos")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<VendaUnificadaDTO>> listarVendasProjetos() {
        try {
            List<VendaUnificadaDTO> vendas = vendaUnificadaService.listarVendasProjetos();
            return ResponseEntity.ok(vendas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/orcamentos")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<VendaUnificadaDTO>> listarOrcamentos() {
        try {
            List<VendaUnificadaDTO> orcamentos = vendaUnificadaService.listarOrcamentos();
            return ResponseEntity.ok(orcamentos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== DASHBOARD E ESTATÍSTICAS ==========

    @GetMapping("/dashboard/estatisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        try {
            Map<String, Object> estatisticas = vendaUnificadaService.obterEstatisticas();
            return ResponseEntity.ok(estatisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== PROCESSAMENTO COMPLETO ==========

    @PostMapping("/{id}/processar-completa")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Map<String, Object>> processarVendaCompleta(@PathVariable Integer id) {
        try {
            Map<String, Object> resultado = vendaUnificadaService.processarVendaCompleta(id);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("success", false);
            erro.put("message", "Erro interno no processamento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }
}

