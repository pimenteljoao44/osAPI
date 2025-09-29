package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.ProjetoMedida;
import com.joao.osMarmoraria.domain.enums.UnidadeDeMedida;
import com.joao.osMarmoraria.repository.ProjetoMedidaRepository;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProjetoMedidaService {

    @Autowired
    private ProjetoMedidaRepository projetoMedidaRepository;

    @Transactional(readOnly = true)
    public ProjetoMedida findById(Integer id) {
        Optional<ProjetoMedida> obj = projetoMedidaRepository.findById(id);
        return obj.orElseThrow(() -> new ObjectNotFoundException("Medida não encontrada! Id: " + id));
    }

    @Transactional(readOnly = true)
    public List<ProjetoMedida> findByProjetoId(Integer projetoId) {
        return projetoMedidaRepository.findByProjetoIdOrderByNome(projetoId);
    }

    @Transactional(readOnly = true)
    public List<ProjetoMedida> findMedidasComCoordenadas(Integer projetoId) {
        return projetoMedidaRepository.findMedidasComCoordenadas(projetoId);
    }

    @Transactional(readOnly = true)
    public List<ProjetoMedida> buscarPorNome(Integer projetoId, String nome) {
        return projetoMedidaRepository.findByProjetoIdAndNomeContainingIgnoreCase(projetoId, nome);
    }

    @Transactional
    public ProjetoMedida create(ProjetoMedida medida) {
        medida.setId(null);

        // Validar se já existe uma medida com o mesmo nome no projeto
        if (projetoMedidaRepository.existsByProjetoIdAndNomeIgnoreCase(medida.getProjetoId(), medida.getNome())) {
            throw new IllegalArgumentException("Já existe uma medida com este nome no projeto");
        }

        // Garantir que a unidade de medida não seja nula
        if (medida.getUnidadeMedida() == null) {
            medida.setUnidadeMedida(UnidadeDeMedida.METROS);
        }

        return projetoMedidaRepository.save(medida);
    }

    @Transactional
    public ProjetoMedida update(Integer id, ProjetoMedida medidaAtualizada) {
        ProjetoMedida medidaExistente = findById(id);

        // Verificar se o nome foi alterado e se já existe outro com o mesmo nome
        if (!medidaExistente.getNome().equalsIgnoreCase(medidaAtualizada.getNome())) {
            if (projetoMedidaRepository.existsByProjetoIdAndNomeIgnoreCase(
                    medidaExistente.getProjetoId(), medidaAtualizada.getNome())) {
                throw new IllegalArgumentException("Já existe uma medida com este nome no projeto");
            }
        }

        // Atualizar campos
        medidaExistente.setNome(medidaAtualizada.getNome());
        medidaExistente.setLargura(medidaAtualizada.getLargura());
        medidaExistente.setAltura(medidaAtualizada.getAltura());
        medidaExistente.setProfundidade(medidaAtualizada.getProfundidade());
        medidaExistente.setEspessura(medidaAtualizada.getEspessura());
        medidaExistente.setObservacoes(medidaAtualizada.getObservacoes());
        medidaExistente.setCoordenadaX(medidaAtualizada.getCoordenadaX());
        medidaExistente.setCoordenadaY(medidaAtualizada.getCoordenadaY());
        medidaExistente.setRotacao(medidaAtualizada.getRotacao());

        // Se a unidade foi alterada, converter as medidas
        if (medidaAtualizada.getUnidadeMedida() != null &&
                medidaAtualizada.getUnidadeMedida() != medidaExistente.getUnidadeMedida()) {
            medidaExistente.converterParaUnidade(medidaAtualizada.getUnidadeMedida());
        }

        return projetoMedidaRepository.save(medidaExistente);
    }

    @Transactional
    public void delete(Integer id) {
        ProjetoMedida medida = findById(id);
        projetoMedidaRepository.delete(medida);
    }

    @Transactional
    public void deleteByProjetoId(Integer projetoId) {
        projetoMedidaRepository.deleteByProjetoId(projetoId);
    }

    @Transactional
    public ProjetoMedida converterUnidade(Integer id, UnidadeDeMedida novaUnidade) {
        ProjetoMedida medida = findById(id);
        medida.converterParaUnidade(novaUnidade);
        return projetoMedidaRepository.save(medida);
    }

    @Transactional
    public List<ProjetoMedida> converterTodasUnidades(Integer projetoId, UnidadeDeMedida novaUnidade) {
        List<ProjetoMedida> medidas = findByProjetoId(projetoId);

        for (ProjetoMedida medida : medidas) {
            medida.converterParaUnidade(novaUnidade);
        }

        return projetoMedidaRepository.saveAll(medidas);
    }

    @Transactional
    public ProjetoMedida atualizarCoordenadas(Integer id, BigDecimal x, BigDecimal y, BigDecimal rotacao) {
        ProjetoMedida medida = findById(id);
        medida.setCoordenadaX(x);
        medida.setCoordenadaY(y);
        medida.setRotacao(rotacao != null ? rotacao : BigDecimal.ZERO);
        return projetoMedidaRepository.save(medida);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularAreaTotalProjeto(Integer projetoId) {
        List<ProjetoMedida> medidas = findByProjetoId(projetoId);
        return medidas.stream()
                .map(ProjetoMedida::getAreaEmMetrosQuadrados)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularVolumeTotalProjeto(Integer projetoId) {
        List<ProjetoMedida> medidas = findByProjetoId(projetoId);
        return medidas.stream()
                .map(ProjetoMedida::getVolumeEmMetrosCubicos)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public long contarMedidasProjeto(Integer projetoId) {
        return projetoMedidaRepository.countByProjetoId(projetoId);
    }

    @Transactional
    public ProjetoMedida duplicarMedida(Integer id, String novoNome) {
        ProjetoMedida medidaOriginal = findById(id);

        // Verificar se já existe uma medida com o novo nome
        if (projetoMedidaRepository.existsByProjetoIdAndNomeIgnoreCase(medidaOriginal.getProjetoId(), novoNome)) {
            throw new IllegalArgumentException("Já existe uma medida com este nome no projeto");
        }

        ProjetoMedida novaMedida = new ProjetoMedida();
        novaMedida.setProjetoId(medidaOriginal.getProjetoId());
        novaMedida.setNome(novoNome);
        novaMedida.setLargura(medidaOriginal.getLargura());
        novaMedida.setAltura(medidaOriginal.getAltura());
        novaMedida.setProfundidade(medidaOriginal.getProfundidade());
        novaMedida.setEspessura(medidaOriginal.getEspessura());
        novaMedida.setUnidadeMedida(medidaOriginal.getUnidadeMedida());
        novaMedida.setObservacoes(medidaOriginal.getObservacoes());

        return projetoMedidaRepository.save(novaMedida);
    }
}
