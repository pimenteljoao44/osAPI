package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.dtos.VendaProjetoDTO;
import com.joao.osMarmoraria.services.VendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/venda-projeto")
@Validated
public class VendaProjetoController {

    @Autowired
    private VendaService vendaService;


    @PostMapping
    public ResponseEntity<VendaProjetoDTO> criarVendaProjeto(@Valid @RequestBody VendaProjetoDTO vendaProjetoDTO) {
        VendaProjetoDTO novaVenda = vendaService.criarVendaProjeto(vendaProjetoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaVenda);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendaProjetoDTO> buscarVendaProjetoPorId(@PathVariable Integer id) {
        VendaProjetoDTO vendaProjeto = vendaService.buscarVendaProjetoPorId(id);
        return ResponseEntity.ok(vendaProjeto);
    }

    @GetMapping
    public ResponseEntity<List<VendaProjetoDTO>> listarVendasProjetos() {
        List<VendaProjetoDTO> vendasProjetos = vendaService.listarVendasProjetos();
        return ResponseEntity.ok(vendasProjetos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendaProjetoDTO> atualizarVendaProjeto(@PathVariable Integer id, @Valid @RequestBody VendaProjetoDTO vendaProjetoDTO) {
        vendaProjetoDTO.setId(id);
        VendaProjetoDTO vendaAtualizada = vendaService.atualizarVendaProjeto(vendaProjetoDTO);
        return ResponseEntity.ok(vendaAtualizada);
    }

    @PatchMapping("/{id}/efetuar")
    public ResponseEntity<VendaProjetoDTO> efetuarVendaProjeto(@PathVariable Integer id) {
        VendaProjetoDTO vendaEfetivada = vendaService.efetuarVendaProjeto(id);
        return ResponseEntity.ok(vendaEfetivada);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<VendaProjetoDTO>> buscarVendasProjetosPorCliente(@PathVariable Integer clienteId) {
        List<VendaProjetoDTO> vendasProjetos = vendaService.buscarVendasProjetosPorCliente(clienteId);
        return ResponseEntity.ok(vendasProjetos);
    }

    @PostMapping("/{id}/gerar-os")
    public ResponseEntity<String> gerarOrdemServicoParaVendaProjeto(@PathVariable Integer id) {
        try {
            String resultado = vendaService.gerarOrdemServicoParaVendaProjeto(id);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/gerar-conta-receber")
    public ResponseEntity<String> gerarContaReceberParaVendaProjeto(@PathVariable Integer id) {
        try {
            String resultado = vendaService.gerarContaReceberParaVendaProjeto(id);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

