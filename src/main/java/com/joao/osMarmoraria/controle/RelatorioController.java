package com.joao.osMarmoraria.controle;

import lombok.RequiredArgsConstructor;

import com.joao.osMarmoraria.services.RelatorioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @PostMapping("/gerar/relatorioDeVendasResumido")
    public ResponseEntity<byte[]> gerarRelatorioDeVendasResumido( @RequestBody Map<String, Object> parametros) {
        try {
            byte[] pdfRelatorio = relatorioService.gerarRelatorioDeVendasResumido(parametros);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "Relatorio" + ".pdf");

            return new ResponseEntity<>(pdfRelatorio, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erro ao gerar o relatório de vendas resumido", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/gerar/relatorioDeComprasResumido")
    public ResponseEntity<byte[]> gerarRelatorioDeComprasResumido( @RequestBody Map<String, Object> parametros) {
        try {
            byte [] pdfRelatorio = relatorioService.gerarRelatorioDeComprasResumido(parametros);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "Relatorio" + ".pdf");

            return new ResponseEntity<>(pdfRelatorio, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Erro ao gerar o relatório de compras resumido", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
