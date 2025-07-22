package com.joao.osMarmoraria.controle;


import com.joao.osMarmoraria.domain.enums.StatusProjeto;
import com.joao.osMarmoraria.domain.enums.TipoProjeto;
import com.joao.osMarmoraria.dtos.*;


import com.joao.osMarmoraria.services.ProjetoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/projetos-personalizados")
@CrossOrigin(origins = "*")
@Validated
public class ProjetoController {

    @Autowired
    private ProjetoService projetoService;

    @GetMapping
    public ResponseEntity<Page<ProjetoDTO>> listarProjetos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dataCriacao") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) StatusProjeto status,
            @RequestParam(required = false) TipoProjeto tipoProjeto,
            @RequestParam(required = false) Integer clienteId) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProjetoDTO> projetos = projetoService.listarComFiltros(nome, status, tipoProjeto, clienteId, pageable);
        return ResponseEntity.ok(projetos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjetoDTO> buscarPorId(@PathVariable Integer id) {
        ProjetoDTO projeto = projetoService.buscarPorId(id);
        return ResponseEntity.ok(projeto);
    }

    @PostMapping
    public ResponseEntity<ProjetoDTO> criarProjeto(@Valid @RequestBody ProjetoDTO projetoDTO) {
        ProjetoDTO novoProjeto = projetoService.criarProjeto(projetoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoProjeto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjetoDTO> atualizarProjeto(@PathVariable Integer id, @Valid @RequestBody ProjetoDTO projetoDTO) {
        ProjetoDTO projetoAtualizado = projetoService.atualizarProjeto(id, projetoDTO);
        return ResponseEntity.ok(projetoAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirProjeto(@PathVariable Integer id) {
        projetoService.excluirProjeto(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProjetoDTO> atualizarStatus(@PathVariable Integer id, @RequestBody StatusUpdateRequest request) {
        ProjetoDTO projeto = projetoService.atualizarStatus(id, request.getStatus());
        return ResponseEntity.ok(projeto);
    }

    @PostMapping("/calcular-orcamento")
    public ResponseEntity<CalculoOrcamentoDTO> calcularOrcamento(@Valid @RequestBody ProjetoDTO projetoDTO) {
        CalculoOrcamentoDTO calculo = projetoService.calcularOrcamento(projetoDTO);
        return ResponseEntity.ok(calculo);
    }

    @PostMapping("/materiais-sugeridos")
    public ResponseEntity<List<MaterialSugeridoDTO>> obterMateriaisSugeridos(@RequestBody MaterialSugeridoRequest request) {
        List<MaterialSugeridoDTO> materiais = projetoService.obterMateriaisSugeridos(request.getTipoProjeto(), request.getMedidas());
        return ResponseEntity.ok(materiais);
    }

    @GetMapping("/relatorio/periodo")
    public ResponseEntity<List<ProjetoDTO>> gerarRelatorioProjetosPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        List<ProjetoDTO> projetos = projetoService.obterProjetosPorPeriodo(dataInicio, dataFim);
        return ResponseEntity.ok(projetos);
    }

    // Classes internas para requests
    public static class StatusUpdateRequest {
        private StatusProjeto status;

        public StatusProjeto getStatus() {
            return status;
        }

        public void setStatus(StatusProjeto status) {
            this.status = status;
        }
    }

    public static class MaterialSugeridoRequest {
        private TipoProjeto tipoProjeto;
        private MedidasProjetoDTO medidas;

        public TipoProjeto getTipoProjeto() {
            return tipoProjeto;
        }

        public void setTipoProjeto(TipoProjeto tipoProjeto) {
            this.tipoProjeto = tipoProjeto;
        }

        public MedidasProjetoDTO getMedidas() {
            return medidas;
        }

        public void setMedidas(MedidasProjetoDTO medidas) {
            this.medidas = medidas;
        }
    }
}
