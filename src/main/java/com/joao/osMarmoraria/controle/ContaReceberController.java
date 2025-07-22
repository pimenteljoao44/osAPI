package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.dtos.ContaReceberDTO;
import com.joao.osMarmoraria.services.ContaReceberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/contas-receber")
@CrossOrigin(origins = "*")
@Validated
public class ContaReceberController {

    @Autowired
    private ContaReceberService contaReceberService;

    @GetMapping
    public ResponseEntity<List<ContaReceberDTO>> listarTodas() {
        List<ContaReceberDTO> contasReceber = contaReceberService.listarTodas();
        return ResponseEntity.ok(contasReceber);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaReceberDTO> buscarPorId(@PathVariable Integer id) {
        ContaReceberDTO contaReceber = contaReceberService.buscarPorId(id);
        return ResponseEntity.ok(contaReceber);
    }

    @PostMapping("/venda")
    public ResponseEntity<ContaReceberDTO> criarPorVenda(@Valid @RequestBody ContaReceberDTO contaReceberDTO) {
        ContaReceberDTO contaCriada = contaReceberService.criarPorVenda(contaReceberDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(contaCriada);
    }

    @PostMapping("/projeto")
    public ResponseEntity<ContaReceberDTO> criarPorProjeto(@Valid @RequestBody ContaReceberDTO contaReceberDTO) {
        ContaReceberDTO contaCriada = contaReceberService.criarPorProjeto(contaReceberDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(contaCriada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContaReceberDTO> atualizar(@PathVariable Integer id, @Valid @RequestBody ContaReceberDTO contaReceberDTO) {
        ContaReceberDTO contaAtualizada = contaReceberService.atualizar(id, contaReceberDTO);
        return ResponseEntity.ok(contaAtualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        contaReceberService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/receber")
    public ResponseEntity<ContaReceberDTO> marcarComoRecebida(
            @PathVariable Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataPagamento) {
        ContaReceberDTO contaRecebida = contaReceberService.marcarComoRecebida(id, dataPagamento);
        return ResponseEntity.ok(contaRecebida);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ContaReceberDTO>> buscarPorStatus(@PathVariable String status) {
        List<ContaReceberDTO> contas = contaReceberService.buscarPorStatus(status);
        return ResponseEntity.ok(contas);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ContaReceberDTO>> buscarContasReceberPorCliente(@PathVariable Integer clienteId) {
        List<ContaReceberDTO> contas = contaReceberService.buscarContasReceberPorCliente(clienteId);
        return ResponseEntity.ok(contas);
    }

    @GetMapping("/vencidas")
    public ResponseEntity<List<ContaReceberDTO>> buscarVencidas() {
        List<ContaReceberDTO> contasVencidas = contaReceberService.buscarVencidas();
        return ResponseEntity.ok(contasVencidas);
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<ContaReceberDTO>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dataFim) {
        List<ContaReceberDTO> contas = contaReceberService.buscarPorPeriodo(dataInicio, dataFim);
        return ResponseEntity.ok(contas);
    }

    @GetMapping("/total-pendente")
    public ResponseEntity<BigDecimal> calcularTotalPendente() {
        BigDecimal total = contaReceberService.calcularTotalPendente();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/total-recebido")
    public ResponseEntity<BigDecimal> calcularTotalRecebido() {
        BigDecimal total = contaReceberService.calcularTotalRecebido();
        return ResponseEntity.ok(total);
    }
}