package com.epages.restdocs.apispec.sample

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters.Companion.builder
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.data.rest.webmvc.RestMediaTypes
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.hypermedia.HypermediaDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.snippet.Snippet
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@AutoConfigureRestDocs
@ExtendWith(SpringExtension::class)
class CartIntegrationKtTest : BaseIntegrationTest() {

    private var cartId: String? = null

    @Test
    fun should_create_cart() {
        whenCartIsCreated()

        resultActions
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andDo(document(identifier = "carts-create", snippets = arrayOf<Snippet>(resource("Create a cart"))))
    }

    @Test
    fun should_add_product_to_cart() {
        givenCart()
        givenProduct()

        whenProductIsAddedToCart()

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(
                document(
                    identifier = "cart-add-product",
                    snippets = arrayOf<Snippet>(resource("Add products to a cart"))
                )
            )
    }

    @Test
    fun should_get_cartKt() {
        givenCartWithProduct()

        whenCartIsRetrieved()

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("products", Matchers.hasSize<Any>(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("products[0].quantity", Matchers.`is`(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("products[0].product.name", Matchers.notNullValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("total", Matchers.notNullValue()))
            .andDo(
                document(
                    identifier = "cart-get",
                    snippets = arrayOf<Snippet>(
                        resource(
                            builder()
                                .description("Get a cart by id")
                                .pathParameters(
                                    parameterWithName("id").description("the cart id")
                                )
                                .responseFields(
                                    PayloadDocumentation.fieldWithPath("total")
                                        .description("Total amount of the cart."),
                                    PayloadDocumentation.fieldWithPath("products")
                                        .description("The product line item of the cart."),
                                    PayloadDocumentation.subsectionWithPath("products[]._links.product")
                                        .description("Link to the product."),
                                    PayloadDocumentation.fieldWithPath("products[].quantity")
                                        .description("The quantity of the line item."),
                                    PayloadDocumentation.subsectionWithPath("products[].product")
                                        .description("The product the line item relates to."),
                                    PayloadDocumentation.subsectionWithPath("_links").description("Links section.")
                                )
                                .links(
                                    HypermediaDocumentation.linkWithRel("self").ignored(),
                                    HypermediaDocumentation.linkWithRel("order").description("Link to order the cart.")
                                )
                                .build()
                        )
                    )
                )
            )
    }

    @Test
    fun should_order_cart() {
        givenCartWithProduct()

        whenCartIsOrdered()

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(document(identifier = "cart-order", snippets = arrayOf<Snippet>(resource("Order a cart"))))
    }

    private fun whenProductIsAddedToCart() {
        resultActions = mockMvc.perform(
            RestDocumentationRequestBuilders.post("/carts/{id}/products", cartId)
                .contentType(RestMediaTypes.TEXT_URI_LIST)
                .content(entityLinks.linkForItemResource(Product::class.java, productId).toUri().toString())
        )
    }

    private fun whenCartIsCreated() {
        resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/carts"))

        val location = resultActions.andReturn().response.getHeader(HttpHeaders.LOCATION)
        cartId = location!!.substring(location!!.lastIndexOf("/") + 1)
    }

    private fun whenCartIsRetrieved() {
        resultActions = mockMvc.perform(
            RestDocumentationRequestBuilders.get("/carts/{id}", cartId)
                .accept(RestMediaTypes.HAL_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
    }

    private fun whenCartIsOrdered() {
        resultActions = mockMvc.perform(RestDocumentationRequestBuilders.post("/carts/{id}/order", cartId))
    }

    private fun givenCart() {
        whenCartIsCreated()
        resultActions.andExpect(MockMvcResultMatchers.status().isCreated())
    }

    private fun givenCartWithProduct() {
        givenCart()
        givenProduct()
        whenProductIsAddedToCart()

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
    }
}
