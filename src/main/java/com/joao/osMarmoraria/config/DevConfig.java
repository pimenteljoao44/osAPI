package com.joao.osMarmoraria.config;

import lombok.RequiredArgsConstructor;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


import com.joao.osMarmoraria.services.DBservice;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevConfig {
 
	private final DBservice  dataBaseService;
	
	@Value("${spring.jpa.hibernate.ddl-auto}")
	private String ddl;
	
	@Bean
	public boolean instanciaDB() {
		
		if(ddl.equals("create")) {
			this.dataBaseService.instanciaDB();
		}
		return false;
	}
}
