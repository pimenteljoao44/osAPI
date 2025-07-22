package com.joao.osMarmoraria.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


import com.joao.osMarmoraria.services.DBservice;

@Configuration
@Profile("test")
public class TestConfig {
 
	@Autowired
	private DBservice  dataBaseService;
	
	@Bean
	public void instanciaDB() {
		
	}
}
