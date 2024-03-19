package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.domain.Cidade;
import com.joao.osMarmoraria.dtos.CidadeDTO;
import com.joao.osMarmoraria.services.CidadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/cidade")
public class CidadeController {
    @Autowired
    private CidadeService cidadeService;

    @GetMapping("{id}")
    public ResponseEntity<CidadeDTO> findById(@PathVariable Integer id) {
        CidadeDTO cidadeDTO = new CidadeDTO(cidadeService.findById(id));
        return ResponseEntity.ok().body(cidadeDTO);
    }

    @GetMapping
    public ResponseEntity<List<CidadeDTO>> findAll() {
        List<Cidade> list = cidadeService.findAll();
        List<CidadeDTO> listDTO = new ArrayList<>();
        for (Cidade c : list) {
            listDTO.add(new CidadeDTO(c));
        }
        return ResponseEntity.ok().body(listDTO);
    }

    @PostMapping
    public ResponseEntity<CidadeDTO> create(@Valid @RequestBody CidadeDTO cidadeDTO) {
        Cidade newObj = cidadeService.create(cidadeDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(newObj.getCidId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<CidadeDTO> update(@PathVariable Integer id, @Valid @RequestBody CidadeDTO cidadeDTO) {
        CidadeDTO newObj = new CidadeDTO(cidadeService.update(id, cidadeDTO));
        return ResponseEntity.ok().body(newObj);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        cidadeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
