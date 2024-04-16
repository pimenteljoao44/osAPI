package com.joao.osMarmoraria;




import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication
public class OsMarmorariaApplication {
	
	public static void main(String[] args) {

		System.out.println("JDBC_DATABASE_URL: " + System.getenv("DATABASE_URL"));
		System.out.println("JDBC_DATABASE_USERNAME: " + System.getenv("JDBC_DATABASE_USERNAME"));
		System.out.println("JDBC_DATABASE_PASSWORD: " + System.getenv("JDBC_DATABASE_PASSWORD"));
		SpringApplication.run( OsMarmorariaApplication.class,args);
	}
}
