package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.Cliente;
import com.joao.osMarmoraria.domain.ItemVenda;
import com.joao.osMarmoraria.domain.Venda;
import com.joao.osMarmoraria.domain.enums.FormaPagamento;
import com.joao.osMarmoraria.domain.enums.VendaTipo;
import com.joao.osMarmoraria.dtos.VendaDTO;
import com.joao.osMarmoraria.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    public Venda findById(Integer id) {
        Optional<Venda> obj = vendaRepository.findById(id);
        return obj.orElseThrow(() -> new RuntimeException("Venda não encontrada! ID: " + id));
    }

    public List<Venda> findAll() {
        return vendaRepository.findAll();
    }

    @Transactional
    public Venda create(VendaDTO objDto) {
        Venda venda = fromDTO(objDto);
        venda.setVenId(null);  // Para garantir que será uma nova venda
        return vendaRepository.save(venda);
    }

    @Transactional
    public Venda update(VendaDTO objDto) {
        Venda venda = fromDTO(objDto);
        Venda newObj = findById(venda.getVenId());
        updateData(newObj, venda);
        return vendaRepository.save(newObj);
    }

    @Transactional
    public Venda addItem(Integer vendaId, ItemVenda item) {
        Venda venda = findById(vendaId);
        venda.addItem(item);
        return vendaRepository.save(venda);
    }

    @Transactional
    public Venda removeItem(Integer vendaId, Integer itemId) {
        Venda venda = findById(vendaId);
        ItemVenda item = venda.getItensVenda().stream()
                .filter(iv -> iv.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item não encontrado! ID: " + itemId));
        venda.removeItem(item);
        return vendaRepository.save(venda);
    }

    @Transactional
    public Venda efetuarVenda(Integer id) {
        Venda venda = findById(id);
        venda.efetuarVenda();
        return vendaRepository.save(venda);
    }

    private void updateData(Venda newObj, Venda venda) {
        newObj.setDataAbertura(venda.getDataAbertura());
        newObj.setDataFechamento(venda.getDataFechamento());
        newObj.setTotal(venda.getTotal());
        newObj.setDesconto(venda.getDesconto());
        newObj.setVendaTipo(venda.getVendaTipo());
        newObj.setFormaPagamento(venda.getFormaPagamento());
        newObj.setItensVenda(venda.getItensVenda());
        newObj.setCliente(venda.getCliente());
    }

    public Venda fromDTO(VendaDTO objDto) {
        return new Venda(
                objDto.getId(),
                objDto.getDataAbertura(),
                objDto.getDataFechamento(),
                objDto.getTotal(),
                objDto.getDesconto(),
                VendaTipo.toEnum(objDto.getVendaTipo()),
                FormaPagamento.toEnum(objDto.getFormaPagamento()),
                objDto.getItensVenda().stream()
                        .map(itemId -> new ItemVenda(itemId))
                        .collect(Collectors.toList()),
                new Cliente(objDto.getCliente())
        );
    }
}
