package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.Cliente;
import com.joao.osMarmoraria.domain.enums.TipoPessoa;
import com.joao.osMarmoraria.dtos.ClienteDTO;
import com.joao.osMarmoraria.repository.ClienteRepository;
import com.joao.osMarmoraria.repository.EnderecoRepository;
import com.joao.osMarmoraria.repository.PessoaRepository;
import com.joao.osMarmoraria.services.exceptions.DataIntegratyViolationException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private EnderecoRepository enderecoRepository;

    @InjectMocks
    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAll_ShouldReturnListOfClientes() {
        List<Cliente> clientes = new ArrayList<>();
        when(clienteRepository.findAll()).thenReturn(clientes);

        List<Cliente> result = clienteService.findAll();

        assertEquals(clientes, result);
    }

    @Test
    void findById_ShouldReturnCliente_WhenClienteExists() {
        int id = 1;
        Cliente cliente = new Cliente();
        cliente.setCliId(id);
        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));

        Cliente result = clienteService.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getCliId());
    }

    @Test
    void findById_ShouldThrowObjectNotFoundException_WhenClienteDoesNotExist() {
        int id = 1;
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> clienteService.findById(id));
    }
}