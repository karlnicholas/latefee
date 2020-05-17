package com.fluxtest.latefee;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.fluxtest.fluxtestcommon.IdHolder;
import com.fluxtest.fluxtestcommon.LateFeeEntity;
import com.fluxtest.fluxtestcommon.SomeEntity;

import reactor.core.publisher.Mono;

@SpringBootApplication
@EntityScan("com.fluxtest.fluxtestcommon")
@RestController
@RequestMapping("/")
public class LatefeeApplication {

	public static void main(String[] args) {
		SpringApplication.run(LatefeeApplication.class, args);
	}
	
	@Bean
	WebClient getTransactionClient() {
		return WebClient.create("http://localhost:8080"); 
	}
	
	@Autowired
	WebClient transactionClient;

	@PostMapping("latefee")
	Mono<UUID> postLateFeeRequest(@RequestBody IdHolder idHolder) {
		return Mono.just(idHolder).flatMap(idh->
			transactionClient.get()
			.uri(uriBuilder->uriBuilder.path("/transaction/{id}").build(idh.getId()))
			.retrieve()
			.bodyToMono(SomeEntity.class)
			.flatMap(transaction->
				transactionClient
				.post()
				.uri("/latefee")
				.bodyValue(LateFeeEntity.builder().id(transaction.getId()).timestamp(LocalDateTime.now()).build())
				.retrieve()
				.bodyToMono(UUID.class)
				.flatMap(si->Mono.just(UUID.randomUUID())))
		);
	}
}
