package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.domain.Grupo;
import com.joao.osMarmoraria.dtos.GrupoDTO;
import com.joao.osMarmoraria.services.GrupoService;
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
@RequestMapping("/grupo")
public class GrupoController {

    @Autowired
    private GrupoService grupoService;

    @GetMapping("{id}")
    public ResponseEntity<GrupoDTO> findById(@PathVariable Integer id) {
        GrupoDTO GrupoDTO = new GrupoDTO(grupoService.findById(id));
        return ResponseEntity.ok().body(GrupoDTO);
    }

    @GetMapping
    public ResponseEntity<List<GrupoDTO>> findAll() {
        List<Grupo> list = grupoService.findAll();
        List<GrupoDTO> listDTO = new ArrayList<>();
        for (Grupo g : list) {
            listDTO.add(new GrupoDTO(g));
        }
        return ResponseEntity.ok().body(listDTO);
    }

    @PostMapping
    public ResponseEntity<GrupoDTO> create(@Valid @RequestBody GrupoDTO GrupoDTO) {
        Grupo newObj = grupoService.create(GrupoDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(newObj.getId()).toUri();
        return ResponseEntity.created(uri).body(new GrupoDTO(newObj));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<GrupoDTO> update(@PathVariable Integer id, @Valid @RequestBody GrupoDTO GrupoDTO) {
        GrupoDTO newObj = new GrupoDTO(grupoService.update(id, GrupoDTO));
        return ResponseEntity.ok().body(newObj);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        grupoService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
