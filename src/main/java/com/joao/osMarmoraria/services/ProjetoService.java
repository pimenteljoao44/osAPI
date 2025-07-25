package com.joao.osMarmoraria.services;
import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.StatusProjeto;
import com.joao.osMarmoraria.domain.enums.TipoProjeto;
import com.joao.osMarmoraria.dtos.*;
import com.joao.osMarmoraria.repository.*;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
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

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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

        // Removido: projeto.setValorTotal(BigDecimal.ONE); // Valor temporário incorreto

        projeto = projetoRepository.save(projeto);
        if (projetoDTO.getItens() != null && !projetoDTO.getItens().isEmpty()) {
            salvarItens(projeto.getId(), projetoDTO.getItens());
        }
        List<ProjetoItem> itensSalvos = projetoItemRepository.findByProjetoId(projeto.getId());
        calcularValoresProjeto(projeto, itensSalvos);

        projeto = projetoRepository.save(projeto);

        return convertToDTO(projetoRepository.findByIdWithDetails(projeto.getId())
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado após criação")));
    }

    public ProjetoDTO atualizarProjeto(Integer id, ProjetoDTO projetoDTO) {
        Projeto projetoExistente = projetoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado com ID: " + id));
        projetoExistente.setNome(projetoDTO.getNome());
        projetoExistente.setDescricao(projetoDTO.getDescricao());
        projetoExistente.setTipoProjeto(projetoDTO.getTipoProjeto());
        projetoExistente.setDataPrevista(projetoDTO.getDataPrevista());
        projetoExistente.setObservacoes(projetoDTO.getObservacoes());
        projetoExistente.setMargemLucro(projetoDTO.getMargemLucro());
        if (projetoDTO.getMedidas() != null) {
            projetoExistente.setProfundidade(projetoDTO.getMedidas().getProfundidade());
            projetoExistente.setLargura(projetoDTO.getMedidas().getLargura());
            projetoExistente.setAltura(projetoDTO.getMedidas().getAltura());
            projetoExistente.setObservacoesMedidas(projetoDTO.getMedidas().getObservacoes());
        }
        calcularMedidasProjeto(projetoExistente);
        projetoItemRepository.deleteByProjetoId(id);
        if (projetoDTO.getItens() != null && !projetoDTO.getItens().isEmpty()) {
            salvarItens(id, projetoDTO.getItens());
        }


        List<ProjetoItem> itensAtualizados = projetoItemRepository.findByProjetoId(id);
        calcularValoresProjeto(projetoExistente, itensAtualizados);

        projetoExistente = projetoRepository.save(projetoExistente); // Salva os valores calculados
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

    public CalculoOrcamentoDTO calcularOrcamento(ProjetoDTO projetoDTO) {
        CalculoOrcamentoDTO calculo = new CalculoOrcamentoDTO();

        // 1. Calcular valor dos materiais
        BigDecimal valorMateriais = BigDecimal.ZERO;
        if (projetoDTO.getItens() != null && !projetoDTO.getItens().isEmpty()) {
            valorMateriais = projetoDTO.getItens().stream()
                    .map(item -> {
                        if (item.getQuantidade() != null && item.getValorUnitario() != null) {
                            return item.getQuantidade().multiply(item.getValorUnitario());
                        }
                        return BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal valorMaoObra = BigDecimal.ZERO;
        BigDecimal taxaMaoObraPorM2 = this.getTaxaMaoObraPorTipo(projetoDTO.getTipoProjeto());

        if (projetoDTO.getMedidas() != null &&
                projetoDTO.getMedidas().getProfundidade() != null &&
                projetoDTO.getMedidas().getLargura() != null) {
            BigDecimal area = projetoDTO.getMedidas().getProfundidade()
                    .multiply(projetoDTO.getMedidas().getLargura());
            valorMaoObra = area.multiply(taxaMaoObraPorM2);
        }

        BigDecimal subtotal = valorMateriais.add(valorMaoObra);
        BigDecimal margemLucro = projetoDTO.getMargemLucro() != null ?
                projetoDTO.getMargemLucro() : new BigDecimal("20.00");

        BigDecimal multiplicadorLucro = BigDecimal.ONE.add(
                margemLucro.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
        );

        BigDecimal valorTotal = subtotal.multiply(multiplicadorLucro);

        calculo.setValorMateriais(valorMateriais);
        calculo.setValorMaoObra(valorMaoObra);
        calculo.setMargemLucro(margemLucro);
        calculo.setValorTotal(valorTotal);

        return calculo;
    }

    private BigDecimal getTaxaMaoObraPorTipo(TipoProjeto tipoProjeto) {
        if (tipoProjeto == null) {
            return new BigDecimal("50.00");
        }
        switch (tipoProjeto) {
            case BANHEIRO:
                return new BigDecimal("80.00");
            case COZINHA:
                return new BigDecimal("70.00");
            case CUBA:
                return new BigDecimal("120.00");
            case BANCADA:
                return new BigDecimal("60.00");
            case ESCADA:
                return new BigDecimal("100.00");
            case LAREIRA:
                return new BigDecimal("90.00");
            case SOLEIRA:
                return new BigDecimal("40.00");
            case PIA:
                return new BigDecimal("110.00");
            case OUTROS:
            default:
                return new BigDecimal("50.00");
        }
    }

    private void calcularValoresProjeto(Projeto projeto, List<ProjetoItem> itensProjeto) {
        BigDecimal valorMateriais = BigDecimal.ZERO;
        if (itensProjeto != null) {
            valorMateriais = itensProjeto.stream()
                    .map(ProjetoItem::getValorTotal) // Assume que getValorTotal já foi calculado
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal valorMaoObra = BigDecimal.ZERO;
        BigDecimal taxaMaoObraPorM2 = this.getTaxaMaoObraPorTipo(projeto.getTipoProjeto());

        if (projeto.getArea() != null) {
            valorMaoObra = projeto.getArea().multiply(taxaMaoObraPorM2);
        }

        // Calcular valor total com margem de lucro
        BigDecimal subtotal = valorMateriais.add(valorMaoObra);
        BigDecimal margemLucro = projeto.getMargemLucro() != null ?
                projeto.getMargemLucro() : new BigDecimal("20.00");

        BigDecimal multiplicadorLucro = BigDecimal.ONE.add(
                margemLucro.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
        );

        BigDecimal valorTotal = subtotal.multiply(multiplicadorLucro);

        projeto.setValorMaoObra(valorMaoObra);
        projeto.setValorTotal(valorTotal);
    }

    public List<MaterialSugeridoDTO> obterMateriaisSugeridos(TipoProjeto tipoProjeto, MedidasProjetoDTO medidas) {
        List<MaterialSugeridoDTO> materiais = new ArrayList<>();

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

    private ProjetoDTO convertToDTO(Projeto projeto) {
        if (projeto == null) {
            return null;
        }

        ProjetoDTO dto = new ProjetoDTO();
        dto.setId(projeto.getId());
        dto.setNome(projeto.getNome());
        dto.setDescricao(projeto.getDescricao());
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

        if (projeto.getUsuarioCriacao() != null) {
            dto.setUsuarioCriacao(projeto.getUsuarioCriacao().getId());
        } else {
            throw new IllegalStateException("Projeto não possui usuário de criação associado");
        }

        if (projeto.getCliente() != null) {
            dto.setClienteId(projeto.getCliente().getCliId());
        }

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

        List<ProjetoItem> itens = projetoItemRepository.findByProjetoIdWithProduto(projeto.getId());
        List<ProjetoItemDTO> itensDTO = itens.stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
        dto.setItens(itensDTO);

        return dto;
    }

    private ProjetoItemDTO convertItemToDTO(ProjetoItem item) {
        ProjetoItemDTO dto = new ProjetoItemDTO();
        dto.setId(item.getId());
        dto.setProjetoId(item.getProjetoId());
        dto.setProdutoId(item.getProdutoId());
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
        projeto.setTipoProjeto(dto.getTipoProjeto());
        projeto.setStatus(dto.getStatus() != null ? dto.getStatus() : StatusProjeto.ORCAMENTO);
        projeto.setDataInicio(dto.getDataInicio());
        projeto.setDataPrevista(dto.getDataPrevista());
        projeto.setDataFinalizacao(dto.getDataFinalizacao());
        projeto.setValorTotal(dto.getValorTotal() != null ? dto.getValorTotal() : BigDecimal.ZERO);
        projeto.setValorMaoObra(dto.getValorMaoObra() != null ? dto.getValorMaoObra() : BigDecimal.ZERO);
        projeto.setMargemLucro(dto.getMargemLucro() != null ? dto.getMargemLucro() : new BigDecimal("20.00"));
        projeto.setObservacoes(dto.getObservacoes());
        projeto.setDataCriacao(dto.getDataCriacao() != null ? dto.getDataCriacao() : LocalDate.now());
        projeto.setDataAtualizacao(dto.getDataAtualizacao() != null ? dto.getDataAtualizacao() : LocalDate.now());

        if (dto.getClienteId() != null) {
            Cliente cliente = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new ObjectNotFoundException("Cliente não encontrado com ID: " + dto.getClienteId()));

            projeto.setCliente(cliente);
        } else {
            throw new ObjectNotFoundException("ID do Cliente é obrigatório.");
        }


        if (dto.getUsuarioCriacao() != null) {
            Usuario usuarioCriacao = usuarioRepository.findById(dto.getUsuarioCriacao())
                    .orElseThrow(() -> new ObjectNotFoundException("Usuário de criação não encontrado com ID: " + dto.getUsuarioCriacao()));

            projeto.setUsuarioCriacao(usuarioCriacao);
        } else {
            throw new ObjectNotFoundException("ID do Usuário de criação é obrigatório.");
        }
        if (dto.getMedidas() != null) {
            projeto.setProfundidade(dto.getMedidas().getProfundidade());
            projeto.setLargura(dto.getMedidas().getLargura());
            projeto.setAltura(dto.getMedidas().getAltura());
            projeto.setObservacoesMedidas(dto.getMedidas().getObservacoes());
        }

        return projeto;
    }
}
