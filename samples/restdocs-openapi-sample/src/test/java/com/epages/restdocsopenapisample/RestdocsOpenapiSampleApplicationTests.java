package com.epages.restdocsopenapisample;

import com.epages.restdocs.openapi.HeaderDescriptorWithType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RestdocsOpenapiSampleApplicationTests {

	@Test
	public void contextLoads() throws JsonProcessingException {
		System.out.println(new ObjectMapper().writeValueAsString(
				new HeaderDescriptorWithType("some").optional().description("some")));
	}

}
