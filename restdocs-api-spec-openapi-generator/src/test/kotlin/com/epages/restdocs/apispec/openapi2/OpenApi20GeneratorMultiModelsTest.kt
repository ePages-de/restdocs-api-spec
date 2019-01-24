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
                response = getProduct200Response(getProductPayloadExample())
            ),
            ResourceModel(
                operationId = "products-list",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getProductsListRequest(),
                response = getProductsListResponse()
            ),
            ResourceModel(
                operationId = "products-create",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = createProductRequest(),
                response = createProductResponse()
            ),
            ResourceModel(
                operationId = "orders-list",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getOrdersListRequest(),
                response = getOrdersListResponse()
            ),
            ResourceModel(
                operationId = "orders-get-order",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getOrderRequest(),
                response = getOrderResponse()
            ),
            ResourceModel(
                operationId = "merchant-shop-attribute-get-list",
                privateResource = false,
                deprecated = false,
                tags = setOf("tag1", "tag2"),
                request = getShopAttributesRequest(),
                response = getShopAttributesResponse()
            )
        )
    }

    private fun createProductResponse(): ResponseModel {
        return ResponseModel(
            status = 200,
            contentType = "application/json",
            headers = listOf(),
            responseFields = listOf(),
            example = """{
  "_embedded" : {
    "availability" : {
      "availabilityState" : "IN_STOCK",
      "availableStock" : null,
      "stockThreshold" : null,
      "purchasable" : true,
      "_links" : {
        "self" : {
          "href" : "https://yourshop.api.urn/products/f7ea2f85-5be6-438a-87f8-a1094b85998c/availability"
        }
      }
    }
  },
  "sku" : "123456789-001",
  "salesPrice" : {
    "taxModel" : "NET",
    "currency" : "EUR",
    "amount" : 8.7,
    "derivedPrice" : {
      "taxModel" : "GROSS",
      "currency" : "EUR",
      "amount" : 9.57,
      "taxRate" : 0.1
    }
  },
  "listPrice" : {
    "taxModel" : "NET",
    "currency" : "EUR",
    "amount" : 10.95,
    "derivedPrice" : {
      "taxModel" : "GROSS",
      "currency" : "EUR",
      "amount" : 12.045,
      "taxRate" : 0.1
    }
  },
  "manufacturerPrice" : {
    "taxModel" : "NET",
    "currency" : "EUR",
    "amount" : 99.95,
    "derivedPrice" : {
      "taxModel" : "GROSS",
      "currency" : "EUR",
      "amount" : 109.945,
      "taxRate" : 0.1
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
        "amount" : 12.76,
        "taxRate" : 0.1
      }
    }
  },
  "shippingPeriod" : {
    "minDays" : 2,
    "maxDays" : 4,
    "displayUnit" : "WEEKS"
  },
  "name" : "Rioja Castillo de Puerto (2013)",
  "description" : "Spain\nRioja Tempranillo",
  "manufacturer" : "Grape Vineyard",
  "essentialFeatures" : "Dry. 12% alcohol. Best vine variety.",
  "_id" : "f7ea2f85-5be6-438a-87f8-a1094b85998c",
  "_links" : {
    "self" : {
      "href" : "https://yourshop.api.urn/products/f7ea2f85-5be6-438a-87f8-a1094b85998c"
    },
    "product" : {
      "href" : "https://yourshop.api.urn/products/f7ea2f85-5be6-438a-87f8-a1094b85998c"
    },
    "availability" : {
      "href" : "https://yourshop.api.urn/products/f7ea2f85-5be6-438a-87f8-a1094b85998c/availability"
    },
    "attributes" : {
      "href" : "https://yourshop.api.urn/products/f7ea2f85-5be6-438a-87f8-a1094b85998c/attributes"
    },
    "attachments" : {
      "href" : "https://yourshop.api.urn/products/f7ea2f85-5be6-438a-87f8-a1094b85998c/attachments"
    },
    "images" : {
      "href" : "https://yourshop.api.urn/products/f7ea2f85-5be6-438a-87f8-a1094b85998c/images"
    },
    "default-image" : {
      "href" : "https://yourshop.api.urn/products/f7ea2f85-5be6-438a-87f8-a1094b85998c/default-image"
    }
  }
}"""
        )
    }

    private fun createProductRequest(): RequestModel {
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
            example = """{
  "sku" : "123456789-001",
  "name" : "Rioja Castillo de Puerto (2013)",
  "description" : "Spain\nRioja Tempranillo",
  "manufacturer" : "Grape Vineyard",
  "essentialFeatures" : "Dry. 12% alcohol. Best vine variety.",
  "tags" : [ "Bestseller", "Red Wine", "Sale" ],
  "productIdentifiers" : [ {
    "type" : "EAN",
    "value" : "9780134308135"
  } ],
  "salesPrice" : {
    "taxModel" : "NET",
    "amount" : 8.7,
    "currency" : "EUR"
  },
  "listPrice" : {
    "taxModel" : "NET",
    "amount" : 10.95,
    "currency" : "EUR"
  },
  "manufacturerPrice" : {
    "taxModel" : "NET",
    "amount" : 99.95,
    "currency" : "EUR"
  },
  "onSale" : true,
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
    "price" : null
  },
  "shippingPeriod" : {
    "minDays" : 2,
    "maxDays" : 4,
    "displayUnit" : "WEEKS"
  }
}"""
        )
    }

    private fun getProductsListResponse(): ResponseModel {
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

    private fun getShopAttributesResponse(): ResponseModel {
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
            example = """{
  "_embedded" : {
    "attributes" : [ {
      "name" : "second-unknown-attribute-name",
      "value" : "correct-value",
      "public" : false,
      "readOnly" : false,
      "_links" : {
        "owner" : {
          "href" : "https://yourshop.api.urn/shop"
        },
        "self" : {
          "href" : "https://yourshop.api.urn/shop/attributes/second-unknown-attribute-name"
        }
      }
    } ]
  },
  "_links" : {
    "self" : {
      "href" : "https://yourshop.api.urn/shop/attributes?page=0&size=20"
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

    private fun getOrderResponse(): ResponseModel {
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
// 				FieldDescriptor(
// 					path = "_id",
// 					description = "The unique identifier of the order.",
// 					type = "STRING"
// 				),
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
            example = """{
   "_id":"6bb610d3-4411-42c2-bcfd-e834042f3b99",
   "createdAt":"2019-01-14T16:11:46.597",
   "cartId":"304ad2bd-d96d-4e5b-a8a4-e944f6d1cde0",
   "currency":"EUR",
   "taxModel":"GROSS",
   "taxable":false,
   "testOrder":false,
   "termsAndConditionsExplicitlyAccepted":false,
   "productLineItems":[
      {
         "_id":"21e323d4-85f7-4afe-8991-a204625a876d",
         "product":{
            "_id":"cd5c6222-cf91-4f28-8ad4-7dd6386864df",
            "sku":"EB001",
            "name":"Power & Vanilla Coconut Protein Bar (Pack of 5)",
            "essentialFeatures":"Preservative-free. No added sweeteners. Vegan.",
            "shippingWeight":190,
            "defaultImageDataUri":"http://your.image.hoster.com/randomimage.png",
            "_links":{
               "default-image-data":{
                  "href":"https://yourshop.api.urn/http:/your.image.hoster.com/randomimage.png"
               }
            }
         },
         "quantity":5,
         "initialQuantity":5,
         "unitPrice":{
            "taxModel":"GROSS",
            "currency":"EUR",
            "amount":9.99
         },
         "lineItemPrice":{
            "taxModel":"GROSS",
            "currency":"EUR",
            "amount":49.95
         },
         "initialLineItemPrice":{
            "taxModel":"GROSS",
            "currency":"EUR",
            "amount":49.95
         },
         "lineItemTax":{
            "taxClass":"REGULAR",
            "taxRate":0.19,
            "currency":"EUR",
            "amount":7.9743
         },
         "unshipped":{
            "quantity":5,
            "amount":null
         },
         "shipped":{
            "quantity":0,
            "amount":null
         },
         "pending":{
            "quantity":0,
            "amount":null
         },
         "returned":{
            "quantity":0,
            "amount":{
               "currency":"EUR",
               "amount":0
            }
         },
         "broken":{
            "quantity":0,
            "amount":{
               "currency":"EUR",
               "amount":0
            }
         },
         "canceled":{
            "quantity":0,
            "amount":{
               "currency":"EUR",
               "amount":0
            }
         }
      },
      {
         "_id":"44029a08-0ac4-4a40-b4d0-c12bca7256dc",
         "product":{
            "_id":"24a40aa8-ceda-40a2-8613-1171696eed4b",
            "sku":"vino020",
            "name":"Rioja Castillo de Puerto (2013)",
            "essentialFeatures":"Dry. 12% alcohol. Best vine variety.",
            "shippingWeight":100,
            "defaultImageDataUri":"http://your.image.hoster.com/randomimage.png",
            "_links":{
               "default-image-data":{
                  "href":"https://yourshop.api.urn/http:/your.image.hoster.com/randomimage.png"
               }
            }
         },
         "quantity":3,
         "initialQuantity":3,
         "unitPrice":{
            "taxModel":"GROSS",
            "currency":"EUR",
            "amount":2.5
         },
         "lineItemPrice":{
            "taxModel":"GROSS",
            "currency":"EUR",
            "amount":7.5
         },
         "initialLineItemPrice":{
            "taxModel":"GROSS",
            "currency":"EUR",
            "amount":7.5
         },
         "lineItemTax":{
            "taxClass":"REGULAR",
            "taxRate":0.19,
            "currency":"EUR",
            "amount":1.197
         },
         "unshipped":{
            "quantity":3,
            "amount":null
         },
         "shipped":{
            "quantity":0,
            "amount":null
         },
         "pending":{
            "quantity":0,
            "amount":null
         },
         "returned":{
            "quantity":0,
            "amount":{
               "currency":"EUR",
               "amount":0
            }
         },
         "broken":{
            "quantity":0,
            "amount":{
               "currency":"EUR",
               "amount":0
            }
         },
         "canceled":{
            "quantity":0,
            "amount":{
               "currency":"EUR",
               "amount":0
            }
         }
      }
   ],
   "shippingLineItem":{
      "lineItemPrice":{
         "taxModel":"GROSS",
         "currency":"EUR",
         "amount":19.99
      },
      "initialLineItemPrice":{
         "taxModel":"GROSS",
         "currency":"EUR",
         "amount":19.99
      },
      "taxClass":"REGULAR",
      "shippingMethod":{
         "name":"Standard Shipping 2",
         "description":"Standard Shipping",
         "freeShippingValue":{
            "currency":"EUR",
            "amount":400
         },
         "fixedPrice":{
            "taxModel":"GROSS",
            "currency":"EUR",
            "amount":19.99
         },
         "weightBasedPrice":null
      },
      "lineItemTaxes":[
         {
            "taxClass":"REGULAR",
            "taxRate":0.19,
            "currency":"EUR",
            "amount":3.192
         }
      ]
   },
   "paymentLineItem":{
      "lineItemPrice":{
         "taxModel":"GROSS",
         "currency":"EUR",
         "amount":0
      },
      "initialLineItemPrice":{
         "taxModel":"GROSS",
         "currency":"EUR",
         "amount":0
      },
      "taxClass":"REGULAR",
      "paymentMethod":{
         "name":"PayPal",
         "description":"Pay with PayPal",
         "discountOrFee":{
            "type":"ABSOLUTE",
            "absoluteValue":{
               "taxModel":"GROSS",
               "currency":"EUR",
               "amount":0
            },
            "percentageValue":null
         },
         "onlinePayment":false
      },
      "lineItemTaxes":[
         {
            "taxClass":"REGULAR",
            "taxRate":0.19,
            "currency":"EUR",
            "amount":0
         }
      ]
   },
   "subtotal":{
      "currency":"EUR",
      "amount":57.45
   },
   "grandTotal":{
      "currency":"EUR",
      "amount":77.44
   },
   "balanceDue":{
      "currency":"EUR",
      "amount":77.44
   },
   "netTotal":{
      "currency":"EUR",
      "amount":68.2687
   },
   "taxTotal":{
      "currency":"EUR",
      "amount":9.1713
   },
   "initialSubtotal":{
      "currency":"EUR",
      "amount":57.45
   },
   "initialGrandTotal":{
      "currency":"EUR",
      "amount":77.44
   },
   "initialBalanceDue":{
      "currency":"EUR",
      "amount":77.44
   },
   "initialNetTotal":{
      "currency":"EUR",
      "amount":68.2687
   },
   "initialTaxTotal":{
      "currency":"EUR",
      "amount":9.1713
   },
   "taxes":[
      {
         "taxClass":"REGULAR",
         "taxRate":0.19,
         "currency":"EUR",
         "amount":9.1713
      }
   ],
   "shippingAddress":{
      "salutation":"Mrs",
      "gender":"FEMALE",
      "company":"Astrid Alster GmbH",
      "title":null,
      "firstName":"Astrid",
      "middleName":"Agnes",
      "lastName":"Alster",
      "street":"Alsterwasserweg",
      "houseNumber":"2",
      "street2":"Erdgeschoss",
      "addressExtension":"Hinterhof",
      "postalCode":"20999",
      "dependentLocality":"Seevetal",
      "city":"Alsterwasser",
      "country":"DE",
      "state":"Hamburg",
      "email":"a.alsterh@example.com",
      "phone":"(800) 555-0102",
      "mobile":"(800) 555-0103",
      "doorCode":"456",
      "displayAddressLines":[
         "Astrid Alster GmbH",
         "Astrid Agnes Alster",
         "Alsterwasserweg 2",
         "Erdgeschoss",
         "Hinterhof",
         "Seevetal",
         "20999 Alsterwasser",
         "GERMANY"
      ]
   },
   "billingAddress":{
      "salutation":"Mrs",
      "gender":"FEMALE",
      "company":"Astrid Alster GmbH",
      "title":null,
      "firstName":"Astrid",
      "middleName":"Agnes",
      "lastName":"Alster",
      "street":"Alsterwasserweg",
      "houseNumber":"2",
      "street2":"Erdgeschoss",
      "addressExtension":"Hinterhof",
      "postalCode":"20999",
      "dependentLocality":"Seevetal",
      "city":"Alsterwasser",
      "country":"DE",
      "state":"Hamburg",
      "email":"a.alsterh@example.com",
      "phone":"(800) 555-0102",
      "mobile":"(800) 555-0103",
      "vatId":"DE123456789",
      "taxNumber":"HRE 987654/32123/864516",
      "birthDate":"1985-05-11",
      "displayAddressLines":[
         "Astrid Alster GmbH",
         "Astrid Agnes Alster",
         "Alsterwasserweg 2",
         "Erdgeschoss",
         "Hinterhof",
         "Seevetal",
         "20999 Alsterwasser",
         "GERMANY"
      ]
   },
   "paymentStatus":"PENDING",
   "shippingStatus":"UNSHIPPED",
   "canceled":false,
   "orderNumber":"10001",
   "customerComment":null,
   "orderNote":"not paid yet",
   "paymentNote":"Please transfer the money using the reference ABC123XYZ to the bank account IBAN!",
   "entryDate":"2019-01-14T16:11:46.597",
   "totalReceived":{
      "currency":"EUR",
      "amount":0
   },
   "totalRefunded":{
      "currency":"EUR",
      "amount":0
   },
   "openAmount":{
      "currency":"EUR",
      "amount":77.44
   },
   "marketingChannel":"Google shopping",
   "marketingSubchannel":"Some category",
   "salesChannel":"Storefront",
   "_links":{
      "self":{
         "href":"https://yourshop.api.urn/orders/6bb610d3-4411-42c2-bcfd-e834042f3b99"
      },
      "order":{
         "href":"https://yourshop.api.urn/orders/6bb610d3-4411-42c2-bcfd-e834042f3b99"
      },
      "order-note":{
         "href":"https://yourshop.api.urn/orders/6bb610d3-4411-42c2-bcfd-e834042f3b99/order-note"
      },
      "processes":{
         "href":"https://yourshop.api.urn/orders/6bb610d3-4411-42c2-bcfd-e834042f3b99/processes"
      },
      "events":{
         "href":"https://yourshop.api.urn/orders/6bb610d3-4411-42c2-bcfd-e834042f3b99/events"
      },
      "cancel":{
         "href":"https://yourshop.api.urn/orders/6bb610d3-4411-42c2-bcfd-e834042f3b99/cancel"
      },
      "create-invoice":{
         "href":"https://yourshop.api.urn/orders/6bb610d3-4411-42c2-bcfd-e834042f3b99/create-invoice"
      },
      "right-of-withdrawal-pdf":{
         "href":"https://yourshop.api.urn/pdf-storage/attachments/some.pdf?hash=23213"
      },
      "terms-and-conditions-pdf":{
         "href":"https://yourshop.api.urn/pdf-storage/attachments/some.pdf?hash=23213"
      }
   }
} """
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

    private fun getOrdersListResponse(): ResponseModel {
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
            example = """{
  "_embedded" : {
    "orders" : [ {
      "_id" : "b84debed-95b8-483c-a0f1-978b0894b804",
      "createdAt" : "2019-01-14T16:11:46.991",
      "testOrder" : false,
      "termsAndConditionsExplicitlyAccepted" : false,
      "paymentLineItem" : {
        "taxClass" : "REGULAR",
        "paymentMethod" : {
          "name" : "PayPal",
          "description" : "Pay with PayPal",
          "discountOrFee" : {
            "type" : "ABSOLUTE",
            "absoluteValue" : {
              "taxModel" : "GROSS",
              "currency" : "EUR",
              "amount" : 0
            },
            "percentageValue" : null
          },
          "onlinePayment" : false
        },
        "lineItemTaxes" : [ {
          "taxClass" : "REGULAR",
          "taxRate" : 0.19,
          "currency" : "EUR",
          "amount" : 0
        } ]
      },
      "initialNetTotal" : {
        "currency" : "EUR",
        "amount" : 68.2687
      },
      "initialTaxTotal" : {
        "currency" : "EUR",
        "amount" : 9.1713
      },
      "taxes" : [ {
        "taxClass" : "REGULAR",
        "taxRate" : 0.19,
        "currency" : "EUR",
        "amount" : 9.1713
      } ],
      "shippingAddress" : {
        "salutation" : "Mrs",
        "houseNumber" : "2",
        "street2" : "Erdgeschoss",
        "addressExtension" : "Hinterhof",
        "postalCode" : "20999",
        "dependentLocality" : "Seevetal",
        "doorCode" : "456",
        "displayAddressLines" : [ "Astrid Alster GmbH", "Astrid Agnes Alster", "Alsterwasserweg 2", "Erdgeschoss", "Hinterhof", "Seevetal", "20999 Alsterwasser", "GERMANY" ]
      },
      "paymentStatus" : "PENDING",
      "shippingStatus" : "UNSHIPPED",
      "paymentNote" : "Please transfer the money using the reference ABC123XYZ to the bank account IBAN!",
      "entryDate" : "2019-01-14T16:11:46.991",
      "totalReceived" : {
        "currency" : "EUR",
        "amount" : 0
      },
      "totalRefunded" : {
        "currency" : "EUR",
        "amount" : 0
      },
      "openAmount" : {
        "currency" : "EUR",
        "amount" : 77.44
      },
      "marketingChannel" : "Google shopping",
      "marketingSubchannel" : "Some category",
      "salesChannel" : "Storefront",
      "_links" : {
        "self" : {
          "href" : "https://yourshop.api.urn/orders/b84debed-95b8-483c-a0f1-978b0894b804"
        },
        "order" : {
          "href" : "https://yourshop.api.urn/orders/b84debed-95b8-483c-a0f1-978b0894b804"
        },
        "create-invoice" : {
          "href" : "https://yourshop.api.urn/orders/b84debed-95b8-483c-a0f1-978b0894b804/create-invoice"
        },
        "right-of-withdrawal-pdf" : {
          "href" : "https://yourshop.api.urn/pdf-storage/attachments/some.pdf?hash=23213"
        },
        "terms-and-conditions-pdf" : {
          "href" : "https://yourshop.api.urn/pdf-storage/attachments/some.pdf?hash=23213"
        }
      }
    } ]
  },
  "_links" : {
    "self" : {
      "href" : "https://yourshop.api.urn/orders?page=0&size=20&sort=createdAt,desc"
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

    private fun getProductPayloadExample(): String {
        return """{
   "_embedded":{
      "availability":{
         "availabilityState":"IN_STOCK",
         "availableStock":null,
         "stockThreshold":null,
         "purchasable":true,
         "_links":{
            "self":{
               "href":"https://yourshop.api.urn/products/b774960e-bcee-495f-9148-c8b35a47d23f/availability"
            }
         }
      }
   },
   "sku":"vino020",
   "salesPrice":{
      "taxModel":"NET",
      "currency":"EUR",
      "amount":8.7,
      "derivedPrice":{
         "taxModel":"GROSS",
         "currency":"EUR",
         "amount":54.989,
         "taxRate":0.011
      }
   },
   "listPrice":{
      "taxModel":"NET",
      "currency":"EUR",
      "amount":10.95,
      "derivedPrice":{
         "taxModel":"GROSS",
         "currency":"EUR",
         "amount":76.989,
         "taxRate":0.011
      }
   },
   "manufacturerPrice":{
      "taxModel":"NET",
      "currency":"EUR",
      "amount":99.95,
      "derivedPrice":{
         "taxModel":"GROSS",
         "currency":"EUR",
         "amount":118.9405,
         "taxRate":0.19
      }
   },
   "onSale":true,
   "tags":[
      "Bestseller",
      "Red Wine",
      "Sale"
   ],
   "productIdentifiers":[
      {
         "type":"EAN",
         "value":"9780134308135"
      }
   ],
   "visible":true,
   "taxClass":"REGULAR",
   "shippingWeight":100,
   "maxOrderQuantity":6,
   "shippingDimension":{
      "length":1500,
      "width":1000,
      "height":2000
   },
   "refPrice":{
      "refQuantity":1,
      "unit":"LITER",
      "quantity":0.75,
      "price":{
         "taxModel":"NET",
         "currency":"EUR",
         "amount":11.6,
         "derivedPrice":{
            "taxModel":"GROSS",
            "currency":"EUR",
            "amount":73.319,
            "taxRate":0.011
         }
      }
   },
   "shippingPeriod":{
      "minDays":2,
      "maxDays":null,
      "displayUnit":"WEEKS"
   },
   "name":"Rioja Castillo de Puerto (2013)",
   "description":"SpainRioja Tempranillo",
   "manufacturer":"Grape Vineyard",
   "essentialFeatures":"Dry. 12% alcohol. Best vine variety.",
   "_id":"b774960e-bcee-495f-9148-c8b35a47d23f",
   "_links":{
      "self":{
         "href":"https://yourshop.api.urn/products/b774960e-bcee-495f-9148-c8b35a47d23f"
      },
      "product":{
         "href":"https://yourshop.api.urn/products/b774960e-bcee-495f-9148-c8b35a47d23f"
      },
      "availability":{
         "href":"https://yourshop.api.urn/products/b774960e-bcee-495f-9148-c8b35a47d23f/availability"
      },
      "attributes":{
         "href":"https://yourshop.api.urn/products/b774960e-bcee-495f-9148-c8b35a47d23f/attributes"
      },
      "attachments":{
         "href":"https://yourshop.api.urn/products/b774960e-bcee-495f-9148-c8b35a47d23f/attachments"
      },
      "images":{
         "href":"https://yourshop.api.urn/products/b774960e-bcee-495f-9148-c8b35a47d23f/images"
      },
      "amazon":{
         "href":"http://amazon/amazon-products/b774960e-bcee-495f-9148-c8b35a47d23f"
      },
      "google-shopping":{
         "href":"http://google-shopping/google-products/b774960e-bcee-495f-9148-c8b35a47d23f"
      },
      "default-image":{
         "href":"https://yourshop.api.urn/products/b774960e-bcee-495f-9148-c8b35a47d23f/default-image"
      },
      "default-image-data":{
         "href":"https://yourshop.api.urn/api/core-storage/images/photostore-2.JPG?hash=8a627f655c68f56dfbbf217ab7d5563281225998{&width,height,upscale}",
         "templated":true
      },
      "default-image-metadata":{
         "href":"https://yourshop.api.urn/api/core-storage/images/photostore-2.JPG?hash=8a627f655c68f56dfbbf217ab7d5563281225998&download=no"
      }
   }
}"""
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
