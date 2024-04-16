package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.Cidade;
import com.joao.osMarmoraria.domain.Estado;
import com.joao.osMarmoraria.dtos.EstadoDTO;
import com.joao.osMarmoraria.repository.CidadeRepository;
import com.joao.osMarmoraria.repository.EstadoRepository;
import com.joao.osMarmoraria.services.exceptions.DataIntegratyViolationException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class EstadoService {
    @Autowired
    EstadoRepository estadoRepository;

    @Autowired
    CidadeRepository cidadeRepository;

    public Estado findById(Integer id) {
        Optional<Estado> obj = estadoRepository.findById(id);
        return obj.orElseThrow(() -> new ObjectNotFoundException("Objeto não encontrado id:" + id + " tipo:" +
                Estado.class.getName()));
    }

    public List<Estado> findAll() {
        return estadoRepository.findAll();
    }

    public Estado create(@Valid EstadoDTO estado) {
        if (estadoRepository.existsByNomeOrSigla(estado.getNome(), estado.getSigla())) {
            throw new DataIntegratyViolationException("Estado com o mesmo nome ou sigla já existe");
        }

        Estado newEstado = fromDTO(estado);
        newEstado.setDataCriacao(new Date());
        return estadoRepository.save(newEstado);
    }

    public Estado update(Integer id, @Valid EstadoDTO estadoDTO) {
        Estado estado = findById(id);
        estado.setNome(estadoDTO.getNome());
        estado.setSigla(estadoDTO.getSigla());
        estado.setDataAtualizacao(new Date());

        return estadoRepository.save(estado);
    }

    public void delete(Integer id) {
        Estado estado = findById(id);
        List<Cidade> cidades = estado.getCidades();

        if (cidades != null && !cidades.isEmpty()) {
            for (Cidade cidade : cidades) {
                cidadeRepository.deleteById(cidade.getCidId());
            }
        }

        estadoRepository.deleteById(id);
    }

    public Estado fromDTO(EstadoDTO obj) {
        Estado newEstado = new Estado();
        newEstado.setNome(obj.getNome());
        newEstado.setSigla(obj.getSigla());
        newEstado.setDataCriacao(obj.getDataCriacao());
        newEstado.setDataAtualizacao(obj.getDataAtualizacao());
        return newEstado;
    }
}