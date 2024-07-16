package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.domain.ItemVenda;
import com.joao.osMarmoraria.dtos.VendaDTO;
import com.joao.osMarmoraria.services.VendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping(value = "/venda")
public class VendaController {

	@Autowired
	private VendaService vendaService;

	@GetMapping(value = "/{id}")
	public ResponseEntity<VendaDTO> findById(@PathVariable Integer id) {
		VendaDTO obj = new VendaDTO(vendaService.findById(id));
		return ResponseEntity.ok().body(obj);
	}

	@GetMapping
	public ResponseEntity<List<VendaDTO>> findAll() {
		List<VendaDTO> list = vendaService.findAll().stream()
				.map(VendaDTO::new)
				.collect(Collectors.toList());
		return ResponseEntity.ok().body(list);
	}

	@PostMapping
	public ResponseEntity<VendaDTO> create(@Valid @RequestBody VendaDTO obj) {
		VendaDTO newObj = new VendaDTO(vendaService.create(obj));
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(newObj.getId()).toUri();
		return ResponseEntity.created(uri).build();
	}

	@PutMapping(value = "/{id}")
	public ResponseEntity<VendaDTO> update(@Valid @RequestBody VendaDTO obj, @PathVariable Integer id) {
		obj.setId(id);
		VendaDTO newObj = new VendaDTO(vendaService.update(obj));
		return ResponseEntity.ok().body(newObj);
	}

	@PutMapping(value = "/efetuar/{id}")
	public ResponseEntity<VendaDTO> efetuarVenda(@PathVariable Integer id) {
		VendaDTO newObj = new VendaDTO(vendaService.efetuarVenda(id));
		return ResponseEntity.ok().body(newObj);
	}

	@PostMapping(value = "/{id}/addItem")
	public ResponseEntity<VendaDTO> addItem(@PathVariable Integer id, @Valid @RequestBody ItemVenda item) {
		VendaDTO newObj = new VendaDTO(vendaService.addItem(id, item));
		return ResponseEntity.ok().body(newObj);
	}

	@DeleteMapping(value = "/{id}/removeItem/{itemId}")
	public ResponseEntity<VendaDTO> removeItem(@PathVariable Integer id, @PathVariable Integer itemId) {
		VendaDTO newObj = new VendaDTO(vendaService.removeItem(id, itemId));
		return ResponseEntity.ok().body(newObj);
	}
}
