package com.epages.restdocs.apispec.sample

import com.epages.restdocs.apispec.ConstrainedFields
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.data.rest.webmvc.RestMediaTypes
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation
import org.springframework.restdocs.hypermedia.HypermediaDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.restdocs.snippet.Snippet
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@AutoConfigureRestDocs
@ExtendWith(SpringExtension::class)
class ProductRestIntegrationKtTest : BaseIntegrationTest() {

    @Autowired
    private val objectMapper: ObjectMapper? = null

    private val fields = ConstrainedFields(Product::class.java)

    @Test
    fun should_get_products() {
        givenProduct()
        givenProduct("Fancy Shirt", "15.10")
        givenProduct("Fancy Shoes", "75.95")

        whenProductsAreRetrieved()

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("_embedded.products", Matchers.hasSize<Any>(2)))
            .andDo(
                document(
                    identifier = "products-get", snippets = arrayOf<Snippet>(
                        PayloadDocumentation.responseFields(
                            PayloadDocumentation.subsectionWithPath("_embedded.products[].name")
                                .description("The name of the product."),
                            PayloadDocumentation.fieldWithPath("_embedded.products[].price")
                                .description("The price of the product."),
                            PayloadDocumentation.subsectionWithPath("_embedded.products[]._links")
                                .description("The product links."),
                            PayloadDocumentation.fieldWithPath("page.size").description("The size of one page."),
                            PayloadDocumentation.fieldWithPath("page.totalElements")
                                .description("The total number of elements found."),
                            PayloadDocumentation.fieldWithPath("page.totalPages")
                                .description("The total number of pages."),
                            PayloadDocumentation.fieldWithPath("page.number").description("The current page number."),
                            PayloadDocumentation.fieldWithPath("page").description("Paging information"),
                            PayloadDocumentation.subsectionWithPath("_links").description("Links section")
                        ),

                        HypermediaDocumentation.links(
                            HypermediaDocumentation.linkWithRel("first").description("Link to the first page"),
                            HypermediaDocumentation.linkWithRel("next").description("Link to the next page"),
                            HypermediaDocumentation.linkWithRel("last").description("Link to the next page"),
                            HypermediaDocumentation.linkWithRel("self").ignored(),
                            HypermediaDocumentation.linkWithRel("profile").ignored()
                        ),
                        HeaderDocumentation.requestHeaders(
                            HeaderDocumentation.headerWithName("accept").description("accept header")
                        ),
                        RequestDocumentation.queryParameters(
                            RequestDocumentation.parameterWithName("page").description("The page to be requested."),
                            RequestDocumentation.parameterWithName("size")
                                .description("Parameter determining the size of the requested page."),
                            RequestDocumentation.parameterWithName("sort")
                                .description("Information about sorting items.")
                        )
                    )
                )
            )
    }

    @Test
    fun should_get_product() {
        givenProduct()

        whenProductIsRetrieved()

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("name", Matchers.notNullValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("price", Matchers.notNullValue()))
            .andDo(
                document(
                    identifier = "product-get", snippets = arrayOf<Snippet>(
                        PayloadDocumentation.responseFields(
                            fields.withPath("name").description("The name of the product."),
                            fields.withPath("price").description("The price of the product."),
                            PayloadDocumentation.subsectionWithPath("_links").description("Links section")
                        )
                    )
                )
            )
    }

    @Test
    fun should_create_product() {
        givenProductPayload()

        whenProductIsCreated()

        resultActions
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andDo(
                document(
                    identifier = "products-create", snippets = arrayOf<Snippet>(
                        PayloadDocumentation.requestFields(
                            fields.withPath("name").description("The name of the product."),
                            fields.withPath("price").description("The price of the product.")
                        )
                    )
                )
            )
    }

    @Test
    fun should_update_product() {
        givenProduct()
        givenProductPayload("Updated name", "12.12")

        whenProductIsPatched()

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(
                document(
                    identifier = "product-patch", snippets = arrayOf<Snippet>(
                        PayloadDocumentation.requestFields(
                            fields.withPath("name").description("The name of the product."),
                            fields.withPath("price").description("The price of the product.")
                        )
                    )
                )
            )
    }

    @Test
    fun should_fail_to_update_product_with_negative_price() {
        givenProduct()
        givenProductPayload("Updated name", "-12.12")

        whenProductIsPatched()

        resultActions
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andDo(document("product-patch-constraint-violation"))
    }

    @Test
    fun should_partially_update_product() {
        givenProduct()
        givenPatchPayload()

        whenProductIsPatchedJsonPatch()

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(
                document(
                    identifier = "product-patch-json-patch", snippets = arrayOf<Snippet>(
                        PayloadDocumentation.requestFields(
                            fields.withPath("[].op").description("Patch operation."),
                            fields.withPath("[].path").description("The path of the field."),
                            fields.withPath("[].value").description("The value to assign.")
                        )
                    )
                )
            )
    }

    private fun givenPatchPayload() {
        json = objectMapper!!.writeValueAsString(
            ImmutableList.of(
                ImmutableMap.of(
                    "op", "replace",
                    "path", "/name",
                    "value", "Fancy socks"
                )
            )
        )
    }

    private fun whenProductIsRetrieved() {
        resultActions = mockMvc.perform(RestDocumentationRequestBuilders.get("/products/{id}", productId))
    }

    private fun whenProductIsPatched() {
        resultActions = mockMvc.perform(
            RestDocumentationRequestBuilders.patch("/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
        )
    }

    private fun whenProductIsPatchedJsonPatch() {
        resultActions = mockMvc.perform(
            RestDocumentationRequestBuilders.patch("/products/{id}", productId)
                .contentType(RestMediaTypes.JSON_PATCH_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
        )
    }

    private fun whenProductsAreRetrieved() {
        resultActions = mockMvc.perform(
            RestDocumentationRequestBuilders.get("/products")
                .header("accept", RestMediaTypes.HAL_JSON)
                .param("page", "0")
                .param("size", "2")
                .param("sort", "name asc")
        )
            .andDo(MockMvcResultHandlers.print())
    }
}
