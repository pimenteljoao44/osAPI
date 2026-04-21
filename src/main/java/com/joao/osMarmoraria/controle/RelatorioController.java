package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.services.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    @PostMapping("/gerar/relatorioDeVendasResumido")
    public ResponseEntity<byte[]> gerarRelatorioDeVendasResumido( @RequestBody Map<String, Object> parametros) {
        try {
            byte[] pdfRelatorio = relatorioService.gerarRelatorioDeVendasResumido(parametros);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "Relatorio" + ".pdf");

            return new ResponseEntity<>(pdfRelatorio, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Erro ao gerar o relatório: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("Erro ao gerar o relatório: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
