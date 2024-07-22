package com.app.Titanic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.jdbc.core.JdbcTemplate;
@EnableCaching
@SpringBootApplication
public class TitanicApplication {

	public static void main(String[] args) {
		SpringApplication.run(TitanicApplication.class, args);

	}
}
