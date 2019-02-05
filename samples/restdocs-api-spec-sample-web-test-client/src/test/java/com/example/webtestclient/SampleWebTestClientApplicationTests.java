package com.example.webtestclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

@SpringBootTest
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
public class SampleWebTestClientApplicationTests {

	@Autowired
	ApplicationContext context;

	private WebTestClient webTestClient;

	@BeforeEach
	public void setUp(RestDocumentationContextProvider restDocumentation) {
		this.webTestClient = WebTestClient.bindToApplicationContext(context)
				.configureClient()
				.filter(documentationConfiguration(restDocumentation))
				.build();
	}

	@Test
	public void sample() throws Exception {
		this.webTestClient.get().uri("/").exchange()
			.expectStatus().isOk().expectBody()
			.consumeWith(document("sample", resource("sample description")));
	}

}
