package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.Cidade;
import com.joao.osMarmoraria.domain.Estado;
import com.joao.osMarmoraria.dtos.CidadeDTO;
import com.joao.osMarmoraria.repository.CidadeRepository;
import com.joao.osMarmoraria.repository.EstadoRepository;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CidadeService {
    @Autowired
    CidadeRepository cidadeRepository;

    @Autowired
    EstadoRepository estadoRepository;

    public Cidade findById(Integer id) {
        Optional<Cidade> obj = cidadeRepository.findById(id);
        return obj.orElseThrow(() -> new ObjectNotFoundException("Objeto não encontrado id:" + id + " tipo:" +
                Estado.class.getName()));
    }

    private Estado findEstadoById(Integer estadoId) {
        Optional<Estado> estado = estadoRepository.findById(estadoId);
        return estado.orElseThrow(() -> new ObjectNotFoundException("Estado não encontrado id:" + estadoId));
    }

    public List<Cidade> findAll() {
        return cidadeRepository.findAll();
    }

    public Cidade create(@Valid CidadeDTO cidadeDTO) {

        Cidade newCidade = new Cidade();
        newCidade.setNome(cidadeDTO.getNome());
        newCidade.setId(cidadeDTO.getId());

        return cidadeRepository.save(newCidade);
    }


    public Cidade update(Integer id, @Valid CidadeDTO cidadeDTO) {
        Cidade cidade = findById(id);
        cidade.setNome(cidadeDTO.getNome());
        cidade.setId(cidadeDTO.getId());

        return cidadeRepository.save(cidade);
    }

    public void delete(Integer id) {
        Cidade cidade = findById(id);
        cidadeRepository.deleteById(cidade.getId());
    }

    public Cidade fromDTO(CidadeDTO obj) {
        Cidade newCidade = new Cidade();
        newCidade.setId(obj.getId());
        newCidade.setNome(obj.getNome());
        newCidade.setId(obj.getId());
        return newCidade;
    }
}