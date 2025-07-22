package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface OsRepository extends JpaRepository<OrdemServico,Integer> {

}
