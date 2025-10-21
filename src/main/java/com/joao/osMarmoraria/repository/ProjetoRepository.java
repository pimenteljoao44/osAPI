package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.Projeto;
import com.joao.osMarmoraria.domain.enums.StatusProjeto;
import com.joao.osMarmoraria.domain.enums.TipoProjeto;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Integer> {

    @Query("SELECT p FROM Projeto p WHERE p.id = :id")
    Optional<Projeto> findByIdWithDetails(@Param("id") Integer id);

    @Query(value = "SELECT p FROM Projeto p LEFT JOIN FETCH p.cliente",
            countQuery = "SELECT COUNT(p) FROM Projeto p")
    Page<Projeto> findAllWithCliente(Pageable pageable);

    @Query("SELECT p FROM Projeto p LEFT JOIN p.cliente c LEFT JOIN c.pessoa pe WHERE " +
            "(:nome IS NULL OR p.nome LIKE CONCAT('%', CAST(:nome AS string), '%')) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:tipoProjeto IS NULL OR p.tipoProjeto = :tipoProjeto) AND " +
            "(:clienteId IS NULL OR p.cliente.cliId = :clienteId) AND " +
            "(:clienteNome IS NULL OR LOWER(pe.nome) LIKE CONCAT('%', CAST(:clienteNome AS string), '%'))")
    Page<Projeto> findWithFilters(@Param("nome") String nome,
                                  @Param("status") StatusProjeto status,
                                  @Param("tipoProjeto") TipoProjeto tipoProjeto,
                                  @Param("clienteId") Integer clienteId,
                                  @Param("clienteNome") String clienteNome,
                                  Pageable pageable);

    List<Projeto> findByStatus(StatusProjeto status);

    List<Projeto> findByTipoProjeto(TipoProjeto tipoProjeto);

    @Query("SELECT p FROM Projeto p WHERE p.cliente.cliId = :clienteId AND p.status = 'APROVADO'")
    List<Projeto> findProjetosAprovadosByCliente(@Param("clienteId") Integer clienteId);

    List<Projeto> findByCliente_CliId(Integer clienteId);
    @Query("SELECT p FROM Projeto p WHERE p.cliente.cliId = :clienteId AND p.status = :status")
    List<Projeto> findByClienteIdAndStatus(@Param("clienteId") Integer clienteId,
                                           @Param("status") StatusProjeto status);
    @Query("SELECT p FROM Projeto p WHERE p.dataCriacao BETWEEN :dataInicio AND :dataFim")
    List<Projeto> findByPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query("SELECT COUNT(p) FROM Projeto p WHERE p.status = :status")
    Integer countByStatus(@Param("status") StatusProjeto status);

    @Query("SELECT p FROM Projeto p WHERE p.dataPrevista < :data AND p.status IN :statusList")
    List<Projeto> findProjetosAtrasados(@Param("data") LocalDate data, @Param("statusList") List<StatusProjeto> statusList);

    @Query("SELECT SUM(p.valorTotal) FROM Projeto p WHERE p.status = :status")
    Double sumValorTotalByStatus(@Param("status") StatusProjeto status);

    @Query("SELECT COUNT(p) > 0 FROM Projeto p WHERE p.nome = :nome AND p.cliente.cliId = :clienteId")
    boolean existsByNomeAndClienteId(@Param("nome") String nome, @Param("clienteId") Integer clienteId);

    boolean existsByUsuarioCriacao_Id(Integer usuarioId); // Adicionado para verificar relações com Usuario

    boolean existsByCliente_CliId(Integer clienteId); // Adicionado para verificar relações com Cliente

}
