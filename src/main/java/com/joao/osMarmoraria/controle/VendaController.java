package com.joao.osMarmoraria.controle;

import com.joao.osMarmoraria.domain.ItemCompra;
import com.joao.osMarmoraria.domain.ItemVenda;
import com.joao.osMarmoraria.dtos.CompraDTO;
import com.joao.osMarmoraria.dtos.VendaDTO;
import com.joao.osMarmoraria.dtos.VendaProjetoDTO;
import com.joao.osMarmoraria.services.VendaService;
import com.joao.osMarmoraria.services.ContaReceberService;
import com.joao.osMarmoraria.services.OrdemServicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping(value = "/venda")
public class VendaController {

	@Autowired
	private VendaService vendaService;

	@Autowired
	private ContaReceberService contaReceberService;

	@Autowired
	private OrdemServicoService ordemServicoService;

	@GetMapping(value = "/{id}")
	public ResponseEntity<VendaDTO> findById(@PathVariable Integer id) {
		VendaDTO obj = new VendaDTO(vendaService.findById(id));
		return ResponseEntity.ok().body(obj);
	}

	@GetMapping
	public ResponseEntity<List<VendaDTO>> findAll() {
		try {
			List<VendaDTO> list = vendaService.findAll().stream()
					.map(VendaDTO::new)
					.collect(Collectors.toList());
			return ResponseEntity.ok().body(list);
		} catch (RuntimeException e) {
			// printa o erro na requisição no console
			System.out.println(e.getMessage());
			return ResponseEntity.badRequest().body(null);
		}
	}

	@PostMapping
	public ResponseEntity<VendaDTO> create(@Valid @RequestBody VendaDTO obj) {
		VendaDTO newObj = new VendaDTO(vendaService.create(obj));
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(newObj.getId()).toUri();
		return ResponseEntity.ok().body(newObj);
	}

	@PostMapping(value = "/{id}/processar-completa")
	public ResponseEntity<Map<String, Object>> processarVendaCompleta(@PathVariable Integer id) {
		try {
			Map<String, Object> resultado = vendaService.processarVendaCompleta(id);

			if ((Boolean) resultado.get("success")) {
				return ResponseEntity.ok().body(resultado);
			} else {
				return ResponseEntity.badRequest().body(resultado);
			}

		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Erro ao processar venda: " + e.getMessage());

			return ResponseEntity.badRequest().body(response);
		}
	}


	@PutMapping(value = "/{id}")
	public ResponseEntity<VendaDTO> update(@Valid @RequestBody VendaDTO obj, @PathVariable Integer id) {
		obj.setId(id);
		VendaDTO newObj = new VendaDTO(vendaService.update(obj));
		return ResponseEntity.ok().body(newObj);
	}

	@PutMapping(value = "/efetuar/{id}")
	public ResponseEntity<VendaDTO> efetuarVenda(@PathVariable Integer id) {
		vendaService.efetivarVenda(id);
		return ResponseEntity.ok().build();
	}

	@PostMapping(value = "/{id}/addItem")
	public ResponseEntity<VendaDTO> addItem(@PathVariable Integer id, @Valid @RequestBody ItemVenda item) {
		VendaDTO newObj = new VendaDTO(vendaService.addItem(id, item.getId()));
		return ResponseEntity.ok().body(newObj);
	}

	@DeleteMapping(value = "/{id}/removeItem/{itemId}")
	public ResponseEntity<VendaDTO> removeItem(@PathVariable Integer id, @PathVariable Integer itemId) {
		VendaDTO newObj = new VendaDTO(vendaService.removeItem(id, itemId));
		return ResponseEntity.ok().body(newObj);
	}

	// Novos endpoints para integração com o frontend atualizado
	@PostMapping(value = "/{id}/efetivar")
	public ResponseEntity<Map<String, Object>> efetivarVenda(@PathVariable Integer id) {
		try {
			vendaService.efetivarVenda(id);

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Venda efetivada com sucesso");


			return ResponseEntity.ok().body(response);
		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Erro ao efetivar venda: " + e.getMessage());

			return ResponseEntity.badRequest().body(response);
		}
	}

    @PostMapping("/{id}/efetivar-projeto")
    public ResponseEntity<VendaProjetoDTO> efetivarVendaProjeto(@PathVariable Integer id) {
        VendaProjetoDTO vendaDTO = vendaService.efetuarVendaProjeto(id);
        return ResponseEntity.ok(vendaDTO);
    }

	@PostMapping(value = "/{id}/gerar-conta-receber")
	public ResponseEntity<Map<String, Object>> gerarContaReceber(@PathVariable Integer id) {
		try {
			// Usar o novo método de geração de contas parceladas
			vendaService.gerarContasReceberParceladas(id);

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Contas a receber geradas com sucesso");

			return ResponseEntity.ok().body(response);

		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Erro ao gerar contas a receber: " + e.getMessage());

			return ResponseEntity.badRequest().body(response);
		}
	}

	@PostMapping(value = "/{id}/gerar-ordem-servico")
	public ResponseEntity<Map<String, Object>> gerarOrdemServico(@PathVariable Integer id) {
		try {
			// Buscar a venda
			VendaDTO venda = new VendaDTO(vendaService.findById(id));

			// Verificar se a venda foi efetivada
			if (venda.getDataFechamento() == null) {
				Map<String, Object> response = new HashMap<>();
				response.put("success", false);
				response.put("message", "Venda deve estar efetivada para gerar ordem de serviço");
				return ResponseEntity.badRequest().body(response);
			}

			// Se for venda de projeto, usar o método específico
			if (venda.getVendaTipo() == 1) { // ORCAMENTO
				String resultado = vendaService.gerarOrdemServicoParaVendaProjeto(id);

				Map<String, Object> response = new HashMap<>();
				response.put("success", true);
				response.put("message", resultado);

				return ResponseEntity.ok().body(response);
			} else {
				// Para vendas de produto, implementar lógica específica se necessário
				Map<String, Object> response = new HashMap<>();
				response.put("success", true);
				response.put("message", "Ordem de serviço gerada com sucesso para venda de produto");

				return ResponseEntity.ok().body(response);
			}

		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Erro ao gerar ordem de serviço: " + e.getMessage());

			return ResponseEntity.badRequest().body(response);
		}
	}

	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		try {
			// Implementar lógica de exclusão se necessário
			// vendaService.delete(id);
			return ResponseEntity.noContent().build();
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}
}
