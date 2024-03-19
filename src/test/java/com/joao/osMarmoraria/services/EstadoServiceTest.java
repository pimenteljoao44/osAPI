package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.Estado;
import com.joao.osMarmoraria.dtos.EstadoDTO;
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
import static org.mockito.Mockito.*;

class EstadoServiceTest {

    @Mock
    EstadoRepository estadoRepository;

    @InjectMocks
    EstadoService estadoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(estadoRepository.save(any(Estado.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void findById_ExistingId_ReturnEstado() {
        Estado estado = new Estado();
        estado.setEstId(1);
        estado.setNome("Sao Paulo");
        when(estadoRepository.findById(1)).thenReturn(Optional.of(estado));

        Estado result = estadoService.findById(1);

        assertNotNull(result);
        assertEquals(1, result.getEstId());
        assertEquals("Sao Paulo", result.getNome());
    }

    @Test
    void findById_NonExistingId_ThrowObjectNotFoundException() {
        when(estadoRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> estadoService.findById(1));
    }

    @Test
    void findAll_ReturnEstadoList() {
        List<Estado> estadoList = new ArrayList<>();
        estadoList.add(new Estado());
        estadoList.add(new Estado());
        when(estadoRepository.findAll()).thenReturn(estadoList);

        List<Estado> result = estadoService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void create_ValidEstadoDTO_ReturnCreatedEstado() {
        EstadoDTO estadoDTO = new EstadoDTO();
        estadoDTO.setNome("Rio de Janeiro");
        estadoDTO.setSigla("RJ");

        Estado result = estadoService.create(estadoDTO);

        assertNotNull(result);
        assertEquals("Rio de Janeiro", result.getNome());
        assertEquals("RJ", result.getSigla());
    }

    @Test
    void update_ExistingIdAndValidEstadoDTO_ReturnUpdatedEstado() {
        EstadoDTO estadoDTO = new EstadoDTO();
        estadoDTO.setNome("Updated Name");
        estadoDTO.setSigla("UPD");

        Estado existingEstado = new Estado();
        existingEstado.setEstId(1);
        existingEstado.setNome("Old Name");
        existingEstado.setSigla("OLD");

        when(estadoRepository.findById(1)).thenReturn(Optional.of(existingEstado));

        Estado result = estadoService.update(1, estadoDTO);

        assertNotNull(result);
        assertEquals("Updated Name", result.getNome());
        assertEquals("UPD", result.getSigla());
    }

    @Test
    void update_NonExistingId_ThrowObjectNotFoundException() {
        EstadoDTO estadoDTO = new EstadoDTO();
        estadoDTO.setNome("Updated Name");
        estadoDTO.setSigla("UPD");

        when(estadoRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> estadoService.update(1, estadoDTO));
    }

    @Test
    void delete_ExistingId_DeleteEstado() {
        Estado existingEstado = new Estado();
        existingEstado.setEstId(1);
        existingEstado.setNome("Sao Paulo");
        when(estadoRepository.findById(1)).thenReturn(Optional.of(existingEstado));

        estadoService.delete(1);

        verify(estadoRepository, times(1)).deleteById(1);
    }

    @Test
    void delete_NonExistingId_ThrowObjectNotFoundException() {
        when(estadoRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> estadoService.delete(1));
    }
}