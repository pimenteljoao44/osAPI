package com.joao.osMarmoraria.controle;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


import com.joao.osMarmoraria.domain.OrdemServico;
import com.joao.osMarmoraria.domain.enums.StatusOrdemServico;
import com.joao.osMarmoraria.dtos.OrdemServicoDTO;

import com.joao.osMarmoraria.services.OrdemServicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/os")
@CrossOrigin(origins = "*")
@Validated
public class OrdemServicoController {

    @Autowired
    private OrdemServicoService ordemServicoService;

    @GetMapping
    public ResponseEntity<List<OrdemServicoDTO>> listarTodas() {
        List<OrdemServicoDTO> ordensServico = ordemServicoService.listarTodas();
        return ResponseEntity.ok(ordensServico);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdemServicoDTO> buscarPorId(@PathVariable Integer id) {
        OrdemServicoDTO ordemServico = ordemServicoService.buscarPorId(id);
        return ResponseEntity.ok(ordemServico);
    }

    @GetMapping("/numero/{numero}")
    public ResponseEntity<OrdemServicoDTO> buscarPorNumero(@PathVariable String numero) {
        OrdemServicoDTO ordemServico = ordemServicoService.buscarPorNumero(numero);
        return ResponseEntity.ok(ordemServico);
    }

    @PostMapping("/gerar-por-projeto/{projetoId}")
    public ResponseEntity<OrdemServicoDTO> gerarPorProjeto(@PathVariable Integer projetoId) {
        OrdemServicoDTO ordemServico = ordemServicoService.gerarPorProjeto(projetoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ordemServico);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdemServicoDTO> atualizarOrdemServico(@PathVariable Integer id, @Valid @RequestBody OrdemServicoDTO ordemServicoDTO) {
        OrdemServicoDTO ordemAtualizada = ordemServicoService.atualizarOrdemServico(id, ordemServicoDTO);
        return ResponseEntity.ok(ordemAtualizada);
    }

    @PatchMapping("/{id}/iniciar-os")
    public ResponseEntity<OrdemServicoDTO> iniciarExecucao(@PathVariable Integer id) {
        OrdemServicoDTO ordemServico = ordemServicoService.iniciarExecucao(id);
        return ResponseEntity.ok(ordemServico);
    }

    @PatchMapping("/{id}/pausar")
    public ResponseEntity<OrdemServicoDTO> pausarExecucao(@PathVariable Integer id) {
        OrdemServicoDTO ordemServico = ordemServicoService.pausarExecucao(id);
        return ResponseEntity.ok(ordemServico);
    }

    @PatchMapping("/{id}/retornar")
    public ResponseEntity<OrdemServicoDTO> retornarExecucao(@PathVariable Integer id) {
        OrdemServicoDTO ordemServico = ordemServicoService.retornarExecucao(id);
        return ResponseEntity.ok(ordemServico);
    }

    @PatchMapping("/{id}/concluir-os")
    public ResponseEntity<OrdemServicoDTO> concluirExecucao(@PathVariable Integer id) {
        OrdemServicoDTO ordemServico = ordemServicoService.concluirExecucao(id);
        return ResponseEntity.ok(ordemServico);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<OrdemServicoDTO> cancelarExecucao(@PathVariable Integer id) {
        OrdemServicoDTO ordemServico = ordemServicoService.cancelarExecucao(id);
        return ResponseEntity.ok(ordemServico);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrdemServicoDTO>> buscarPorStatus(@PathVariable StatusOrdemServico status) {
        List<OrdemServicoDTO> ordensServico = ordemServicoService.buscarPorStatus(status);
        return ResponseEntity.ok(ordensServico);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<OrdemServicoDTO>> buscarPorCliente(@PathVariable Integer clienteId) {
        List<OrdemServicoDTO> ordensServico = ordemServicoService.buscarPorCliente(clienteId);
        return ResponseEntity.ok(ordensServico);
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<OrdemServicoDTO>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        List<OrdemServicoDTO> ordensServico = ordemServicoService.buscarPorPeriodo(dataInicio, dataFim);
        return ResponseEntity.ok(ordensServico);
    }
}