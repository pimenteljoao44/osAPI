package com.joao.osMarmoraria.config;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


import com.joao.osMarmoraria.services.DBservice;

@Configuration
@Profile("dev")
public class DevConfig {
 
	@Autowired
	private DBservice  dataBaseService;
	
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
