package com.example.bookshop_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BookshopGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookshopGatewayApplication.class, args);
	}

}
