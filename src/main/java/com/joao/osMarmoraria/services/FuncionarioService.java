package com.joao.osMarmoraria.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.joao.osMarmoraria.domain.*;
import com.joao.osMarmoraria.domain.enums.TipoPessoa;
import com.joao.osMarmoraria.dtos.*;
import com.joao.osMarmoraria.exceptions.DeletionRestrictedException;
import com.joao.osMarmoraria.repository.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.joao.osMarmoraria.services.exceptions.DataIntegratyViolationException;
import com.joao.osMarmoraria.services.exceptions.ObjectNotFoundException;

import javax.transaction.Transactional;
import javax.validation.Valid;

@Service
public class FuncionarioService {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // Injetando UsuarioRepository

    @Autowired
    private ServicoRepository servicoRepository; // Injetando ServicoRepository

    @Autowired
    private OrdemServicoRepository ordemServicoRepository; // Injetando OrdemServicoRepository

    @Autowired
    private CompraRepository compraRepository; // Injetando CompraRepository

    @Autowired
    private VendaRepository vendaRepository; // Injetando VendaRepository

    @Autowired
    private EstadoRepository estadoRepository;

    @Autowired
    private CidadeRepository cidadeRepository;

    public Funcionario findById(Integer id) {
        Optional<Funcionario> obj = funcionarioRepository.findById(id);
        return obj.orElseThrow(() -> new ObjectNotFoundException(
                "Objeto não encontrado! id:" + id + ",tipo: " + Cliente.class.getName()));
    }

    public List<Funcionario> findAll() {
        return funcionarioRepository.findAll();
    }

    public Cidade handleCidade(CidadeInputDTO cidadeDTO) {
        // Busca por ID
        if (cidadeDTO.getId() != null) {
            Optional<Cidade> cidadeOpt = cidadeRepository.findById(cidadeDTO.getId());
            if (cidadeOpt.isPresent()) {
                return cidadeOpt.get();
            }
        }
        // Busca por nome+UF
        Optional<Cidade> cidadeOpt = cidadeRepository.findByNomeAndUf(
                cidadeDTO.getNome(), cidadeDTO.getUf()
        );
        if (cidadeOpt.isPresent()) {
            return cidadeOpt.get();
        }
        // Se não existe, cria nova
        Cidade novaCidade = new Cidade();
        novaCidade.setId(cidadeDTO.getId());
        novaCidade.setNome(cidadeDTO.getNome());
        novaCidade.setUf(cidadeDTO.getUf());
        return cidadeRepository.save(novaCidade);
    }

    public Estado handleEstado(EstadoInputDTO estadoDTO) {
        if (estadoDTO.getId() != null) {
            Optional<Estado> estadoOpt = estadoRepository.findById(estadoDTO.getId());
            if (estadoOpt.isPresent()) {
                return estadoOpt.get();
            }
        }
        Optional<Estado> estadoOpt = estadoRepository.findByNomeAndSigla(
                estadoDTO.getNome(), estadoDTO.getSigla()
        );
        if (estadoOpt.isPresent()) {
            return estadoOpt.get();
        }
        Estado novoEstado = new Estado();
        novoEstado.setId(estadoDTO.getId());
        novoEstado.setNome(estadoDTO.getNome());
        novoEstado.setSigla(estadoDTO.getSigla());
        return estadoRepository.save(novoEstado);
    }

    @Transactional
    public Funcionario create(FuncionarioDTO objDTO) {
        if (findByDocumento(objDTO) != null) {
            throw new DataIntegratyViolationException("Pessoa já cadastrada na base de dados!");
        }

        Pessoa pessoa = createPessoaFromDTO(objDTO);
        pessoa = pessoaRepository.save(pessoa);

        Funcionario funcionario = new Funcionario();
        funcionario.setSalario(objDTO.getSalario());
        funcionario.setCargo(objDTO.getCargo());
        funcionario.setDataCriacao(new Date());
        funcionario.setPessoa(pessoa);

        return funcionarioRepository.save(funcionario);
    }

    @Transactional
    public Funcionario update(Integer id, @Valid FuncionarioDTO objDTO) {
        Funcionario oldObj = findById(id);

        boolean tipoPessoaAlterado = oldObj.getPessoa() instanceof PessoaFisica && objDTO.getTipoPessoa() == TipoPessoa.PESSOA_JURIDICA
                || oldObj.getPessoa() instanceof PessoaJuridica && objDTO.getTipoPessoa() == TipoPessoa.PESSOA_FISICA;

        if (tipoPessoaAlterado) {
            Pessoa novaPessoa = createPessoaFromDTO(objDTO);
            novaPessoa.setId(oldObj.getPessoa().getId());

            pessoaRepository.save(novaPessoa);
            oldObj.setPessoa(novaPessoa);
        } else {
            Pessoa pessoa = oldObj.getPessoa();
            pessoa.setNome(objDTO.getNome());
            pessoa.setTelefone(objDTO.getTelefone());

            if (pessoa instanceof PessoaFisica) {
                ((PessoaFisica) pessoa).setCpf(objDTO.getCpf());
                ((PessoaFisica) pessoa).setRg(objDTO.getRg());
            } else if (pessoa instanceof PessoaJuridica) {
                ((PessoaJuridica) pessoa).setCnpj(objDTO.getCnpj());
            }

            if (objDTO.getEndereco() != null) {
                Endereco endereco = pessoa.getEndereco();

                if (endereco == null) {
                    endereco = new Endereco();
                    endereco.setPessoa(pessoa);
                    pessoa.setEndereco(endereco);
                }

                endereco.setRua(objDTO.getEndereco().getRua());
                endereco.setNumero(objDTO.getEndereco().getNumero());
                endereco.setComplemento(objDTO.getEndereco().getComplemento());
                endereco.setBairro(objDTO.getEndereco().getBairro());

                if (objDTO.getEndereco().getCidade() != null) {
                    Cidade cidade = handleCidade(objDTO.getEndereco().getCidade());
                    endereco.setCidade(cidade);
                }

                if (objDTO.getEndereco().getEstado() != null) {
                    Estado estado = handleEstado(objDTO.getEndereco().getEstado());
                    endereco.setEstado(estado);
                }
            }

            pessoaRepository.save(pessoa);
        }

        oldObj.setSalario(objDTO.getSalario());
        oldObj.setCargo(objDTO.getCargo());
        oldObj.setDataAtualizacao(new Date());

        return funcionarioRepository.save(oldObj);
    }

    @Transactional
    public void delete(Integer id) {
        Funcionario funcionario = findById(id);

        if (usuarioRepository.existsByFuncionario_Id(id)) {
            throw new DeletionRestrictedException("Não é possível excluir este funcionário pois ele possui um usuário vinculado. Por favor, remova o usuário antes de tentar excluir o funcionário.");
        }
        if (servicoRepository.existsByFuncionario_Id(id)) {
            throw new DeletionRestrictedException("Não é possível excluir este funcionário pois ele está associado a serviços. Por favor, remova os serviços associados antes de tentar excluir o funcionário.");
        }
        if (ordemServicoRepository.existsByFuncionario_Id(id)) {
            throw new DeletionRestrictedException("Não é possível excluir este funcionário pois ele está associado a ordens de serviço. Por favor, remova as ordens de serviço associadas antes de tentar excluir o funcionário.");
        }
        if (compraRepository.existsByFuncionario_Id(id)) {
            throw new DeletionRestrictedException("Não é possível excluir este funcionário pois ele está associado a compras. Por favor, remova as compras associadas antes de tentar excluir o funcionário.");
        }
        if (vendaRepository.existsByFuncionario_Id(id)) {
            throw new DeletionRestrictedException("Não é possível excluir este funcionário pois ele está associado a vendas. Por favor, remova as vendas associadas antes de tentar excluir o funcionário.");
        }

        funcionarioRepository.delete(funcionario);
    }

    private Pessoa createPessoaFromDTO(FuncionarioDTO objDTO) {
        Pessoa pessoa = objDTO.getTipoPessoa() == TipoPessoa.PESSOA_FISICA ?
                new PessoaFisica() : new PessoaJuridica();

        if (pessoa instanceof PessoaFisica) {
            ((PessoaFisica) pessoa).setCpf(objDTO.getCpf());
            ((PessoaFisica) pessoa).setRg(objDTO.getRg());
        } else {
            ((PessoaJuridica) pessoa).setCnpj(objDTO.getCnpj());
        }
        pessoa.setNome(objDTO.getNome());
        pessoa.setTelefone(objDTO.getTelefone());

        if (pessoa instanceof PessoaFisica) {
            ((PessoaFisica) pessoa).setCpf(objDTO.getCpf());
            ((PessoaFisica) pessoa).setRg(objDTO.getRg());
        } else {
            ((PessoaJuridica) pessoa).setCnpj(objDTO.getCnpj());
        }

        if (objDTO.getEndereco() != null) {
            Endereco endereco = new Endereco();
            endereco.setRua(objDTO.getEndereco().getRua());
            endereco.setNumero(objDTO.getEndereco().getNumero());
            endereco.setComplemento(objDTO.getEndereco().getComplemento());
            endereco.setBairro(objDTO.getEndereco().getBairro());

            if (objDTO.getEndereco().getCidade() != null) {
                Cidade cidade = handleCidade(objDTO.getEndereco().getCidade());
                endereco.setCidade(cidade);
            }

            if (objDTO.getEndereco().getEstado() != null) {
                Estado estado = handleEstado(objDTO.getEndereco().getEstado());
                endereco.setEstado(estado);
            }

            endereco.setPessoa(pessoa);
            pessoa.setEndereco(endereco);
        }

        return pessoa;
    }

    private Pessoa findByDocumento(FuncionarioDTO objDTO) {
        if (objDTO.getTipoPessoa() == TipoPessoa.PESSOA_FISICA) {
            return pessoaRepository.findByCPF(objDTO.getCpf());
        } else if (objDTO.getTipoPessoa() == TipoPessoa.PESSOA_JURIDICA) {
            return pessoaRepository.findByCNPJ(objDTO.getCnpj());
        }

        return null;
    }
}