package com.joao.osMarmoraria.config;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


import com.joao.osMarmoraria.services.DBservice;

@Configuration
@Profile("test")
@RequiredArgsConstructor
public class TestConfig {
 
	private final DBservice  dataBaseService;
	
	@Bean
	public void instanciaDB() {
		
	}
}
