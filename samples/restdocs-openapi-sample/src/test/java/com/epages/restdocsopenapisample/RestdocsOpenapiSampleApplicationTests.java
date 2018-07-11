package com.epages.restdocsopenapisample;

import com.epages.restdocs.openapi.HeaderDescriptorWithType;
import com.epages.restdocs.openapi.MockMvcRestDocumentationWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class RestdocsOpenapiSampleApplicationTests {

	@Test
	public void contextLoads() throws JsonProcessingException {
        MockMvcRestDocumentationWrapper.document("some", PayloadDocumentation.requestFields());
        System.out.println(new ObjectMapper().writeValueAsString(
                new HeaderDescriptorWithType("some").optional().description("some")));
	}

}
