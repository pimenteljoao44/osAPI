package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.UnidadeDeMedida;
import com.joao.osMarmoraria.dtos.ProdutoDTO;
import com.joao.osMarmoraria.repository.FornecedorRepository;
import com.joao.osMarmoraria.repository.GrupoRepository;
import com.joao.osMarmoraria.repository.ItemCompraRepository;
import com.joao.osMarmoraria.repository.ItemOrdemServicoRepository;
import com.joao.osMarmoraria.repository.ItemVendaRepository;
import com.joao.osMarmoraria.repository.MovimentacaoEstoqueRepository;
import com.joao.osMarmoraria.repository.ProdutoRepository;
import com.joao.osMarmoraria.repository.ProjetoItemRepository;
import com.joao.osMarmoraria.repository.ProjetoMaterialRepository;
import com.joao.osMarmoraria.repository.EstoqueReservadoRepository;
import com.joao.osMarmoraria.services.exceptions.DataIntegratyViolationException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {
    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private ProjetoItemRepository projetoItemRepository; // Injetando ProjetoItemRepository

    @Autowired
    private ProjetoMaterialRepository projetoMaterialRepository; // Injetando ProjetoMaterialRepository

    @Autowired
    private ItemCompraRepository itemCompraRepository; // Injetando ItemCompraRepository

    @Autowired
    private ItemOrdemServicoRepository itemOrdemServicoRepository; // Injetando ItemOrdemServicoRepository

    @Autowired
    private ItemVendaRepository itemVendaRepository; // Injetando ItemVendaRepository

    @Autowired
    private EstoqueReservadoRepository estoqueReservadoRepository; // Injetando EstoqueReservadoRepository

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository; // Injetando MovimentacaoEstoqueRepository

    public List<Produto> findAll() {
        return produtoRepository.findAll();
    }

    public Produto findById(Integer id) {
        Optional<Produto> obj = produtoRepository.findById(id);
        return obj.orElseThrow(() -> new ObjectNotFoundException(
                "Objeto não encontrado! id:" + id + ",tipo: " + Produto.class.getName()));
    }

    public Produto create(@Valid ProdutoDTO produtoDTO) {
        if (produtoRepository.existsByNome(produtoDTO.getNome())) {
            throw new DataIntegratyViolationException("Este produto já está cadastrado na base de dados!");
        }

        Produto newProd = new Produto();
        fromDTO(newProd, produtoDTO);

        return produtoRepository.save(newProd);
    }

    public Produto update(Integer id, @Valid ProdutoDTO produtoDTO) {
        Produto produto = findById(id);
        fromDTO(produto, produtoDTO);
        return produtoRepository.save(produto);
    }

    private void fromDTO(Produto produto, ProdutoDTO produtoDTO) {
        produto.setNome(produtoDTO.getNome());
        produto.setPrecoCusto(produtoDTO.getPreco());
        produto.setAtivo(produtoDTO.getAtivo());
        produto.setEstoque(produtoDTO.getEstoque());
        produto.setQuantidade(produtoDTO.getQuantidade());

        if (produtoDTO.getGrupo() != null && produtoDTO.getGrupo().getId() != null) {
            Grupo grupo = grupoRepository.findById(produtoDTO.getGrupo().getId())
                    .orElseThrow(() -> new ObjectNotFoundException(
                            "Grupo não encontrado! id:" + produtoDTO.getGrupo().getId() + ",tipo: " + Grupo.class.getName()));
            produto.setGrupo(grupo);
        }

        if (StringUtils.hasText(produtoDTO.getUnidadeDeMedida())) {
            try {
                UnidadeDeMedida um = UnidadeDeMedida.valueOf(produtoDTO.getUnidadeDeMedida());
                produto.setUnidadeDeMedida(um);
            } catch (IllegalArgumentException e) {
                throw new DataIntegratyViolationException("Unidade de medida inválida: " + produtoDTO.getUnidadeDeMedida());
            }
        }

        if (produtoDTO.getFornecedor() != null) {
            Fornecedor fornecedor = findFornecedorById(produtoDTO.getFornecedor());
            produto.setFornecedor(fornecedor);
        }
    }

    public void delete(Integer id) {
        Produto produto = findById(id);
        if (itemCompraRepository.existsByProduto(produto)) {
            throw new DataIntegratyViolationException("Não é possível excluir o produto, pois ele está associado a uma compra.");
        }

        if (itemOrdemServicoRepository.existsByProduto(produto)) {
            throw new DataIntegratyViolationException("Não é possível excluir o produto, pois ele está associado a uma ordem de serviço.");
        }

        if (itemVendaRepository.existsByProduto(produto)) {
            throw new DataIntegratyViolationException("Não é possível excluir o produto, pois ele está associado a uma venda.");
        }

        if (movimentacaoEstoqueRepository.existsByProduto(produto)) {
            throw new DataIntegratyViolationException("Não é possível excluir o produto, pois ele possui movimentações de estoque registradas.");
        }

        if (estoqueReservadoRepository.existsByProduto(produto)) {
            throw new DataIntegratyViolationException("Não é possível excluir o produto, pois ele possui estoque reservado.");
        }

        if (projetoItemRepository.existsByProduto(produto)) {
            throw new DataIntegratyViolationException("Não é possível excluir o produto, pois ele está associado a um item de projeto.");
        }

        if (projetoMaterialRepository.existsByProduto(produto)) {
            throw new DataIntegratyViolationException("Não é possível excluir o produto, pois ele está associado a um material de projeto.");
        }
        produtoRepository.deleteById(id);
    }

    private Fornecedor findFornecedorById(Integer id) {
        if (id == null) {
            return null;
        }
        Optional<Fornecedor> f = fornecedorRepository.findById(id);
        return f.orElseThrow(() -> new ObjectNotFoundException(
                "Objeto não encontrado! id:" + id + ",tipo: " + Fornecedor.class.getName()));
    }
}
