package com.epages.restdocs.apispec.sample;

import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static lombok.AccessLevel.PROTECTED;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;

@FieldDefaults(level = PROTECTED)
class BaseIntegrationTest {

    @Autowired MockMvc mockMvc;

    @Autowired ProductRepository productRepository;

    @Autowired CartRepository cartsRepository;

    @Autowired EntityLinks entityLinks;

    ResultActions resultActions;

    String json;

    String productId;

    @Before
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

    protected void givenProduct() {
        givenProductPayload();
        whenProductIsCreated();
    }

    protected void givenProduct(String name, String price) {
        givenProductPayload(name, price);
        whenProductIsCreated();
    }

    @SneakyThrows
    protected void whenProductIsCreated() {
        resultActions = mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        String location = resultActions.andReturn().getResponse().getHeader(LOCATION);
        productId = location.substring(location.lastIndexOf("/") + 1);
    }
}
