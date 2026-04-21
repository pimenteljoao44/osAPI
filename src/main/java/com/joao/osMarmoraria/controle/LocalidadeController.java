package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.services.LocalidadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("localidades")
public class LocalidadeController {
    private final LocalidadeService localidadeService;

    public LocalidadeController(LocalidadeService localidadeService) {
        this.localidadeService = localidadeService;
    }

    @GetMapping("/estados")
    public ResponseEntity<List<Map<String, String>>> listarEstados() {
        return ResponseEntity.ok(localidadeService.listarEstados());
    }

    @GetMapping("/cidades/{uf}")
    public ResponseEntity<List<Map<String, String>>> listarCidades(
            @PathVariable String uf) {
        return  ResponseEntity.ok(localidadeService.listarCidades(uf));
    }

}
