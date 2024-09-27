package com.epages.restdocs.apispec.sample;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class BaseIntegrationTest {

    @Autowired MockMvc mockMvc;

    @Autowired ProductRepository productRepository;

    @Autowired CartRepository cartsRepository;

    @Autowired EntityLinks entityLinks;

    ResultActions resultActions;

    String json;

    String productId;

    @BeforeEach
    public void setUp() {
        cartsRepository.deleteAll();
        productRepository.deleteAll();
    }

    protected void givenProductPayload() {
        givenProductPayload("Fancy pants", "49.99");
    }

    protected void givenProductPayload(String name, String price) {
        json = String.format("{\n" +
                "    \"name\": \"%s\",\n" +
                "    \"price\": %s\n" +
                "}", name, price);
    }

    protected void givenProduct() throws Exception {
        givenProductPayload();
        whenProductIsCreated();
    }

    protected void givenProduct(String name, String price) throws Exception {
        givenProductPayload(name, price);
        whenProductIsCreated();
    }

    protected void whenProductIsCreated() throws Exception {
        resultActions = mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        String location = resultActions.andReturn().getResponse().getHeader(LOCATION);
        productId = location.substring(location.lastIndexOf("/") + 1);
    }
}
