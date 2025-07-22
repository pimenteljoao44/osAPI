package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.dtos.ParcelaDTO;
import com.joao.osMarmoraria.services.ParcelaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/parcelas")
@CrossOrigin(origins = "*")
public class ParcelaController {
    
    @Autowired
    private ParcelaService parcelaService;
    
    @GetMapping
    public ResponseEntity<List<ParcelaDTO>> listarTodas() {
        List<ParcelaDTO> parcelas = parcelaService.listarTodas();
        return ResponseEntity.ok(parcelas);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ParcelaDTO> buscarPorId(@PathVariable Integer id) {
        ParcelaDTO parcela = parcelaService.buscarPorId(id);
        return ResponseEntity.ok(parcela);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ParcelaDTO> atualizar(@PathVariable Integer id, @Valid @RequestBody ParcelaDTO parcelaDTO) {
        ParcelaDTO parcelaAtualizada = parcelaService.atualizar(id, parcelaDTO);
        return ResponseEntity.ok(parcelaAtualizada);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        parcelaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/conta-pagar/{contaId}")
    public ResponseEntity<List<ParcelaDTO>> listarPorContaPagar(@PathVariable Integer contaId) {
        List<ParcelaDTO> parcelas = parcelaService.listarParcelasPorContaPagar(contaId);
        return ResponseEntity.ok(parcelas);
    }
    
    @GetMapping("/conta-receber/{contaId}")
    public ResponseEntity<List<ParcelaDTO>> listarPorContaReceber(@PathVariable Integer contaId) {
        List<ParcelaDTO> parcelas = parcelaService.listarParcelasPorContaReceber(contaId);
        return ResponseEntity.ok(parcelas);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ParcelaDTO>> listarPorStatus(@PathVariable String status) {
        List<ParcelaDTO> parcelas = parcelaService.listarParcelasPorStatus(status);
        return ResponseEntity.ok(parcelas);
    }
    
    @GetMapping("/vencidas")
    public ResponseEntity<List<ParcelaDTO>> listarVencidas() {
        List<ParcelaDTO> parcelas = parcelaService.listarParcelasVencidas();
        return ResponseEntity.ok(parcelas);
    }
    
    @GetMapping("/periodo")
    public ResponseEntity<List<ParcelaDTO>> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        List<ParcelaDTO> parcelas = parcelaService.listarParcelasPorPeriodo(dataInicio, dataFim);
        return ResponseEntity.ok(parcelas);
    }
    
    @GetMapping("/proximas-vencer")
    public ResponseEntity<List<ParcelaDTO>> listarProximasAVencer(
            @RequestParam(defaultValue = "30") Integer dias) {
        List<ParcelaDTO> parcelas = parcelaService.listarProximasParcelasAVencer(dias);
        return ResponseEntity.ok(parcelas);
    }
    
    @PutMapping("/{id}/pagar")
    public ResponseEntity<ParcelaDTO> marcarComoPaga(
            @PathVariable Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataPagamento) {
        ParcelaDTO parcela = parcelaService.marcarParcelaComoPaga(id, dataPagamento);
        return ResponseEntity.ok(parcela);
    }
    
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ParcelaDTO> cancelar(@PathVariable Integer id) {
        ParcelaDTO parcela = parcelaService.cancelarParcela(id);
        return ResponseEntity.ok(parcela);
    }
    
    // Endpoints para relatórios e estatísticas
    @GetMapping("/dashboard/resumo")
    public ResponseEntity<ResumoParcelasDTO> obterResumo() {
        List<ParcelaDTO> todasParcelas = parcelaService.listarTodas();
        List<ParcelaDTO> vencidas = parcelaService.listarParcelasVencidas();
        List<ParcelaDTO> proximasVencer = parcelaService.listarProximasParcelasAVencer(7);
        
        ResumoParcelasDTO resumo = new ResumoParcelasDTO();
        resumo.setTotalParcelas(todasParcelas.size());
        resumo.setParcelasVencidas(vencidas.size());
        resumo.setProximasVencer(proximasVencer.size());
        resumo.setParcelasPagas((int) todasParcelas.stream().filter(ParcelaDTO::isPaga).count());
        resumo.setParcelasPendentes((int) todasParcelas.stream().filter(ParcelaDTO::isPendente).count());
        
        return ResponseEntity.ok(resumo);
    }
    
    // DTO interno para resumo
    public static class ResumoParcelasDTO {
        private Integer totalParcelas;
        private Integer parcelasVencidas;
        private Integer proximasVencer;
        private Integer parcelasPagas;
        private Integer parcelasPendentes;
        
        // Getters and setters
        public Integer getTotalParcelas() { return totalParcelas; }
        public void setTotalParcelas(Integer totalParcelas) { this.totalParcelas = totalParcelas; }
        
        public Integer getParcelasVencidas() { return parcelasVencidas; }
        public void setParcelasVencidas(Integer parcelasVencidas) { this.parcelasVencidas = parcelasVencidas; }
        
        public Integer getProximasVencer() { return proximasVencer; }
        public void setProximasVencer(Integer proximasVencer) { this.proximasVencer = proximasVencer; }
        
        public Integer getParcelasPagas() { return parcelasPagas; }
        public void setParcelasPagas(Integer parcelasPagas) { this.parcelasPagas = parcelasPagas; }
        
        public Integer getParcelasPendentes() { return parcelasPendentes; }
        public void setParcelasPendentes(Integer parcelasPendentes) { this.parcelasPendentes = parcelasPendentes; }
    }
}

