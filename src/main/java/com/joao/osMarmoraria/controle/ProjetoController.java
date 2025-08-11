package com.joao.osMarmoraria.controle;


import com.joao.osMarmoraria.domain.enums.StatusProjeto;
import com.joao.osMarmoraria.domain.enums.TipoProjeto;
import com.joao.osMarmoraria.dtos.*;


import com.joao.osMarmoraria.services.ProjetoService;
import com.joao.osMarmoraria.services.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/projetos-personalizados")
@CrossOrigin(origins = "*")
@Validated
public class ProjetoController {

    @Autowired
    private ProjetoService projetoService;

    @Autowired
    private RelatorioService relatorioService;

    @GetMapping
    public ResponseEntity<Page<ProjetoDTO>> listarProjetos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dataCriacao") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) StatusProjeto status,
            @RequestParam(required = false) TipoProjeto tipoProjeto,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) String clienteNome) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProjetoDTO> projetos = projetoService.listarComFiltros(nome, status, tipoProjeto, clienteId, clienteNome, pageable);
        return ResponseEntity.ok(projetos);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ProjetoDTO>> listarProjetosAprovadosPorCliente(@PathVariable Integer clienteId) {
        List<ProjetoDTO> projetos = projetoService.listarProjetosAprovadosPorCliente(clienteId);
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


    @PostMapping("/orcamento-pdf")
    public ResponseEntity<byte[]> gerarOrcamentoPDF(@Valid @RequestBody OrcamentoPDFDTO orcamento) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("projetoId", orcamento.getProjetoId());
            parametros.put("clienteNome", orcamento.getClienteNome());
            parametros.put("clienteEmail", orcamento.getClienteEmail());
            parametros.put("clienteTelefone", orcamento.getClienteTelefone());
            parametros.put("clienteEndereco", orcamento.getClienteEndereco());
            parametros.put("projetoNome", orcamento.getProjetoNome());
            parametros.put("projetoDescricao", orcamento.getProjetoDescricao());
            parametros.put("dataOrcamento", orcamento.getDataOrcamento());
            parametros.put("dataValidade", orcamento.getDataValidade());
            parametros.put("largura", orcamento.getLargura());
            parametros.put("comprimento", orcamento.getComprimento());
            parametros.put("area", orcamento.getArea());
            parametros.put("espessura", orcamento.getEspessura());
            parametros.put("valorMateriais", orcamento.getValorMateriais());
            parametros.put("valorMaoObra", orcamento.getValorMaoObra());
            parametros.put("margemLucro", orcamento.getMargemLucro());
            parametros.put("valorTotal", orcamento.getValorTotal());
            parametros.put("observacoes", orcamento.getObservacoes());

            byte[] pdfRelatorio = relatorioService.gerarOrcamentoPDF(parametros);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "Orcamento.pdf");

            return new ResponseEntity<>(pdfRelatorio, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Erro ao gerar o orçamento: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
