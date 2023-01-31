# Spring REST Docs API specification Integration

[![oss lifecycle](https://img.shields.io/badge/oss_lifecycle-maintenance-yellow.svg)](https://github.com/ePages-de/restdocs-api-spec/issues/204)
![](https://img.shields.io/github/license/ePages-de/restdocs-openapi.svg)
[![Build Status](https://app.travis-ci.com/ePages-de/restdocs-api-spec.svg?branch=master)](https://app.travis-ci.com/github/ePages-de/restdocs-api-spec)
[![Maven Central](https://img.shields.io/maven-central/v/com.epages/restdocs-api-spec)](https://search.maven.org/artifact/com.epages/restdocs-api-spec)
[![Coverage Status](https://coveralls.io/repos/github/ePages-de/restdocs-api-spec/badge.svg?branch=master)](https://coveralls.io/github/ePages-de/restdocs-api-spec?branch=master)
[![Gitter](https://img.shields.io/gitter/room/nwjs/nw.js.svg)](https://gitter.im/restdocs-api-spec/Lobby)

This is an extension that adds API specifications as an output format to [Spring REST Docs](https://projects.spring.io/spring-restdocs/).
It currently supports:
- [OpenAPI 2.0](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md) in `json` and `yaml`
- [OpenAPI 3.0.1](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md) in `json` and `yaml`
- [Postman Collections 2.1.0](https://schema.getpostman.com/json/collection/v2.1.0/docs/index.html)

Please note that this extension was developed for JSON-based APIs.
We do not expect this extension to build usable API specification for non-JSON request or response bodies.

## Motivation

[Spring REST Docs](https://projects.spring.io/spring-restdocs/) is a great tool to produce documentation for your RESTful services that is accurate and readable.

We especially like its test-driven approach and this is the main reason why we chose it.

It offers support for AsciiDoc and Markdown. This is great for generating simple HTML-based documentation.
But both are markup languages and thus it is hard to get any further than statically generated HTML.

API specifications like OpenAPI are a lot more flexible.
With e.g. OpenAPI you get a machine-readable description of your API. There is a rich ecosystem around it that contains tools to:
- generate a HTML representation of your API - [ReDoc](https://github.com/Rebilly/ReDoc)
- generate an interactive API reference - e.g. using services like [stoplight.io](https://stoplight.io) or [readme.io](https://readme.io)

Also, API specifications like OpenAPI are supported by many REST clients like [Postman](https://www.getpostman.com) and [Paw](https://paw.cloud).
Thus having an API specification for a REST API is a great plus when starting to work with it.

The most common use case to generate an OpenAPI specification is code introspection and adding documentation related annotations to your code.
We do not like enriching our production code with this information and clutter it with even more annotations.
We agree with Spring REST Docs that the test-driven way to produce accurate API documentation is the way to go.
This is why we came up with this project.


- [Motivation](#motivation)
- [Getting started](#getting-started)
    - [Version compatibility](#version-compatibility)
    - [Project structure](#project-structure)
    - [Build configuration](#build-configuration)
        - [Gradle](#gradle)
        - [Maven](#maven)
    - [Usage with Spring REST Docs](#usage-with-spring-rest-docs)
    - [Documenting Bean Validation constraints](#documenting-bean-validation-constraints)
    - [Migrate existing Spring REST Docs tests](#migrate-existing-spring-rest-docs-tests)
        - [MockMvc based tests](#mockmvc-based-tests)
        - [REST Assured based tests](#rest-assured-based-tests)
        - [WebTestClient based tests](#webtestclient-based-tests)
    - [Security Definitions in OpenAPI](#security-definitions-in-openapi)
    - [Running the gradle plugin](#running-the-gradle-plugin)
        - [OpenAPI 2.0](#openapi-20)
        - [OpenAPI 3.0.1](#openapi-301)
        - [Postman](#postman)
    - [Gradle plugin configuration](#gradle-plugin-configuration)
        - [Common configuration for all formats](#common-configuration-for-all-formats)
        - [Common OpenAPI configuration](#common-openapi-configuration)
        - [OpenAPI 2.0](#openapi-20-1)
        - [OpenAPI 3.0.1](#openapi-301-1)
        - [Postman](#postman-1)
- [Generate an HTML-based API reference from OpenAPI](#generate-an-html-based-api-reference-from-openapi)
- [RAML](#raml)

## Getting started

### Version compatibility

Spring Boot and Spring REST Docs 3.0.0 introduced [breaking chances to how request parameters are documented: `RequestParameterSnippet` was split into `QueryParameterSnippet` and `FormParameterSnippet`.](https://github.com/spring-projects/spring-restdocs/issues/832)

|Spring Boot version | restdocs-api-spec version|
|---|---|
|3.x|0.17.1 or later|
|2.x|0.16.4|

### Project structure

The project consists of the following main components:

- [restdocs-api-spec](restdocs-api-spec) - contains the actual Spring REST Docs extension.
This is most importantly the [ResourceDocumentation](restdocs-api-spec/src/main/kotlin/com/epages/restdocs/apispec/ResourceDocumentation.kt) which is the entrypoint to use the extension in your tests.
The [ResourceSnippet](restdocs-api-spec/src/main/kotlin/com/epages/restdocs/apispec/ResourceSnippet.kt) is the snippet used to produce a json file `resource.json` containing all the details about the documented resource.
- [restdocs-api-spec-mockmvc](restdocs-api-spec-mockmvc) - contains a wrapper for `MockMvcRestDocumentation` for easier migration to `restdocs-api-spec` from MockMvc tests that use plain `spring-rest-docs-mockmvc`.
- [restdocs-api-spec-restassured](restdocs-api-spec-restassured) - contains a wrapper for `RestAssuredRestDocumentation` for easier migration to `restdocs-api-spec` from [Rest Assured](http://rest-assured.io) tests that use plain `spring-rest-docs-restassured`.
- [restdocs-api-spec-gradle-plugin](restdocs-api-spec-gradle-plugin) - adds a gradle plugin that aggregates the `resource.json` files produced  by `ResourceSnippet` into an API specification file for the whole project.

### Build configuration

#### Gradle

1. Add the plugin
    * Using the [plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block):
        ```groovy
        plugins {
            id 'com.epages.restdocs-api-spec' version '0.16.0'
        }
        ```
        Examples with Kotlin are also available [here](https://plugins.gradle.org/plugin/com.epages.restdocs-api-spec)
    * __OR__ Using [legacy plugin application](https://docs.gradle.org/current/userguide/plugins.html#sec:old_plugin_application):
        * *1.1* Use of `buildscript` requires you to add the `https://plugins.gradle.org/m2/` repository.
        * *1.2* add the dependency to `restdocs-api-spec-gradle-plugin`
        * *1.3* apply `restdocs-api-spec-gradle-plugin`
        ```groovy
        buildscript {
          repositories {
            maven {
              url "https://plugins.gradle.org/m2/" //1.1
            }
          }
          dependencies {
            classpath "com.epages:restdocs-api-spec-gradle-plugin:0.16.0" //1.2
          }
        }

        apply plugin: 'com.epages.restdocs-api-spec' //1.3

        ```
2. Add required dependencies to your tests
    * *2.1* add the `mavenCentral` repository used to resolve the `com.epages:restdocs-api-spec` module of the project.
    * *2.2* add the actual `restdocs-api-spec-mockmvc` dependency to the test scope. Use `restdocs-api-spec-restassured` if you use `RestAssured` instead of `MockMvc`.
    * *2.3* add configuration options for restdocs-api-spec-gradle-plugin`. See [Gradle plugin configuration](#gradle-plugin-configuration)
    ```groovy

    repositories { //2.1
        mavenCentral()
    }

    dependencies {
        //..
        testImplementation('com.epages:restdocs-api-spec-mockmvc:0.16.0') //2.2
    }

    openapi { //2.3
        host = 'localhost:8080'
        basePath = '/api'
        title = 'My API'
        description = 'My API description'
        tagDescriptionsPropertiesFile = 'src/docs/tag-descriptions.yaml'
        version = '1.0.0'
        format = 'json'
    }

    openapi3 {
        server = 'https://localhost:8080'
        title = 'My API'
        description = 'My API description'
        tagDescriptionsPropertiesFile = 'src/docs/tag-descriptions.yaml'
        version = '0.1.0'
        format = 'yaml'
    }

    postman {
        title = 'My API'
        version = '0.1.0'
        baseUrl = 'https://localhost:8080'
    }
    ```

See the [build.gradle](samples/restdocs-api-spec-sample/build.gradle) for the setup used in the sample project.

#### Maven

The root project does not provide a maven plugin.
But you can find a plugin that works with `restdocs-api-spec` at [BerkleyTechnologyServices/restdocs-spec](https://github.com/BerkleyTechnologyServices/restdocs-spec).

### Usage with Spring REST Docs

The class [ResourceDocumentation](restdocs-api-spec/src/main/kotlin/com/epages/restdocs/apispec/ResourceDocumentation.kt) contains the entry point for using the [ResourceSnippet](restdocs-api-spec/src/main/kotlin/com/epages/restdocs/apispec/ResourceSnippet.kt).

The most basic form does not take any parameters:

```java
mockMvc
  .perform(post("/carts"))
  .andDo(document("carts-create", resource("Create a cart")));
```

This test will produce the `resource.json` file in the snippets directory.
This file just contains all the information that we can collect about the resource.
The format of this file is not specific to an API specification.

```json
{
  "operationId" : "carts-create",
  "summary" : "Create a cart",
  "description" : "Create a cart",
  "privateResource" : false,
  "deprecated" : false,
  "request" : {
    "path" : "/carts",
    "method" : "POST",
    "contentType" : null,
    "headers" : [ ],
    "pathParameters" : [ ],
    "requestParameters" : [ ],
    "requestFields" : [ ],
    "example" : null,
    "securityRequirements" : null
  },
  "response" : {
    "status" : 201,
    "contentType" : "application/hal+json",
    "headers" : [ ],
    "responseFields" : [ ],
    "example" : "{\n  \"total\" : 0,\n  \"products\" : [ ],\n  \"_links\" : {\n    \"self\" : {\n      \"href\" : \"http://localhost:8080/carts/4\"\n    },\n    \"order\" : {\n      \"href\" : \"http://localhost:8080/carts/4/order\"\n    }\n  }\n}"
  }
}
```

Just like with Spring REST Docs we can also describe request fields, response fields, path variables, parameters, headers, and links.
Furthermore you can add a text description and a summary for your resource.
The extension also discovers `JWT` tokens in the `Authorization` header and will document the required scopes from it. Also basic auth headers are discovered and documented.

The following example uses `ResourceSnippetParameters` to document response fields, path parameters, and links.
We paid close attention to keep the API as similar as possible to what you already know from Spring REST Docs.
`fieldWithPath` and `linkWithRel` are actually still the static methods you would use in your using Spring REST Docs test.

```java
mockMvc.perform(get("/carts/{id}", cartId)
  .accept(HAL_JSON))
  .andExpect(status().isOk())
  .andDo(document("cart-get",
    resource(ResourceSnippetParameters.builder()
      .description("Get a cart by id")
      .pathParameters(
        parameterWithName("id").description("the cart id"))
      .responseFields(
        fieldWithPath("total").description("Total amount of the cart."),
        fieldWithPath("products").description("The product line item of the cart."),
        subsectionWithPath("products[]._links.product").description("Link to the product."),
        fieldWithPath("products[].quantity").description("The quantity of the line item."),
        subsectionWithPath("products[].product").description("The product the line item relates to."),
        subsectionWithPath("_links").description("Links section."))
      .links(
        linkWithRel("self").ignored(),
        linkWithRel("order").description("Link to order the cart."))
    .build())));
```
Please see the [CartIntegrationTest](samples/restdocs-api-spec-sample/src/test/java/com/epages/restdocs/apispec/sample/CartIntegrationTest.java) in the sample application for a detailed example.

**:warning: Use `template URIs` to refer to path variables in your request**

Note how we use the `urlTemplate` to build the request with [`RestDocumentationRequestBuilders`](https://docs.spring.io/spring-restdocs/docs/current/api/org/springframework/restdocs/mockmvc/RestDocumentationRequestBuilders.html#get-java.lang.String-java.lang.Object...-).
This makes the `urlTemplate` available in the snippet and we can depend on the non-expanded template when generating the OpenAPI file.

 ```java
mockMvc.perform(get("/carts/{id}", cartId)
 ```

### Documenting Bean Validation constraints

Similar to the way Spring REST Docs allows to use [bean validation constraints](https://docs.spring.io/spring-restdocs/docs/current/reference/html5/#documenting-your-api-constraints) to enhance your documentation, you can also use the constraints from your model classes to let `restdocs-api-spec` enrich the generated JsonSchemas.
`restdocs-api-spec` provides the class [com.epages.restdocs.apispec.ConstrainedFields](restdocs-api-spec/src/main/kotlin/com/epages/restdocs/apispec/ConstrainedFields.kt) to generate `FieldDescriptor`s that contain information about the constraints on this field.

Currently the following constraints are considered when generating JsonSchema from `FieldDescriptor`s that have been created via `com.epages.restdocs.apispec.ConstrainedFields`
- `NotNull`, `NotEmpty`, and `NotBlank` annotated fields become required fields in the JsonSchema
- for String fields annotated with `NotEmpty`, and `NotBlank` the `minLength` constraint in JsonSchema is set to 1
- for String fields annotated with `Length` the `minLength` and `maxLength` constraints in JsonSchema are set to the value of the corresponding attribute of the annotation
- for String fields annotated with `Pattern`, the pattern constraint is propagated to JsonSchema
- for Number fields annotated with `Min`, the `minimum` constraint is propagated to JsonSchema
- for Number fields annotated with `Max`, the `maximum` constraint is propagated to JsonSchema
- for Number fields annotated with `Size` the `minimum` and `maximum` constraints in JsonSchema are set to the value of the corresponding attribute of the annotation

If you already have your own `ConstraintFields` implementation you can also add the logic from `com.epages.restdocs.apispec.ConstrainedFields` to your own class.
Here it is important to add the constraints under the key `validationConstraints` into the attributes map if the `FieldDescriptor`.

### Migrate existing Spring REST Docs tests

#### MockMvc based tests

For convenience when applying `restdocs-api-spec` to an existing project that uses Spring REST Docs, we introduced [com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper](restdocs-api-spec/src/main/kotlin/com/epages/restdocs/apispec/MockMvcRestDocumentationWrapper.kt).

In your tests you can just replace calls to `MockMvcRestDocumentation.document` with the corresponding variant of `MockMvcRestDocumentationWrapper.document`.

`MockMvcRestDocumentationWrapper.document` will execute the specified snippets and also add a `ResourceSnippet` equipped with the input from your snippets.

Here is an example:

```java
resultActions
  .andDo(
    MockMvcRestDocumentationWrapper.document(operationName,
      requestFields(fieldDescriptors().getFieldDescriptors()),
      responseFields(
        fieldWithPath("comment").description("the comment"),
        fieldWithPath("flag").description("the flag"),
        fieldWithPath("count").description("the count"),
        fieldWithPath("id").description("id"),
        fieldWithPath("_links").ignored()
      ),
      links(linkWithRel("self").description("some"))
  )
);
```

This will do exactly what `MockMvcRestDocumentation.document` does.
Additionally it will add a `ResourceSnippet` with the descriptors you provided in the `RequestFieldsSnippet`, `ResponseFieldsSnippet`, and `LinksSnippet`.

#### REST Assured based tests

Also for REST Assured we offer a convenience wrapper similar to `MockMvcRestDocumentationWrapper`.
The usage for REST Assured is also similar to MockMVC, except that [com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper](restdocs-api-spec/src/main/kotlin/com/epages/restdocs/apispec/RestAssuredRestDocumentationWrapper.kt) is used instead of [com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper](restdocs-api-spec/src/main/kotlin/com/epages/restdocs/apispec/MockMvcRestDocumentationWrapper.kt).

To use the `RestAssuredRestDocumentationWrapper`, you have to add a dependency to [restdocs-api-spec-restassured](restdocs-api-spec-restassured) to your build.
```java
RestAssured.given(this.spec)
        .filter(RestAssuredRestDocumentationWrapper.document("{method-name}",
                "The API description",
                requestParameters(
                        parameterWithName("param").description("the param")
                ),
                responseFields(
                        fieldWithPath("doc.timestamp").description("Creation timestamp")
                )
        ))
        .when()
        .queryParam("param", "foo")
        .get("/restAssuredExample")
        .then()
        .statusCode(200);
```

#### WebTestClient based tests

We also offer a convenience wrapper for `WebTestClient` which works similar to `MockMvcRestDocumentationWrapper`.
The usage is similar to MockMVC, except that [com.epages.restdocs.apispec.WebTestClientRestDocumentationWrapper](restdocs-api-spec-webtestclient/src/main/kotlin/com/epages/restdocs/apispec/WebTestClientRestDocumentationWrapper.kt) is used instead of [com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper](restdocs-api-spec/src/main/kotlin/com/epages/restdocs/apispec/MockMvcRestDocumentationWrapper.kt).

To use the `WebTestClientRestDocumentationWrapper`, you will have to add a dependency to [restdocs-api-spec-webtestclient](restdocs-api-spec-webtestclient) to your build.

```
webTestClient.get().uri("/sample/{id}?queryParam=something", "1024").exchange()
    .expectStatus().isOk().expectBody()
    .consumeWith(
        WebTestClientRestDocumentationWrapper
            .document("sample",
                RequestDocumentation.pathParameters(
                    parameterWithName("id").description(
                        "description of the path parameter")
                ),
                RequestDocumentation.requestParameters(
                    parameterWithName("queryParam").description(
                        "description of the query parameter")
                ),
                HeaderDocumentation.responseHeaders(
                    headerWithName(HttpHeaders.CONTENT_TYPE)
                        .description(MediaType.APPLICATION_JSON_UTF8_VALUE)
                ),
                responseFields(
                    PayloadDocumentation.fieldWithPath("field1").type(JsonFieldType.STRING)
                        .description("description of field1"),
                    PayloadDocumentation.fieldWithPath("field2").type(JsonFieldType.STRING)
                        .description("description of field2")
                )
            )
    );
```

### Security Definitions in OpenAPI

The project has limited support for describing security requirements of an API.
Currently we only support Oauth2 with [JWT](https://jwt.io/) tokens and HTTP Basic Auth.

`restdocs-api-spec` inspects the `AUTHORIZATION` header of a request for a `JWT` token.
Also the a HTTP basic authorization header is discovered and documented.
If such a token is found the scopes are extracted and added to the `resource.json` snippet.

The `restdocs-api-spec-gradle-plugin` will consider this information if the `oauth2SecuritySchemeDefinition` configuration option is set (see [Gradle plugin configuration](#gradle-plugin-configuration)).
This will result in a top-level `securityDefinitions` in the OpenAPI definition.
Additionally the required scopes will be added in the `security` section of an `operation`.

### Running the gradle plugin

`restdocs-api-spec-gradle-plugin` is responsible for picking up the generated `resource.json` files and aggregate them into an API specification.

#### OpenAPI 2.0
In order to generate an OpenAPI 2.0 specification we use the `openapi` task:

```
./gradlew openapi
```

#### OpenAPI 3.0.1
In order to generate an OpenAPI 3.0.1 specification we use the `openapi3` task:

```
./gradlew openapi3
```

For our [sample project](samples/restdocs-api-spec-sample) this creates a `openapi3.yaml` file in the output directory (`build/api-spec`).

### Postman

In order to generate a [Postman collection](https://www.getpostman.com/collection) we use the `postman` task:

```
./gradlew postman
```

For our [sample project](samples/restdocs-api-spec-sample) this creates a `postman-collection.json` file in the output directory (`build/api-spec`).

### Gradle plugin configuration

#### Common configuration for all formats

Name | Description | Default value
---- | ----------- | -------------
separatePublicApi | Should the plugin generate additional API specification files which do **not** contain the resources marked as private | `false`
outputDirectory | The output directory for the API specification files | `build/api-spec`
snippetsDirectory | The directory Spring REST Docs generated the snippets to | `build/generated-snippets`

#### Common OpenAPI configuration

The `restdocs-api-spec-gradle-plugin` takes the following configuration options for OpenAPI 2.0 and OpenAPI 3.0.1 - all are optional.

Name | Description | Default value
---- | ----------- | -------------
title | The title of the application. Used for the `title` attribute in the [Info object](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#info-object) | `API documentation`
description | A description of the application. Used for the `description` attribute in the [Info object](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#info-object) | empty
version | The version of the api. Used for the `version` attribute in the [Info object](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#info-object) | project version
format | The format of the output OpenAPI file - supported values are `json` and `yaml` | `json`
tagDescriptionsPropertiesFile | A yaml file mapping tag names to descriptions. These are populated into the top level ` [Tags attribute](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#tag-object) | no default - if not provided no tags created.
oauth2SecuritySchemeDefinition | Closure containing information to generate the [securityDefinitions](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#securityDefinitionsObject) object in the `OpenAPI` specification. | empty
oauth2SecuritySchemeDefinition.flows | The Oauth2 flows the API supports. Use valid values from the [securityDefinitions](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#securityDefinitionsObject) specification. | no default - required if `oauth2SecuritySchemeDefinition` is set.
oauth2SecuritySchemeDefinition.tokenUrl | The Oauth2 tokenUrl | no default - required for the flows `password`, `application`, `accessCode`.
oauth2SecuritySchemeDefinition.authorizationUrl | The Oauth2 authorizationUrl | no default - required for the flows `implicit`, `accessCode`.
oauth2SecuritySchemeDefinition.scopeDescriptionsPropertiesFile | A yaml file mapping scope names to descriptions. These are used in the `securityDefinitions` as the [scope description](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#scopesObject) | no default - if not provided the scope descriptions default to `No description`.


The `scopeDescriptionsPropertiesFile` is supposed to be a yaml file:
```yaml
scope-name: A description
```
#### OpenAPI 2.0

The `restdocs-api-spec-gradle-plugin` takes the following configuration options for OpenAPI 2.0 - all are optional.

Name | Description | Default value
---- | ----------- | -------------
host | The host serving the API - corresponds to the attribute with the same name in the [OpenAPI root object](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#swagger-object)| `localhost`
basePath | The base path on which the API is served - corresponds to the attribute with the same name in the [OpenAPI root object](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#swagger-object) | null
schemes | The supported transfer protocols of the API - corresponds to the attribute with the same name in the [OpenAPI root object](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#swagger-object) | `['http'"]`
outputFileNamePrefix | The file name prefix of the output file. | `openapi` which results in e.g. `openapi.json` for the format `json`

Example configuration closure:
```
openapi {
    basePath = "/api"
    host = "api-shop.beyondshop.cloud"
    schemes = ["https"]
    format = "yaml"
    title = 'Beyond REST API'
    version = "1.0.0"
    separatePublicApi = true
    snippetsDirectory="src/docs/asciidoc/generated-snippets/"
    outputDirectory="openapi/"
    oauth2SecuritySchemeDefinition = {
        flows = ['accessCode', 'application']
        tokenUrl = 'https://api-shop.beyondshop.cloud/api/oauth/token'
        authorizationUrl = 'https://api-shop.beyondshop.cloud/api/auth/oauth-ext/authorize'
        scopeDescriptionsPropertiesFile = "src/docs/scope-descriptions.yaml"
    }
}
```

#### OpenAPI 3.0.1

The `restdocs-api-spec-gradle-plugin` takes the following configuration options for OpenAPI 3.0.1 - all are optional.

Name | Description | Default value
---- | ----------- | -------------
outputFileNamePrefix | The file name prefix of the output file. | `openapi3` which results in e.g. `openapi3.json` for the format `json`
servers | Specifies the servers the API is available from. Use this property to specify multiple server definitions. See example below.  | `http://localhost`
server | Specifies the servers the API is available from. Use this property to specify just a single server definition. See example below | `http://localhost`

Example configuration closure:
```
openapi3 {
    servers = [ { url = "http://some.api" } ]
    title = 'My API title'
    version = '1.0.1'
    format = 'yaml'
    separatePublicApi = true
    outputFileNamePrefix = 'my-api-spec'
    oauth2SecuritySchemeDefinition = {
        flows = ['authorizationCode']
        tokenUrl = 'http://example.com/token'
        authorizationUrl = 'http://example.com/authorize'
        scopeDescriptionsPropertiesFile = "scopeDescriptions.yaml"
    }
}
```

Example `build.gradle.kts` configuration closure (by [axkb](https://github.com/axkb), [#112](https://github.com/ePages-de/restdocs-api-spec/issues/112)):
```
configure<com.epages.restdocs.apispec.gradle.OpenApi3Extension> {
    setServer("http://$apiHost:$apiPort")
    title = "Your title"
    description = "Your description"
    version = "0.1.0"
    format = "json"
    tagDescriptionsPropertiesFile = "src/test/resources/tags.yaml"
}
```

The `servers` and `server` property can also contain variables. Is this case the` property can be specified like this:

This configuration follows the same semantics as the ['Servers Object'](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#server-object) in the OpenAPI specification

```
servers = [ {
    url = 'https://{host}/api'
    variables = [
        host: [
            default: 'api-shop.beyondshop.cloud/api',
            description: 'The hostname of your beyond shop',
            enum: ['api-shop', 'oz']
        ]
    ]
} ]
```

The same structure applies to `server`.
A single server can also be specified using a plain string:

```
server = 'http://some.api/api'
```

#### Postman

The `restdocs-api-spec-gradle-plugin` takes the following configuration options for Postman collections - all are optional.

Name | Description | Default value
---- | ----------- | -------------
title | The title of the application. Used for the `name` attribute of the `Information` object of the collection | `API documentation`
version | The version of the api. Used for the `version` attribute in the `Information` object | project version if specified - otherwise `1.0.0`
baseUrl | The baseUrl of the application. e.g. `https://myapi.example.com:8080/api` | `http://localhost`

Example configuration closure:
```
postman {
    title = 'Beyond REST API'
    version = '1.0.1'
    baseUrl = 'https://api-shop.beyondshop.cloud/api'
    separatePublicApi = true
    outputFileNamePrefix = 'my-postman-collection'
}
```

## Generate an HTML-based API reference from OpenAPI

We can use [redoc](https://github.com/Rebilly/ReDoc) to generate an HTML API reference from our OpenAPI specification.

The [redoc-cli](https://www.npmjs.com/package/redoc-cli) can be used to bundle (and serve) this API reference:
```
# Install redoc-cli
npm install -g redoc-cli

# Bundle the documentation into a zero-dependency HTML-file
redoc-cli bundle build/api-spec/openapi.json

# Bundle and serve
redoc-cli serve build/api-spec/openapi.json
```

## RAML

This project supersedes [restdocs-raml](https://github.com/ePages-de/restdocs-raml).
So if you are coming from `restdocs-raml` you might want to switch to `restdocs-api-spec`.

The API of both projects is fairly similar and it is easy to migrate.

We plan to support RAML in the future.
In the meantime you can use one of several ways to convert an OpenAPI specification to RAML.
There are converters around that can help you to achieve this conversion.

- [oas-raml-converter](https://github.com/mulesoft/oas-raml-converter) - an npm project that provides a CLI to convert between OpenAPI and RAML - it also provides an [online converter](https://mulesoft.github.io/oas-raml-converter/)
- [api-matic](https://apimatic.io/transformer) - an online converter capable of converting between many api specifications

In the [sample project](samples/restdocs-api-spec-sample) you find a build configuration that uses the [oas-raml-converter-docker](https://hub.docker.com/r/zaddo/oas-raml-converter-docker/) docker image and the [gradle-docker-plugin](https://github.com/bmuschko/gradle-docker-plugin) to leverage the `oas-raml-converter` to convert the output of the `openapi` task to RAML.
Using this approach your gradle build can still output a RAML specification.

See [openapi2raml.gradle](samples/restdocs-api-spec-sample/openapi2raml.gradle).

```
./gradlew restdocs-api-spec-sample:openapi
./gradlew -b samples/restdocs-api-spec-sample/openapi2raml.gradle openapi2raml
```

## Maintenance

This section of the README is targeted at project maintainers.

### Publish project

The project is published with the help of [TravisCI](./.travis.yml).
It's version number is determined by the Git tags (see [allegro/axion-release-plugin](https://axion-release-plugin.readthedocs.io)).
The Java dependencies are published to Sonatype with the help of the [gradle-nexus/publish-plugin](https://github.com/gradle-nexus/publish-plugin) and the Maven Publish Plugin.
The Gradle plugin is published to the [Gradle plugin portal](https://plugins.gradle.org/plugin/com.epages.restdocs-api-spec) with the help of the ['plugin-publish' plugin](https://plugins.gradle.org/plugin/com.gradle.plugin-publish) (see [docs.gradle.org](https://docs.gradle.org/current/userguide/publishing_gradle_plugins.html)).

Given that the `master` branch on the upstream repository is in the state from which you want to create a release, execute the following steps:

1. [Create release via the GitHub UI](https://github.com/ePages-de/restdocs-api-spec/releases/new) <br>
    Use the intended version number as "Tag version", e.g. "0.16.0".
    This will automatically trigger a Travis build which publishes the JAR files for this release to Sonatype.
2. Login to Sonatype and navigate to the [staging repositories](https://oss.sonatype.org/#stagingRepositories)
3. Close the staging repository <br>
    Select the generated staging repository and close it.
    Check that there are no errors afterwards (e.g. missing signatures or Javadoc JARs).
4. Release the repository <br>
    Select the generated staging repository and release it.
    Soon after, the release should be available in the ["Public Repositories" of ePages](https://oss.sonatype.org/service/local/repo_groups/public/content/com/epages/).
5. Update documentation <br>
    Create a new commit which updates the version numbers in the `README` file.
