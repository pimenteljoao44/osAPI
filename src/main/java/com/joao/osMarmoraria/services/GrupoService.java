package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.Grupo;
import com.joao.osMarmoraria.domain.Produto;
import com.joao.osMarmoraria.dtos.GrupoDTO;
import com.joao.osMarmoraria.repository.GrupoRepository;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Service
public class GrupoService {

    @Autowired
    private GrupoRepository grupoRepository;

    public List<Grupo> findAll(){return grupoRepository.findAll();}

    public Grupo findById(Integer id) {
        Optional<Grupo> obj = grupoRepository.findById(id);
        return obj.orElseThrow(() -> new ObjectNotFoundException(
                "Objeto não encontrado! id:" + id + ",tipo: " + Grupo.class.getName()));
    }

    public Grupo create (@Valid GrupoDTO grupoDTO) {
        if(grupoRepository.existsByNome(grupoDTO.getNome())) {
            throw new DataIntegrityViolationException("Este produto já está cadastrado na base de dados!");
        }
        Grupo newGrupo = new Grupo();
        Grupo grupoPai = findById(grupoDTO.getGrupoPaiId());
        newGrupo.setNome(grupoDTO.getNome());
        newGrupo.setAtivo(grupoDTO.getAtivo());
        newGrupo.setGrupoPai(grupoPai);
        grupoRepository.save(newGrupo);
        return newGrupo;
    }

    public Grupo update(Integer id,@Valid GrupoDTO grupoDTO) {
        Grupo grupo = findById(id);
        Grupo grupoPai = findById(grupoDTO.getGrupoPaiId());
        grupo.setAtivo(grupoDTO.getAtivo());
        grupo.setNome(grupoDTO.getNome());
        grupo.setGrupoPai(grupoPai);
        return grupoRepository.save(grupo);
    }

    public void  delete(Integer id) {
        Grupo grupo = findById(id);
        grupoRepository.deleteById(grupo.getId());
    }

    public Grupo fromDTO(GrupoDTO obj) {
        Grupo grupo = new Grupo();
        Grupo grupoPai = findById(obj.getGrupoPaiId());
        grupo.setNome(obj.getNome());
        grupo.setGrupoPai(grupoPai);
        grupo.setAtivo(obj.getAtivo());
        return grupo;
    }
}
