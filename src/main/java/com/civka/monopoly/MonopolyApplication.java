package com.civka.monopoly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@SpringBootApplication
//@EnableWebSocketMessageBroker
public class MonopolyApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonopolyApplication.class, args);
	}

}
