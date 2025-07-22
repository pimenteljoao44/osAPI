package com.joao.osMarmoraria.repository;

import com.joao.osMarmoraria.domain.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompraRepository extends JpaRepository<Compra,Integer> {
}
