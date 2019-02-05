
package com.example.webtestclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@SpringBootApplication
@EnableWebFlux
public class SampleWebTestClientApplication {

	@Bean
	public RouterFunction<ServerResponse> routerFunction() {
		return RouterFunctions.route(RequestPredicates.GET("/"), (request) -> ServerResponse.status(HttpStatus.OK).syncBody("Hello, World"));
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleWebTestClientApplication.class, args);
	}

}
