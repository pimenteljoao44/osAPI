package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.domain.ContaPagar;
import com.joao.osMarmoraria.dtos.ContaPagarDTO;
import com.joao.osMarmoraria.services.ContaPagarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat; // 1. Importe a anotação correta
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate; // 2. Importe LocalDate
import java.util.List;

@RestController
@RequestMapping("/api/contas-pagar" )
@CrossOrigin(origins = "*") // Lembre-se que em produção é melhor restringir a origem
public class ContaPagarController {

    @Autowired
    private ContaPagarService contaPagarService;

    @GetMapping
    public ResponseEntity<List<ContaPagarDTO>> listarTodas() {
        List<ContaPagarDTO> contasPagar = contaPagarService.listarTodas();
        return ResponseEntity.ok(contasPagar);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaPagarDTO> buscarPorId(@PathVariable Integer id) {
        ContaPagarDTO contaPagar = contaPagarService.buscarPorId(id);
        return ResponseEntity.ok(contaPagar);
    }

    @PostMapping
    public ResponseEntity<ContaPagarDTO> criar(@Valid @RequestBody ContaPagarDTO contaPagarDTO) {
        ContaPagar contaCriada = contaPagarService.criar(contaPagarDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ContaPagarDTO(contaCriada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContaPagarDTO> atualizar(@PathVariable Integer id, @Valid @RequestBody ContaPagarDTO contaPagarDTO) {
        ContaPagarDTO contaAtualizada = contaPagarService.atualizar(id, contaPagarDTO);
        return ResponseEntity.ok(contaAtualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        contaPagarService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // --- INÍCIO DA CORREÇÃO ---
    @PatchMapping("/{id}/pagar")
    public ResponseEntity<ContaPagarDTO> marcarComoPaga(
            @PathVariable Integer id,
            // 3. Use @DateTimeFormat e especifique o padrão ISO para data (yyyy-MM-dd)
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataPagamento) {
        ContaPagarDTO contaPaga = contaPagarService.marcarComoPaga(id, dataPagamento);
        return ResponseEntity.ok(contaPaga);
    }
    // --- FIM DA CORREÇÃO ---

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ContaPagarDTO>> buscarPorStatus(@PathVariable String status) {
        List<ContaPagarDTO> contas = contaPagarService.buscarPorStatus(status);
        return ResponseEntity.ok(contas);
    }

    @GetMapping("/vencidas")
    public ResponseEntity<List<ContaPagarDTO>> buscarVencidas() {
        List<ContaPagarDTO> contasVencidas = contaPagarService.buscarVencidas();
        return ResponseEntity.ok(contasVencidas);
    }

    // --- INÍCIO DA CORREÇÃO ---
    @GetMapping("/periodo")
    public ResponseEntity<List<ContaPagarDTO>> buscarPorPeriodo(
            // 4. Use @DateTimeFormat aqui também
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        List<ContaPagarDTO> contas = contaPagarService.buscarPorPeriodo(dataInicio, dataFim);
        return ResponseEntity.ok(contas);
    }
    // --- FIM DA CORREÇÃO ---

    @GetMapping("/total-pendente")
    public ResponseEntity<BigDecimal> calcularTotalPendente() {
        BigDecimal total = contaPagarService.calcularTotalPendente();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/total-pago")
    public ResponseEntity<BigDecimal> calcularTotalPago() {
        BigDecimal total = contaPagarService.calcularTotalPago();
        return ResponseEntity.ok(total);
    }
}
