package com.joao.osMarmoraria.services;
import com.joao.osMarmoraria.domain.Produto;
import com.joao.osMarmoraria.domain.Projeto;
import com.joao.osMarmoraria.domain.ProjetoItem;
import com.joao.osMarmoraria.domain.enums.StatusProjeto;
import com.joao.osMarmoraria.domain.enums.TipoProjeto;
import com.joao.osMarmoraria.dtos.*;
import com.joao.osMarmoraria.repository.ProdutoRepository;
import com.joao.osMarmoraria.repository.ProjetoItemRepository;
import com.joao.osMarmoraria.repository.ProjetoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjetoService {

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private ProjetoItemRepository projetoItemRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    // CRUD Operations
    public Page<ProjetoDTO> listarProjetos(Pageable pageable) {
        return projetoRepository.findAllWithCliente(pageable)
                .map(this::convertToDTO);
    }

    public Page<ProjetoDTO> listarComFiltros(String nome, StatusProjeto status, TipoProjeto tipoProjeto, Integer clienteId, Pageable pageable) {
        return projetoRepository.findWithFilters(nome, status, tipoProjeto, clienteId, pageable)
                .map(this::convertToDTO);
    }

    public ProjetoDTO buscarPorId(Integer id) {
        Projeto projeto = projetoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado com ID: " + id));
        return convertToDTO(projeto);
    }

    @Transactional
    public ProjetoDTO criarProjeto(ProjetoDTO projetoDTO) {
        if (projetoRepository.existsByNomeAndClienteId(projetoDTO.getNome(), projetoDTO.getClienteId())) {
            throw new IllegalArgumentException("Já existe um projeto com este nome para o cliente informado");
        }

        Projeto projeto = convertToEntity(projetoDTO);

        calcularMedidasProjeto(projeto);

        projeto.setValorTotal(BigDecimal.ONE); // Valor temporário para passar validação
        projeto = projetoRepository.save(projeto);

        if (projetoDTO.getItens() != null && !projetoDTO.getItens().isEmpty()) {
            salvarItens(projeto.getId(), projetoDTO.getItens());
        }

        calcularValoresItens(projeto);
        projeto.recalcularValorTotal();

        projeto = projetoRepository.save(projeto);

        return convertToDTO(projetoRepository.findByIdWithDetails(projeto.getId())
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado após criação")));
    }

    public ProjetoDTO atualizarProjeto(Integer id, ProjetoDTO projetoDTO) {
        Projeto projetoExistente = projetoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado com ID: " + id));

        // Atualizar campos
        projetoExistente.setNome(projetoDTO.getNome());
        projetoExistente.setDescricao(projetoDTO.getDescricao());
        projetoExistente.setTipoProjeto(projetoDTO.getTipoProjeto());
        projetoExistente.setDataPrevista(projetoDTO.getDataPrevista());
        projetoExistente.setObservacoes(projetoDTO.getObservacoes());
        projetoExistente.setMargemLucro(projetoDTO.getMargemLucro());

        // Atualizar medidas
        if (projetoDTO.getMedidas() != null) {
            projetoExistente.setProfundidade(projetoDTO.getMedidas().getProfundidade());
            projetoExistente.setLargura(projetoDTO.getMedidas().getLargura());
            projetoExistente.setAltura(projetoDTO.getMedidas().getAltura());
            projetoExistente.setObservacoesMedidas(projetoDTO.getMedidas().getObservacoes());
        }

        // Recalcular valores
        calcularMedidasProjeto(projetoExistente);

        // Atualizar itens
        projetoItemRepository.deleteByProjetoId(id);
        if (projetoDTO.getItens() != null && !projetoDTO.getItens().isEmpty()) {
            salvarItens(id, projetoDTO.getItens());
        }

        calcularValoresItens(projetoExistente);
        projetoExistente.recalcularValorTotal();

        projetoExistente = projetoRepository.save(projetoExistente);
        return convertToDTO(projetoRepository.findByIdWithDetails(projetoExistente.getId()).get());
    }

    public void excluirProjeto(Integer id) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado com ID: " + id));

        if (projeto.getStatus() == StatusProjeto.EM_PRODUCAO || projeto.getStatus() == StatusProjeto.ENTREGUE) {
            throw new IllegalStateException("Não é possível excluir projeto em produção ou entregue");
        }

        projetoRepository.delete(projeto);
    }

    // Operações de Status
    public ProjetoDTO atualizarStatus(Integer id, StatusProjeto novoStatus) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado com ID: " + id));

        StatusProjeto statusAtual = projeto.getStatus();

        // Validar transição de status
        if (!isTransicaoStatusValida(statusAtual, novoStatus)) {
            throw new IllegalStateException("Transição de status inválida: " + statusAtual + " -> " + novoStatus);
        }

        projeto.setStatus(novoStatus);

        // Definir datas baseadas no status
        switch (novoStatus) {
            case APROVADO:
                projeto.setDataInicio(LocalDate.now());
                break;
            case ENTREGUE:
                projeto.setDataFinalizacao(LocalDate.now());
                break;
        }

        projeto = projetoRepository.save(projeto);
        return convertToDTO(projeto);
    }

    // Cálculo de Orçamento
    public CalculoOrcamentoDTO calcularOrcamento(ProjetoDTO projetoDTO) {
        CalculoOrcamentoDTO calculo = new CalculoOrcamentoDTO();

        // Calcular valor dos materiais
        BigDecimal valorMateriais = BigDecimal.ZERO;
        if (projetoDTO.getItens() != null) {
            valorMateriais = projetoDTO.getItens().stream()
                    .map(item -> item.getQuantidade().multiply(item.getValorUnitario()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // Calcular mão de obra baseada na área (R$ 50 por m²)
        BigDecimal valorMaoObra = BigDecimal.ZERO;
        if (projetoDTO.getMedidas() != null &&
                projetoDTO.getMedidas().getProfundidade() != null &&
                projetoDTO.getMedidas().getLargura() != null) {

            BigDecimal area = projetoDTO.getMedidas().getProfundidade()
                    .multiply(projetoDTO.getMedidas().getLargura());
            valorMaoObra = area.multiply(new BigDecimal("50.00"));
        }

        // Calcular valor total com margem de lucro
        BigDecimal subtotal = valorMateriais.add(valorMaoObra);
        BigDecimal margemLucro = projetoDTO.getMargemLucro() != null ?
                projetoDTO.getMargemLucro() : new BigDecimal("20.00");
        BigDecimal multiplicadorLucro = BigDecimal.ONE.add(margemLucro.divide(new BigDecimal("100")));
        BigDecimal valorTotal = subtotal.multiply(multiplicadorLucro);

        calculo.setValorMateriais(valorMateriais);
        calculo.setValorMaoObra(valorMaoObra);
        calculo.setMargemLucro(margemLucro);
        calculo.setValorTotal(valorTotal);

        return calculo;
    }

    // Materiais Sugeridos
    public List<MaterialSugeridoDTO> obterMateriaisSugeridos(TipoProjeto tipoProjeto, MedidasProjetoDTO medidas) {
        List<MaterialSugeridoDTO> materiais = new ArrayList<>();

        // Lógica para sugerir materiais baseado no tipo de projeto
        List<Integer> produtoIds = obterProdutosSugeridosPorTipo(tipoProjeto);

        for (Integer produtoId : produtoIds) {
            produtoRepository.findById(produtoId).ifPresent(produto -> {
                MaterialSugeridoDTO material = new MaterialSugeridoDTO();
                material.setProdutoId(produto.getProdId());
                material.setProduto(produto);
                material.setQuantidadeRecomendada(calcularQuantidadeRecomendada(produto, medidas, tipoProjeto));
                material.setAplicacao(obterAplicacaoProduto(produto, tipoProjeto));
                materiais.add(material);
            });
        }

        return materiais;
    }

    // Relatórios
    public List<ProjetoDTO> obterProjetosPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        List<Projeto> projetos = projetoRepository.findByPeriodo(dataInicio, dataFim);
        return projetos.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Métodos auxiliares privados
    private void salvarItens(Integer projetoId, List<ProjetoItemDTO> itensDTO) {
        for (ProjetoItemDTO itemDTO : itensDTO) {
            ProjetoItem item = new ProjetoItem();
            item.setProjetoId(projetoId);
            item.setProdutoId(itemDTO.getProdutoId());
            item.setQuantidade(itemDTO.getQuantidade());
            item.setValorUnitario(itemDTO.getValorUnitario());
            item.setObservacoes(itemDTO.getObservacoes());
            projetoItemRepository.save(item);
        }
    }

    private void calcularMedidasProjeto(Projeto projeto) {
        if (projeto.getProfundidade() != null && projeto.getLargura() != null) {
            projeto.setArea(projeto.getProfundidade().multiply(projeto.getLargura()));
            projeto.setPerimetro(projeto.getProfundidade().add(projeto.getLargura()).multiply(new BigDecimal("2")));
        }
    }

    private void calcularValoresItens(Projeto projeto) {
        List<ProjetoItem> itens = projetoItemRepository.findByProjetoId(projeto.getId());
        BigDecimal valorMateriais = itens.stream()
                .map(ProjetoItem::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular mão de obra baseada na área
        BigDecimal valorMaoObra = BigDecimal.ZERO;
        if (projeto.getArea() != null) {
            valorMaoObra = projeto.getArea().multiply(new BigDecimal("50.00"));
        }

        projeto.setValorMaoObra(valorMaoObra);
    }

    private boolean isTransicaoStatusValida(StatusProjeto statusAtual, StatusProjeto novoStatus) {
        // Definir transições válidas
        switch (statusAtual) {
            case ORCAMENTO:
                return novoStatus == StatusProjeto.APROVADO || novoStatus == StatusProjeto.CANCELADO;
            case APROVADO:
                return novoStatus == StatusProjeto.EM_PRODUCAO || novoStatus == StatusProjeto.CANCELADO;
            case EM_PRODUCAO:
                return novoStatus == StatusProjeto.PRONTO || novoStatus == StatusProjeto.CANCELADO;
            case PRONTO:
                return novoStatus == StatusProjeto.ENTREGUE;
            case ENTREGUE:
            case CANCELADO:
                return false; // Estados finais
            default:
                return false;
        }
    }

    private List<Integer> obterProdutosSugeridosPorTipo(TipoProjeto tipoProjeto) {
        // Retornar IDs de produtos comumente usados por tipo de projeto
        switch (tipoProjeto) {
            case BANHEIRO:
                return Arrays.asList(1, 2, 3);
            case COZINHA:
                return Arrays.asList(4, 5, 6);
            case CUBA:
                return Arrays.asList(7, 8, 9);
            default:
                return Arrays.asList(1, 2, 3);
        }
    }


    private BigDecimal calcularQuantidadeRecomendada(Produto produto, MedidasProjetoDTO medidas, TipoProjeto tipoProjeto) {
        // Lógica simples para calcular quantidade baseada na área
        if (medidas.getProfundidade() != null && medidas.getLargura() != null) {
            BigDecimal area = medidas.getProfundidade().multiply(medidas.getLargura());
            return area.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ONE;
    }

    private String obterAplicacaoProduto(Produto produto, TipoProjeto tipoProjeto) {
        return "Aplicação em " + tipoProjeto.getDescricao().toLowerCase();
    }

    // Conversão DTO
    private ProjetoDTO convertToDTO(Projeto projeto) {
        ProjetoDTO dto = new ProjetoDTO();
        dto.setId(projeto.getId());
        dto.setNome(projeto.getNome());
        dto.setDescricao(projeto.getDescricao());
        dto.setClienteId(projeto.getClienteId());
        dto.setCliente(projeto.getCliente());
        dto.setTipoProjeto(projeto.getTipoProjeto());
        dto.setStatus(projeto.getStatus());
        dto.setDataInicio(projeto.getDataInicio());
        dto.setDataPrevista(projeto.getDataPrevista());
        dto.setDataFinalizacao(projeto.getDataFinalizacao());
        dto.setValorTotal(projeto.getValorTotal());
        dto.setValorMaoObra(projeto.getValorMaoObra());
        dto.setMargemLucro(projeto.getMargemLucro());
        dto.setObservacoes(projeto.getObservacoes());
        dto.setDataCriacao(projeto.getDataCriacao());
        dto.setDataAtualizacao(projeto.getDataAtualizacao());
        dto.setUsuarioCriacao(projeto.getUsuarioCriacao());

        // Medidas
        if (projeto.getProfundidade() != null || projeto.getLargura() != null || projeto.getAltura() != null) {
            MedidasProjetoDTO medidas = new MedidasProjetoDTO();
            medidas.setProfundidade(projeto.getProfundidade());
            medidas.setLargura(projeto.getLargura());
            medidas.setAltura(projeto.getAltura());
            medidas.setArea(projeto.getArea());
            medidas.setPerimetro(projeto.getPerimetro());
            medidas.setObservacoes(projeto.getObservacoesMedidas());
            dto.setMedidas(medidas);
        }

        // Itens
        List<ProjetoItem> itens = projetoItemRepository.findByProjetoIdWithProduto(projeto.getId());
        List<ProjetoItemDTO> itensDTO = itens.stream().map(this::convertItemToDTO).collect(Collectors.toList());
        dto.setItens(itensDTO);

        return dto;
    }

    private ProjetoItemDTO convertItemToDTO(ProjetoItem item) {
        ProjetoItemDTO dto = new ProjetoItemDTO();
        dto.setId(item.getId());
        dto.setProjetoId(item.getProjetoId());
        dto.setProdutoId(item.getProdutoId());
        dto.setProduto(item.getProduto());
        dto.setQuantidade(item.getQuantidade());
        dto.setValorUnitario(item.getValorUnitario());
        dto.setValorTotal(item.getValorTotal());
        dto.setObservacoes(item.getObservacoes());
        return dto;
    }

    private Projeto convertToEntity(ProjetoDTO dto) {
        Projeto projeto = new Projeto();
        projeto.setNome(dto.getNome());
        projeto.setDescricao(dto.getDescricao());
        projeto.setClienteId(dto.getClienteId());
        projeto.setTipoProjeto(dto.getTipoProjeto());
        projeto.setStatus(dto.getStatus() != null ? dto.getStatus() : StatusProjeto.ORCAMENTO);
        projeto.setDataPrevista(dto.getDataPrevista());
        projeto.setObservacoes(dto.getObservacoes());
        projeto.setMargemLucro(dto.getMargemLucro() != null ? dto.getMargemLucro() : new BigDecimal("20.00"));
        projeto.setUsuarioCriacao(dto.getUsuarioCriacao() != null ? dto.getUsuarioCriacao() : 1);

        projeto.setValorTotal(BigDecimal.ONE);

        if (dto.getMedidas() != null) {
            projeto.setProfundidade(dto.getMedidas().getProfundidade());
            projeto.setLargura(dto.getMedidas().getLargura());
            projeto.setAltura(dto.getMedidas().getAltura());
            projeto.setObservacoesMedidas(dto.getMedidas().getObservacoes());
        }

        return projeto;
    }
}
