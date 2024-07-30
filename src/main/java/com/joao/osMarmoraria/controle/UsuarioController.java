package com.joao.osMarmoraria.controle;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import com.joao.osMarmoraria.domain.Usuario;
import com.joao.osMarmoraria.dtos.UsuarioDTO;
import com.joao.osMarmoraria.services.UsuarioService;

import javax.validation.Valid;

@CrossOrigin("*")
@RestController
@RequestMapping(value = "/usuarios")
public class UsuarioController {

	@Autowired
	private UsuarioService service;
	
	@GetMapping(value = "/{id}")
	public ResponseEntity<UsuarioDTO> findById(@PathVariable Integer id){
		UsuarioDTO objDTO = new UsuarioDTO(service.findById(id));
		return ResponseEntity.ok().body(objDTO);
	}
	
	@GetMapping
	public ResponseEntity<List<UsuarioDTO>> findAll(){
		List<Usuario> list = service.findAll();
		List<UsuarioDTO> listDTO = new ArrayList<>();
		
		for (Usuario u : list) {
			listDTO.add(new UsuarioDTO(u));
		}
		return ResponseEntity.ok().body(listDTO);
	}
	
	@PostMapping
	public ResponseEntity<UsuarioDTO> create(@Valid @RequestBody UsuarioDTO objDTO) {
	    Usuario newObj = service.create(objDTO);
	    URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(newObj.getId()).toUri();

	    return ResponseEntity.created(uri).body(new UsuarioDTO(newObj));
	}
	
	@PutMapping(value ="/{id}")
	public ResponseEntity<UsuarioDTO> update(@PathVariable Integer id,@Valid @RequestBody UsuarioDTO obj){
		obj = new UsuarioDTO(service.update(obj));
		return ResponseEntity.ok().body(obj);
	}

	@PutMapping(value = "/{id}/update-password")
	public ResponseEntity<Void> updatePassword(@PathVariable Integer id, @RequestBody String newPassword) {
		System.out.println("Passou aqui");
		service.updatePassword(id, newPassword);
		return ResponseEntity.noContent().build();
	}


	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id){
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
	
}