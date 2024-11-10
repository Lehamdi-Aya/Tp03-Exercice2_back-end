package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.Arrays;

@SpringBootApplication
public class Tp03Application {

	public static void main(String[] args) {
		SpringApplication.run(Tp03Application.class, args);}
		@Bean
		public CorsFilter corsFilter() {
			CorsConfiguration corsConfig = new CorsConfiguration();

			// Allow all origins
			corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:4200"));

			// Allow credentials
			corsConfig.setAllowCredentials(true);

			// Allow all headers
			corsConfig.setAllowedHeaders(Arrays.asList("*"));

			// Allow all HTTP methods (GET, POST, PUT, DELETE, etc.)
			corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

			// Set the allowed max age
			corsConfig.setMaxAge(3600L);

			UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
			source.registerCorsConfiguration("/**", corsConfig);

			return new CorsFilter(source);
		}

	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOrigins("http://localhost:4200")
				.allowedMethods("GET", "POST", "PUT", "DELETE")
				.allowedHeaders("*")
				.exposedHeaders("traceparent", "tracestate");
	}

	}

