package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.domain.Fornecedor;
import com.joao.osMarmoraria.dtos.FornecedorDTO;
import com.joao.osMarmoraria.services.FornecedorService;
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
@RequestMapping(value = "/fornecedores")
public class FornecedorController {

    @Autowired
    private FornecedorService service;

    @GetMapping(value="{id}")
    public ResponseEntity<FornecedorDTO> findById(@PathVariable Integer id){
        FornecedorDTO objDTO = new FornecedorDTO(service.findById(id));
        return ResponseEntity.ok().body(objDTO);
    }

    @GetMapping
    public ResponseEntity<List<FornecedorDTO>> findAll(){
        List<Fornecedor> list = service.findAll();
        List<FornecedorDTO> listDTO = new ArrayList<>();

        for(Fornecedor f : list) {
            FornecedorDTO dto = new FornecedorDTO(f);
            listDTO.add(dto);
        }
        return ResponseEntity.ok().body(listDTO);
    }

    @PostMapping
    public ResponseEntity<FornecedorDTO> create(@Valid @RequestBody FornecedorDTO objDTO){
        Fornecedor newObj = service.create(objDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(newObj.getId()).toUri();

        return ResponseEntity.created(uri).body(new FornecedorDTO(newObj));
    }
    @PutMapping(value ="/{id}")
    public ResponseEntity<FornecedorDTO> update(@PathVariable Integer id, @Valid @RequestBody FornecedorDTO objDTO){
        FornecedorDTO newObj = new FornecedorDTO(service.update(id, objDTO));
        return ResponseEntity.ok().body(newObj);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}