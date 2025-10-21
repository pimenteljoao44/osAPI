package com.joao.osMarmoraria.controle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.joao.osMarmoraria.domain.enums.StatusProjeto;
import com.joao.osMarmoraria.domain.enums.TipoProjeto;
import com.joao.osMarmoraria.dtos.*;
import com.joao.osMarmoraria.services.ProjetoService;
import com.joao.osMarmoraria.services.RelatorioService;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource; // IMPORT NECESSÁRIO
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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projetos-personalizados")
@CrossOrigin(origins = "*")
@Validated
public class ProjetoController {

    @Autowired
    private ProjetoService projetoService;

    @Autowired
    private RelatorioService relatorioService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());


    @PostMapping("/orcamento-pdf")
    public ResponseEntity<byte[]> gerarOrcamentoPDF(@Valid @RequestBody OrcamentoPDFDTO orcamento) {
        try {
            // --- LOG DE DEPURAÇÃO ADICIONADO ---
            // Este bloco irá imprimir na consola os dados exatos recebidos do front-end.
            System.out.println("==========================================================");
            System.out.println("=== DADOS RECEBIDOS PARA GERAR PDF DO ORÇAMENTO ===");
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(orcamento));

            if (orcamento.getItens() == null || orcamento.getItens().isEmpty()) {
                System.err.println("AVISO: A lista de itens do orçamento está vazia ou nula. O PDF será gerado em branco.");
            }
            System.out.println("==========================================================");
            // --- FIM DO LOG DE DEPURAÇÃO ---

            Map<String, Object> parametros = new HashMap<>();
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
            parametros.put("valorTotal", orcamento.getValorTotal());
            parametros.put("observacoes", orcamento.getObservacoes());

            JRBeanCollectionDataSource itensDataSource = new JRBeanCollectionDataSource(orcamento.getItens());
            parametros.put("ITENS_DATA_SOURCE", itensDataSource);

            byte[] pdfRelatorio = relatorioService.gerarOrcamentoPDF(parametros);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "Orcamento.pdf");

            return new ResponseEntity<>(pdfRelatorio, headers, HttpStatus.OK);
        } catch (Throwable t) {
            System.err.println("ERRO GRAVE AO GERAR ORÇAMENTO PDF: " + t.getMessage());
            t.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


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

    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<ProjetoDTO> aprovarProjeto(
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, String> request) {

        String observacoes = request != null ? request.get("observacoes") : null;
        ProjetoDTO projeto = projetoService.aprovarProjeto(id, observacoes);
        return ResponseEntity.ok(projeto);
    }

    @PostMapping("/calcular-orcamento")
    public ResponseEntity<CalculoOrcamentoDTO> calcularOrcamento(@Valid @RequestBody ProjetoDTO projetoDTO) {
        CalculoOrcamentoDTO calculo = projetoService.calcularOrcamento(projetoDTO);
        return ResponseEntity.ok(calculo);
    }

    @PostMapping("/materiais-sugeridos")
    public ResponseEntity<List<MaterialSugeridoDTO>> obterMateriaisSugeridos(@RequestBody ProjetoDTO projetoDTO) { // Alterado para receber ProjetoDTO
        List<MaterialSugeridoDTO> materiais = projetoService.obterMateriaisSugeridos(projetoDTO); // Passa o ProjetoDTO completo
        return ResponseEntity.ok(materiais);
    }

    @GetMapping("/relatorio/periodo")
    public ResponseEntity<List<ProjetoDTO>> gerarRelatorioProjetosPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        List<ProjetoDTO> projetos = projetoService.obterProjetosPorPeriodo(dataInicio, dataFim);
        return ResponseEntity.ok(projetos);
    }
    public static class StatusUpdateRequest {
        private StatusProjeto status;
        public StatusProjeto getStatus() { return status; }
        public void setStatus(StatusProjeto status) { this.status = status; }
    }

    // Removida a classe MaterialSugeridoRequest, pois o endpoint agora recebe ProjetoDTO diretamente
}
