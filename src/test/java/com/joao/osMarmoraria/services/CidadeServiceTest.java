package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.Cidade;
import com.joao.osMarmoraria.domain.Estado;
import com.joao.osMarmoraria.dtos.CidadeDTO;
import com.joao.osMarmoraria.dtos.EstadoDTO;
import com.joao.osMarmoraria.repository.CidadeRepository;
import com.joao.osMarmoraria.repository.EstadoRepository;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class CidadeServiceTest {
    @Mock
    CidadeRepository cidadeRepository;

    @InjectMocks
    CidadeService cidadeService;

    @InjectMocks
    EstadoService estadoService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(cidadeRepository.save(any(Cidade.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void findById_ExistingId_ReturnCidade() {
        Cidade cidade = new Cidade();
        cidade.setCidId(1);
        cidade.setNome("Sao Paulo");
        when(cidadeRepository.findById(1)).thenReturn(Optional.of(cidade));

        Cidade result = cidadeService.findById(1);

        assertNotNull(result);
        assertEquals(1, result.getCidId());
        assertEquals("Sao Paulo", result.getNome());
    }

    @Test
    void findById_NonExistingId_ThrowObjectNotFoundException() {
        when(cidadeRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> cidadeService.findById(1));
    }

    @Test
    void findAll_ReturnCidadeList() {
        List<Cidade> cidadeList = new ArrayList<>();
        cidadeList.add(new Cidade());
        cidadeList.add(new Cidade());
        when(cidadeRepository.findAll()).thenReturn(cidadeList);

        List<Cidade> result = cidadeService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void update_ExistingId_ReturnUpdatedCidade() {
        Cidade existingCidade = new Cidade();
        existingCidade.setCidId(1);
        existingCidade.setNome("Sao Paulo");

        Estado estado = new Estado();
        estado.setEstId(3);
        estado.setSigla("PR");

        CidadeDTO cidadeDTO = new CidadeDTO();
        cidadeDTO.setNome("Updated Name");
        cidadeDTO.setEstado(new EstadoDTO(estado));

        when(cidadeRepository.findById(1)).thenReturn(Optional.of(existingCidade));
        when(cidadeRepository.save(any(Cidade.class))).thenReturn(existingCidade);

        Cidade result = cidadeService.update(1, cidadeDTO);

        assertNotNull(result);
        assertEquals("Updated Name", result.getNome());
    }

    @Test
    void update_NonExistingId_ThrowObjectNotFoundException() {
        Estado estado = new Estado();
        estado.setEstId(3);
        estado.setSigla("PR");

        CidadeDTO cidadeDTO = new CidadeDTO();
        cidadeDTO.setNome("Updated Name");
        cidadeDTO.setEstado(new EstadoDTO(estado));

        when(cidadeRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> cidadeService.update(1, cidadeDTO));
    }

    @Test
    void delete_ExistingId_DeleteCidade() {
        Cidade existingCidade = new Cidade();
        existingCidade.setCidId(1);
        existingCidade.setNome("Sao Paulo");
        when(cidadeRepository.findById(1)).thenReturn(Optional.of(existingCidade));

        cidadeService.delete(1);

        verify(cidadeRepository, times(1)).deleteById(1);
    }

    @Test
    void delete_NonExistingId_ThrowObjectNotFoundException() {
        when(cidadeRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> cidadeService.delete(1));
    }
}