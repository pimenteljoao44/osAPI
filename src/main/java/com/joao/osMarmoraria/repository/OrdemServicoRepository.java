package com.joao.osMarmoraria.repository;


import com.joao.osMarmoraria.domain.OrdemServico;
import com.joao.osMarmoraria.domain.enums.StatusOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Integer> {

    @Query("SELECT os FROM OrdemServico os LEFT JOIN FETCH os.cliente LEFT JOIN FETCH os.projeto")
    List<OrdemServico> findAllWithDetails();

    @Query("SELECT os FROM OrdemServico os LEFT JOIN FETCH os.cliente LEFT JOIN FETCH os.projeto WHERE os.id = :id")
    Optional<OrdemServico> findByIdWithDetails(@Param("id") Integer id);

    Optional<OrdemServico> findByNumero(String numero);

    Optional<OrdemServico> findByProjetoId(Integer projetoId);

    List<OrdemServico> findByClienteId(Integer clienteId);

    List<OrdemServico> findByStatus(StatusOrdemServico status);

    @Query("SELECT os FROM OrdemServico os WHERE os.dataEmissao BETWEEN :dataInicio AND :dataFim")
    List<OrdemServico> findByPeriodo(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query("SELECT os FROM OrdemServico os WHERE os.dataPrevistaConclusao < :data AND os.status IN :statusList")
    List<OrdemServico> findOrdensAtrasadas(@Param("data") LocalDate data, @Param("statusList") List<StatusOrdemServico> statusList);

    @Query("SELECT COUNT(os) FROM OrdemServico os WHERE os.status = :status")
    Integer countByStatus(@Param("status") StatusOrdemServico status);

    @Query("SELECT os FROM OrdemServico os WHERE os.responsavel = :responsavel AND os.status IN :statusList")
    List<OrdemServico> findByResponsavelAndStatusIn(@Param("responsavel") String responsavel, @Param("statusList") List<StatusOrdemServico> statusList);

    boolean existsByNumero(String numero);

    boolean existsByProjetoId(Integer projetoId);

    boolean existsByFuncionario_Id(Integer funcionarioId); // Adicionado para verificar relações com Funcionario

    boolean existsByCliente_CliId(Integer clienteId); // CORRIGIDO: Usando cliId para Cliente

    List<OrdemServico> findByStatusAndDataPrevistaInicioIsNull(StatusOrdemServico status);

    List<OrdemServico> findByStatusAndDataPrevistaInicio(StatusOrdemServico status, LocalDate dataPrevistaInicio);

}
