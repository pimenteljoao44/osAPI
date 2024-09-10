package com.joao.osMarmoraria.services;

import com.joao.osMarmoraria.domain.*;

import com.joao.osMarmoraria.dtos.ProdutoDTO;
import com.joao.osMarmoraria.repository.FornecedorRepository;
import com.joao.osMarmoraria.repository.GrupoRepository;
import com.joao.osMarmoraria.repository.OsRepository;
import com.joao.osMarmoraria.repository.ProdutoRepository;
import com.joao.osMarmoraria.services.exceptions.DataIntegratyViolationException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

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
    private OsRepository osRepository;

    @Autowired
    private GrupoRepository grupoRepository;
    public List<Produto> findAll(){return produtoRepository.findAll();}

    public Produto findById(Integer id){
        Optional<Produto> obj = produtoRepository.findById(id);
        return obj.orElseThrow(() -> new ObjectNotFoundException(
                "Objeto não encontrado! id:" + id + ",tipo: " + Produto.class.getName()));
    }

    public Produto create(@Valid ProdutoDTO produtoDTO) {
        if (produtoRepository.existsByNome(produtoDTO.getNome())) {
            throw new DataIntegrityViolationException("Este produto já está cadastrado na base de dados!");
        }

        Grupo grupo = grupoRepository.findById(produtoDTO.getGrupo().getId())
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Grupo não encontrado! id:" + produtoDTO.getGrupo().getId() + ",tipo: " + Grupo.class.getName()));

        Produto newProd = new Produto();
        newProd.setNome(produtoDTO.getNome());
        newProd.setPrecoCusto(produtoDTO.getPreco());
        newProd.setAtivo(produtoDTO.getAtivo());
        newProd.setEstoque(produtoDTO.getEstoque());
        newProd.setQuantidade(produtoDTO.getQuantidade());
        newProd.setGrupo(grupo);

        if (produtoDTO.getFornecedor() != null) {
            Fornecedor fornecedor = findFornecedorById(produtoDTO.getFornecedor());
            newProd.setFornecedor(fornecedor);
        }

        if (produtoDTO.getOrdemDeServico() != null) {
            OrdemDeServico os = findOSById(produtoDTO.getOrdemDeServico());
            newProd.setOrdemDeServico(os);
        }

        produtoRepository.save(newProd);

        return newProd;
    }


    public Produto update(Integer id, @Valid ProdutoDTO produtoDTO) {
        Produto produto = findById(id);
        Grupo grupo = grupoRepository.findById(produtoDTO.getGrupo().getId()).orElseThrow(() -> new ObjectNotFoundException("Grupo not found"));

        produto.setNome(produtoDTO.getNome());
        produto.setGrupo(grupo);
        produto.setAtivo(produtoDTO.getAtivo());
        produto.setEstoque(produtoDTO.getEstoque());
        produto.setPrecoCusto(produtoDTO.getPreco());
        produto.setQuantidade(produtoDTO.getQuantidade());
        if (produtoDTO.getFornecedor() != null) {
            Fornecedor fornecedor = findFornecedorById(produtoDTO.getFornecedor());
            produto.setFornecedor(fornecedor);
        }
        if (produtoDTO.getOrdemDeServico() != null) {
            OrdemDeServico os = findOSById(produtoDTO.getOrdemDeServico());
            produto.setOrdemDeServico(os);
        }
        return produtoRepository.save(produto);
    }


    public void delete(Integer id) {
        Produto produto = findById(id);
        if(produto.getOrdemDeServico() != null) {
            throw new DataIntegratyViolationException("O Produto é pertencente a uma ordem de serviço, não pode ser deletado!");
        }
        produtoRepository.deleteById(produto.getProdId());
    }

    public Fornecedor findFornecedorById(Integer id) {
        Optional<Fornecedor> f = fornecedorRepository.findById(id);
        return f.orElseThrow(() -> new ObjectNotFoundException(
                "Objeto não encontrado! id:" + id + ",tipo: " + Fornecedor.class.getName()));
    }

    public OrdemDeServico findOSById(Integer id){
        Optional<OrdemDeServico> os = osRepository.findById(id);
        return os.orElseThrow(() -> new ObjectNotFoundException(
                "Objeto não encontrado! id:" + id + ",tipo: " + OrdemDeServico.class.getName()));
    }

    public Produto fromDTO(ProdutoDTO obj) {
        OrdemDeServico os = findOSById(obj.getOrdemDeServico());
        Fornecedor fornecedor = findFornecedorById(obj.getFornecedor());
        Produto newProduto = new Produto();
        newProduto.setNome(obj.getNome());
        newProduto.setGrupo(obj.getGrupo());
        newProduto.setAtivo(obj.getAtivo());
        newProduto.setEstoque(obj.getEstoque());
        newProduto.setPrecoCusto(obj.getPreco());
        newProduto.setQuantidade(obj.getQuantidade());
        newProduto.setFornecedor(fornecedor);
        newProduto.setOrdemDeServico(os);
        return newProduto;
    }
}
