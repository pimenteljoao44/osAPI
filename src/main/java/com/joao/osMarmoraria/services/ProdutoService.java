package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.UnidadeDeMedida;
import com.joao.osMarmoraria.dtos.ProdutoDTO;
import com.joao.osMarmoraria.repository.FornecedorRepository;
import com.joao.osMarmoraria.repository.GrupoRepository;
import com.joao.osMarmoraria.repository.ProdutoRepository;
import com.joao.osMarmoraria.services.exceptions.DataIntegratyViolationException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
            throw new DataIntegrityViolationException("Este produto já está cadastrado na base de dados!");
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
                throw new DataIntegrityViolationException("Unidade de medida inválida: " + produtoDTO.getUnidadeDeMedida());
            }
        }

        if (produtoDTO.getFornecedor() != null) {
            Fornecedor fornecedor = findFornecedorById(produtoDTO.getFornecedor());
            produto.setFornecedor(fornecedor);
        }
    }

    public void delete(Integer id) {
        Produto produto = findById(id);

        if (produto.getVenda() != null) {
            throw new DataIntegrityViolationException("O produto é pertencente a uma venda, não pode ser deletado!");
        }
        produtoRepository.deleteById(produto.getProdId());
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
