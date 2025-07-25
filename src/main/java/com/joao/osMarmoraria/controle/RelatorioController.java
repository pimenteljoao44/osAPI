package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.dtos.*;
import com.joao.osMarmoraria.services.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    @PostMapping("/gerar/relatorioDeVendasResumido")
    public ResponseEntity<byte[]> gerarRelatorioDeVendasResumido(@RequestBody Map<String, Object> parametros) {
        try {
            byte[] pdfRelatorio = relatorioService.gerarRelatorioDeVendasResumido(parametros);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "RelatorioVendasResumido.pdf");

            return new ResponseEntity<>(pdfRelatorio, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Erro ao gerar o relatório: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/gerar/relatorioDeComprasResumido")
    public ResponseEntity<byte[]> gerarRelatorioDeComprasResumido(@RequestBody Map<String, Object> parametros) {
        try {
            byte[] pdfRelatorio = relatorioService.gerarRelatorioDeComprasResumido(parametros);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "RelatorioComprasResumido.pdf");

            return new ResponseEntity<>(pdfRelatorio, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Erro ao gerar o relatório: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/vendas-por-cliente-periodo")
    public ResponseEntity<byte[]> gerarRelatorioVendasPorClientePeriodo(@Valid @RequestBody RelatorioVendasClientePeriodoDTO filtros) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("clienteId", filtros.getClienteId());
            parametros.put("dataInicio", filtros.getDataInicio());
            parametros.put("dataFim", filtros.getDataFim());

            byte[] pdfRelatorio = relatorioService.gerarRelatorioVendasPorClientePeriodo(parametros);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "RelatorioVendasPorClientePeriodo.pdf");

            return new ResponseEntity<>(pdfRelatorio, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Erro ao gerar o relatório: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/contas-pagar")
    public ResponseEntity<byte[]> gerarRelatorioContasPagar(@Valid @RequestBody RelatorioContasPagarDTO filtros) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("fornecedorId", filtros.getFornecedorId());
            parametros.put("dataVencimentoInicio", filtros.getDataVencimentoInicio());
            parametros.put("dataVencimentoFim", filtros.getDataVencimentoFim());
            parametros.put("apenasVencidas", filtros.getApenasVencidas());
            parametros.put("apenasQuitadas", filtros.getApenasQuitadas());

            byte[] pdfRelatorio = relatorioService.gerarRelatorioContasPagar(parametros);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "RelatorioContasPagar.pdf");

            return new ResponseEntity<>(pdfRelatorio, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Erro ao gerar o relatório: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/contas-receber")
    public ResponseEntity<byte[]> gerarRelatorioContasReceber(@Valid @RequestBody RelatorioContasReceberDTO filtros) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("clienteId", filtros.getClienteId());
            parametros.put("dataVencimentoInicio", filtros.getDataVencimentoInicio());
            parametros.put("dataVencimentoFim", filtros.getDataVencimentoFim());
            parametros.put("apenasVencidas", filtros.getApenasVencidas());
            parametros.put("apenasRecebidas", filtros.getApenasRecebidas());

            byte[] pdfRelatorio = relatorioService.gerarRelatorioContasReceber(parametros);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "RelatorioContasReceber.pdf");

            return new ResponseEntity<>(pdfRelatorio, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Erro ao gerar o relatório: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/compras-por-fornecedor-periodo")
    public ResponseEntity<byte[]> gerarRelatorioComprasPorFornecedorPeriodo(@Valid @RequestBody RelatorioComprasFornecedorPeriodoDTO filtros) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("fornecedorId", filtros.getFornecedorId());
            parametros.put("dataInicio", filtros.getDataInicio());
            parametros.put("dataFim", filtros.getDataFim());

            byte[] pdfRelatorio = relatorioService.gerarRelatorioComprasPorFornecedorPeriodo(parametros);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "RelatorioComprasPorFornecedorPeriodo.pdf");

            return new ResponseEntity<>(pdfRelatorio, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Erro ao gerar o relatório: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}