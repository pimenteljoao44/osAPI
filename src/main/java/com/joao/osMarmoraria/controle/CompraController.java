package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.domain.ItemCompra;
import com.joao.osMarmoraria.dtos.CompraDTO;
import com.joao.osMarmoraria.services.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/compra")
public class CompraController {

	@Autowired
	private CompraService compraService;

	@GetMapping(value = "/{id}")
	public ResponseEntity<CompraDTO> findById(@PathVariable Integer id) {
		CompraDTO obj = new CompraDTO(compraService.findById(id));
		return ResponseEntity.ok().body(obj);
	}

	@GetMapping
	public ResponseEntity<List<CompraDTO>> findAll() {
		List<CompraDTO> list = compraService.findAll().stream()
				.map(CompraDTO::new)
				.collect(Collectors.toList());
		return ResponseEntity.ok().body(list);
	}

	@PostMapping
	public ResponseEntity<CompraDTO> create(@Valid @RequestBody CompraDTO obj) {
		try {
			CompraDTO newObj = new CompraDTO(compraService.create(obj));
			URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
					.buildAndExpand(newObj.getComprId()).toUri();
			return ResponseEntity.created(uri).body(newObj);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(null);
		}
	}

	@PutMapping(value = "/{id}")
	public ResponseEntity<CompraDTO> update(@Valid @RequestBody CompraDTO obj, @PathVariable Integer id) {
		obj.setComprId(id);
		CompraDTO newObj = new CompraDTO(compraService.update(obj));
		return ResponseEntity.ok().body(newObj);
	}

	@PostMapping(value = "/{id}/addItem")
	public ResponseEntity<CompraDTO> addItem(@PathVariable Integer id, @Valid @RequestBody ItemCompra item) {
		CompraDTO newObj = new CompraDTO(compraService.addItem(id, item.getId()));
		return ResponseEntity.ok().body(newObj);
	}

	@DeleteMapping(value = "/{id}/removeItem/{itemId}")
	public ResponseEntity<CompraDTO> removeItem(@PathVariable Integer id, @PathVariable Integer itemId) {
		CompraDTO newObj = new CompraDTO(compraService.removeItem(id, itemId));
		return ResponseEntity.ok().body(newObj);
	}
}

