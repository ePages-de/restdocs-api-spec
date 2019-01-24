package com.epages.restdocs.apispec.openapi2

import com.epages.restdocs.apispec.model.FieldDescriptor
import com.epages.restdocs.apispec.model.HTTPMethod
import com.epages.restdocs.apispec.model.HeaderDescriptor
import com.epages.restdocs.apispec.model.Oauth2Configuration
import com.epages.restdocs.apispec.model.ParameterDescriptor
import com.epages.restdocs.apispec.model.RequestModel
import com.epages.restdocs.apispec.model.ResourceModel
import com.epages.restdocs.apispec.model.ResponseModel
import com.epages.restdocs.apispec.model.SecurityRequirements
import com.epages.restdocs.apispec.model.SecurityType
import io.swagger.models.Swagger
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.io.File

class OpenApi20GeneratorMultiModelsTest {

    @Test
    fun `should convert resource model with list in response to openapi`() {
        val api = givenGetProductResourceModel()
        val openapi = whenOpenApiObjectGenerated(api)

        then(openapi.definitions).hasSize(6)
    }

    private fun whenOpenApiObjectGenerated(api: List<ResourceModel>): Swagger {
        val openapi = OpenApi20Generator.generate(
            resources = api,
            oauth2SecuritySchemeDefinition = Oauth2Configuration(
                "http://example.com/token",
                "http://example.com/authorize",
                arrayOf("application", "accessCode")
            ),
            description = "API description",
            tagDescriptions = mapOf("tag1" to "tag1 description", "tag2" to "tag2 description")
        )

        println(ApiSpecificationWriter.serialize("json", openapi))
        return openapi
    }

    private fun givenGetProductResourceModel(): List<ResourceModel> {
        return listOf(
            ResourceModel(
                operationId = "products-get-product",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductRequest(),
                response = getProduct200Response(getPayloadExample("/products-get-product.json"))
            ),
            ResourceModel(
                operationId = "products-list",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductsListRequest(),
                response = getProductsListResponse(getPayloadExample("/products-list.json"))
            ),
            ResourceModel(
                operationId = "products-create",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = createProductRequest(getPayloadExample("/products-create-request.json")),
                response = createProductResponse(getPayloadExample("/products-create-response.json"))
            ),
            ResourceModel(
                operationId = "orders-list",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getOrdersListRequest(),
                response = getOrdersListResponse(getPayloadExample("/orders-list.json"))
            ),
            ResourceModel(
                operationId = "orders-get-order",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getOrderRequest(),
                response = getOrderResponse(getPayloadExample("/orders-get-order.json"))
            ),
            ResourceModel(
                operationId = "merchant-shop-attribute-get-list",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getShopAttributesRequest(),
                response = getShopAttributesResponse(getPayloadExample("/merchant-shop-attribute-get-list.json"))
            )
        )
    }

    private fun createProductResponse(examplePayload: String): ResponseModel {
        return ResponseModel(
            status = 200,
            contentType = "application/json",
            headers = listOf(),
            responseFields = listOf(),
            example = examplePayload
        )
    }

    private fun createProductRequest(examplePayload: String): RequestModel {
        return RequestModel(
            path = "/products",
            method = HTTPMethod.POST,
            contentType = "application/json",
            securityRequirements = SecurityRequirements(
                type = SecurityType.OAUTH2,
                requiredScopes = listOf("prod:r")
            ),
            headers = listOf(),
            pathParameters = listOf(),
            requestParameters = listOf(),
            requestFields = listOf(
                FieldDescriptor(
                    path = "_links",
                    description = "See <<hypermedia,Hypermedia>>",
                    type = "OBJECT"
                ),
                FieldDescriptor(
                    path = "name",
                    description = "The name of the product.",
                    type = "STRING"
                ),
                FieldDescriptor(
                    path = "sku",
                    description = "The stock keeping unit (SKU) corresponding to the product.",
                    type = "STRING"
                ),
                FieldDescriptor(
                    path = "visible",
                    description = "Indicates if the product is visible in the online shop. Can be `true` or `false`.",
                    type = "STRING"
                )),
            example = examplePayload
        )
    }

    private fun getProductsListResponse(payloadExample: String): ResponseModel {
        return ResponseModel(
            status = 200,
            contentType = "application/json",
            headers = listOf(),
            responseFields = listOf(
                FieldDescriptor(
                    path = "_embedded.products[].sku",
                    description = "The stock keeping unit (SKU) corresponding to the product.",
                    type = "STRING"
                ),
                FieldDescriptor(
                    path = "_links",
                    description = "See <<hypermedia,Hypermedia>>",
                    type = "OBJECT"
                ),
                FieldDescriptor(
                    path = "page",
                    description = "See <<pagination,Pagination>>",
                    type = "OBJECT"
                ),
                FieldDescriptor(
                    path = "_embedded.products[].name",
                    description = "The name of the product.",
                    type = "STRING"
                )
            ),
            example = """{
  "_embedded" : {
    "products" : [ {
      "_embedded" : {
        "availability" : {
          "availabilityState" : "IN_STOCK",
          "availableStock" : 20,
          "stockThreshold" : 2,
          "purchasable" : true,
          "_links" : {
            "self" : {
              "href" : "https://yourshop.api.urn/products/3d39bf56-c12e-4c92-bcbf-7255ef8c76b2/availability"
            }
          }
        }
      },
      "sku" : "vino020",
      "salesPrice" : {
        "taxModel" : "NET",
        "currency" : "EUR",
        "amount" : 8.7,
        "derivedPrice" : {
          "taxModel" : "GROSS",
          "currency" : "EUR",
          "amount" : 54.989,
          "taxRate" : 0.011
        }
      },
      "listPrice" : {
        "taxModel" : "NET",
        "currency" : "EUR",
        "amount" : 10.95,
        "derivedPrice" : {
          "taxModel" : "GROSS",
          "currency" : "EUR",
          "amount" : 76.989,
          "taxRate" : 0.011
        }
      },
      "manufacturerPrice" : {
        "taxModel" : "NET",
        "currency" : "EUR",
        "amount" : 99.95,
        "derivedPrice" : {
          "taxModel" : "GROSS",
          "currency" : "EUR",
          "amount" : 118.9405,
          "taxRate" : 0.19
        }
      },
      "onSale" : true,
      "tags" : [ "Bestseller", "Red Wine", "Sale" ],
      "productIdentifiers" : [ {
        "type" : "EAN",
        "value" : "9780134308135"
      } ],
      "visible" : true,
      "taxClass" : "REGULAR",
      "shippingWeight" : 100,
      "maxOrderQuantity" : 6,
      "shippingDimension" : {
        "length" : 1500,
        "width" : 1000,
        "height" : 2000
      },
      "refPrice" : {
        "refQuantity" : 1,
        "unit" : "LITER",
        "quantity" : 0.75,
        "price" : {
          "taxModel" : "NET",
          "currency" : "EUR",
          "amount" : 11.6,
          "derivedPrice" : {
            "taxModel" : "GROSS",
            "currency" : "EUR",
            "amount" : 73.319,
            "taxRate" : 0.011
          }
        }
      },
      "shippingPeriod" : {
        "minDays" : 2,
        "maxDays" : null,
        "displayUnit" : "WEEKS"
      },
      "name" : "Rioja Castillo de Puerto (2013)",
      "description" : "Spain\nRioja Tempranillo",
      "manufacturer" : "Grape Vineyard",
      "essentialFeatures" : "Dry. 12% alcohol. Best vine variety.",
      "_id" : "3d39bf56-c12e-4c92-bcbf-7255ef8c76b2",
      "_links" : {
        "self" : {
          "href" : "https://yourshop.api.urn/products/3d39bf56-c12e-4c92-bcbf-7255ef8c76b2"
        },
        "product" : {
          "href" : "https://yourshop.api.urn/products/3d39bf56-c12e-4c92-bcbf-7255ef8c76b2"
        },
        "availability" : {
          "href" : "https://yourshop.api.urn/products/3d39bf56-c12e-4c92-bcbf-7255ef8c76b2/availability"
        },
        "attributes" : {
          "href" : "https://yourshop.api.urn/products/3d39bf56-c12e-4c92-bcbf-7255ef8c76b2/attributes"
        },
        "attachments" : {
          "href" : "https://yourshop.api.urn/products/3d39bf56-c12e-4c92-bcbf-7255ef8c76b2/attachments"
        },
        "images" : {
          "href" : "https://yourshop.api.urn/products/3d39bf56-c12e-4c92-bcbf-7255ef8c76b2/images"
        },
        "google-shopping" : {
          "href" : "http://google-shopping/google-products/3d39bf56-c12e-4c92-bcbf-7255ef8c76b2"
        },
        "amazon" : {
          "href" : "http://amazon/amazon-products/3d39bf56-c12e-4c92-bcbf-7255ef8c76b2"
        },
        "default-image" : {
          "href" : "https://yourshop.api.urn/products/3d39bf56-c12e-4c92-bcbf-7255ef8c76b2/default-image"
        },
        "default-image-data" : {
          "href" : "https://yourshop.api.urn/api/core-storage/images/photostore-2.JPG?hash=8a627f655c68f56dfbbf217ab7d5563281225998{&width,height,upscale}",
          "templated" : true
        },
        "default-image-metadata" : {
          "href" : "https://yourshop.api.urn/api/core-storage/images/photostore-2.JPG?hash=8a627f655c68f56dfbbf217ab7d5563281225998&download=no"
        }
      }
    } ]
  },
  "_links" : {
    "self" : {
      "href" : "https://yourshop.api.urn/products?page=0&size=20"
    },
    "search" : {
      "href" : "https://yourshop.api.urn/products/search"
    }
  },
  "page" : {
    "size" : 20,
    "totalElements" : 1,
    "totalPages" : 1,
    "number" : 0
  }
}"""
        )
    }

    private fun getProductsListRequest(): RequestModel {
        return RequestModel(
            path = "/products",
            method = HTTPMethod.GET,
            contentType = "application/json",
            securityRequirements = SecurityRequirements(
                type = SecurityType.OAUTH2,
                requiredScopes = listOf("prod:r")
            ),
            headers = listOf(),
            pathParameters = listOf(),
            requestParameters = listOf(),
            requestFields = listOf()
        )
    }

    private fun getShopAttributesResponse(examplePayload: String): ResponseModel {
        return ResponseModel(
            status = 200,
            contentType = "application/json",
            headers = listOf(),
            responseFields = listOf(
                FieldDescriptor(
                    path = "_embedded.attributes",
                    description = "A collection of shop attributes.",
                    type = "ARRAY"
                ),
                FieldDescriptor(
                    path = "_embedded.attributes[*].name",
                    description = "The name of the shop attribute, e.g. `default-sorting-products`.",
                    type = "ARRAY"
                ),
                FieldDescriptor(
                    path = "_embedded.attributes[*].value",
                    description = "The value of the shop attribute.",
                    type = "ARRAY"
                ),
                FieldDescriptor(
                    path = "_links",
                    description = "See <<hypermedia,Hypermedia>>",
                    type = "OBJECT"
                ),
                FieldDescriptor(
                    path = "page",
                    description = "See <<pagination,Pagination>>",
                    type = "OBJECT"
                )
            ),
            example = examplePayload
        )
    }

    private fun getShopAttributesRequest(): RequestModel {
        return RequestModel(
            path = "/shop/attributes",
            method = HTTPMethod.GET,
            securityRequirements = SecurityRequirements(
                type = SecurityType.OAUTH2,
                requiredScopes = listOf("shat:r")
            ),
            headers = listOf(),
            pathParameters = listOf(),
            requestParameters = listOf(),
            requestFields = listOf()
        )
    }

    private fun getOrderResponse(examplePayload: String): ResponseModel {
        return ResponseModel(
            status = 200,
            contentType = "application/json",
            headers = listOf(
                HeaderDescriptor(
                    name = "SIGNATURE",
                    description = "This is some signature",
                    type = "STRING",
                    optional = false
                )
            ),
            responseFields = listOf(
                FieldDescriptor(
                    path = "cartId",
                    description = "The ID of the cart this order has been created from (if existing).",
                    type = "STRING"
                ),
                FieldDescriptor(
                    path = "currency",
                    description = "The currency in which the order was entered.",
                    type = "STRING"
                )
            ),
            example = examplePayload
        )
    }

    private fun getOrderRequest(): RequestModel {
        return RequestModel(
            path = "/orders/{order-id}",
            method = HTTPMethod.GET,
            securityRequirements = SecurityRequirements(
                type = SecurityType.OAUTH2,
                requiredScopes = listOf("ordr:r")
            ),
            headers = listOf(
                HeaderDescriptor(
                    name = "Authorization",
                    description = "Access token",
                    type = "string",
                    optional = false
                )
            ),
            pathParameters = listOf(),
            requestParameters = listOf(),
            requestFields = listOf()
        )
    }

    private fun getOrdersListResponse(examplePayload: String): ResponseModel {
        return ResponseModel(
            status = 200,
            contentType = "application/json",
            headers = listOf(),
            responseFields = listOf(
                FieldDescriptor(
                    path = "_embedded.orders[]",
                    description = "The list of orders as documented in <<resources-order-get, Show order details>>.",
                    type = "ARRAY"
                ),
                FieldDescriptor(
                    path = "_links",
                    description = "See <<hypermedia,Hypermedia>>",
                    type = "OBJECT"
                ),
                FieldDescriptor(
                    path = "page",
                    description = "See <<pagination,Pagination>>",
                    type = "OBJECT"
                )
            ),
            example = examplePayload
        )
    }

    private fun getOrdersListRequest(): RequestModel {
        return RequestModel(
            path = "/orders",
            method = HTTPMethod.GET,
            securityRequirements = SecurityRequirements(
                type = SecurityType.OAUTH2,
                requiredScopes = listOf("ordr:r")
            ),
            headers = listOf(),
            pathParameters = listOf(),
            requestParameters = listOf(),
            requestFields = listOf()
        )
    }

    private fun getProductRequest(): RequestModel {
        return RequestModel(
            path = "/products/{id}",
            method = HTTPMethod.GET,
            contentType = "application/json",
            securityRequirements = SecurityRequirements(
                type = SecurityType.OAUTH2,
                requiredScopes = listOf("prod:r")
            ),
            headers = listOf(
                HeaderDescriptor(
                    name = "Authorization",
                    description = "Access token",
                    type = "string",
                    optional = false
                )
            ),
            pathParameters = listOf(
                ParameterDescriptor(
                    name = "id",
                    description = "Product ID",
                    type = "STRING",
                    optional = false,
                    ignored = false
                )
            ),
            requestParameters = listOf(
                ParameterDescriptor(
                    name = "locale",
                    description = "Localizes the product fields to the given locale code",
                    type = "STRING",
                    optional = true,
                    ignored = false
                )
            ),
            requestFields = listOf()
        )
    }

    private fun getPayloadExample(fileName: String): String {
        val productPayload = OpenApi20GeneratorMultiModelsTest::class.java!!.getResource(fileName)
        val productPayloadFile = File(productPayload.toURI())
        return productPayloadFile.readText()
    }

    private fun getProduct200Response(example: String): ResponseModel {
        return ResponseModel(
            status = 200,
            contentType = "application/json",
            headers = listOf(
                HeaderDescriptor(
                    name = "SIGNATURE",
                    description = "This is some signature",
                    type = "STRING",
                    optional = false
                )
            ),
            responseFields = listOf(
                FieldDescriptor(
                    path = "_id",
                    description = "ID of the product",
                    type = "STRING"
                ),
                FieldDescriptor(
                    path = "description",
                    description = "Product description, localized.",
                    type = "STRING"
                )
            ),
            example = example
        )
    }
}
