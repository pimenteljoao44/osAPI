package com.joao.osMarmoraria.controle;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.joao.osMarmoraria.domain.Funcionario;
import com.joao.osMarmoraria.dtos.FuncionarioDTO;
import com.joao.osMarmoraria.services.FuncionarioService;

import javax.validation.Valid;

@CrossOrigin("*")
@RestController
@RequestMapping(value = "/funcionarios")
public class FuncionarioController {
	
	@Autowired
	private FuncionarioService service;

	@GetMapping(value="{id}")
	public ResponseEntity<FuncionarioDTO> findById(@PathVariable Integer id){
		
		FuncionarioDTO objDTO = new FuncionarioDTO(service.findById(id));
		return ResponseEntity.ok().body(objDTO);
	}
	
	@GetMapping
	public ResponseEntity<List<FuncionarioDTO>> findAll(){
		List<Funcionario> list = service.findAll();
		List<FuncionarioDTO> listDTO = new ArrayList<>();
		
		for(Funcionario f : list) {
			listDTO.add(new FuncionarioDTO(f));
		}
		return ResponseEntity.ok().body(listDTO);
	}
	
	@PostMapping
	public ResponseEntity<FuncionarioDTO>  creaate(@Valid @RequestBody FuncionarioDTO objDTO){
		Funcionario newObj = service.create(objDTO);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(newObj.getId()).toUri();
		
		return ResponseEntity.created(uri).body(new FuncionarioDTO(newObj));
	}
	
	@PutMapping(value ="/{id}")
	public ResponseEntity<FuncionarioDTO> update(@PathVariable Integer id,@Valid @RequestBody FuncionarioDTO objDTO){
		FuncionarioDTO newObj = new FuncionarioDTO(service.update(id,objDTO));
		return ResponseEntity.ok().body(newObj);
	}
	
	// delete funcionario
	
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id){
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}
