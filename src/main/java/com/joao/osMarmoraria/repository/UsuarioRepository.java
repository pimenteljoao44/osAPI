package com.joao.osMarmoraria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.joao.osMarmoraria.domain.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Integer> {

	@Query("SELECT obj FROM Usuario obj WHERE obj.login =:login")
	Usuario findByLogin(@Param("login") String login);
}
