# Spring REST Docs OpenAPI Integration

![](https://img.shields.io/github/license/ePages-de/restdocs-openapi.svg)
[![Release](https://jitpack.io/v/ePages-de/restdocs-openapi.svg)](https://jitpack.io/#ePages-de/restdocs-openapi)
[![Build Status](https://travis-ci.org/ePages-de/restdocs-openapi.svg?branch=master)](https://travis-ci.org/ePages-de/restdocs-openapi)
[![Coverage Status](https://coveralls.io/repos/github/ePages-de/restdocs-openapi/badge.svg?branch=master)](https://coveralls.io/github/ePages-de/restdocs-openapi?branch=master)

This is an extension that adds [OpenAPI](https://www.openapis.org) as an output format to [Spring REST Docs](https://projects.spring.io/spring-restdocs/). 
It currently can output [OpenAPI 2.0](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md) in `json` and `yaml`

## Motivation

[Spring REST Docs](https://projects.spring.io/spring-restdocs/) is a great tool to produce documentation for your RESTful services that is accurate and readable.

We especially like its test-driven approach and this is the main reason why we chose it.

It offers support for AsciiDoc and Markdown. This is great for generating simple HTML-based documentation. 
But both are markup languages and thus it is hard to get any further than statically generated HTML. 

OpenAPI is a lot more flexible. 
With OpenAPI you get a machine-readable description of your API. There is a rich ecosystem around it that contains tools to:
- generate a HTML representation of your API - [ReDoc](https://github.com/Rebilly/ReDoc)
- generate an interactive API reference - e.g. using services like [stoplight.io](https://stoplight.io) or [readme.io](https://readme.io)

Also, OpenAPI is supported by many REST clients like [Postman](https://www.getpostman.com) and [Paw](https://paw.cloud). 
Thus having a OpenAPI representation of an API is a great plus when starting to work with it.

The most common use case to generate an OpenAPI specification is code introspection and adding documentation related annotations to your code.
We do not like enriching our production code with this information and clutter it with even more annotations.
We agree with Spring REST Docs that the test-driven way to produce accurate API documentation is the way to go.
This is why we came up with this project.

<!-- TOC depthFrom:2 -->

- [Motivation](#motivation)
- [Getting started](#getting-started)
    - [Project structure](#project-structure)
    - [Build configuration](#build-configuration)
    - [Usage with Spring REST Docs](#usage-with-spring-rest-docs)
    - [Documenting Bean Validation constraints](#documenting-bean-validation-constraints)
    - [Migrate existing Spring REST Docs tests](#migrate-existing-spring-rest-docs-tests)
    - [Security Definitions](#security-definitions)
    - [Running the gradle plugin](#running-the-gradle-plugin)
    - [Gradle plugin configuration](#gradle-plugin-configuration)
- [Generate HTML](#generate-html)
- [Convert to RAML](#convert-to-raml)
- [Limitations](#limitations)
    - [Rest Assured](#rest-assured)
    - [Maven plugin](#maven-plugin)

<!-- /TOC -->

## Getting started

### Project structure

The project consists of two components:

- [restdocs-openapi](restdocs-openapi) - contains the actual Spring REST Docs extension. 
This is most importantly the [ResourceDocumentation](restdocs-openapi/src/main/kotlin/com/epages/restdocs/openapi/ResourceDocumentation.kt) which is the entrypoint to use the extension in your tests. 
The [ResourceSnippet](restdocs-openapi/src/main/kotlin/com/epages/restdocs/openapi/ResourceSnippet.kt) is the snippet a json file `resource.json` containing all the details about the documented resource. 
- [restdocs-openapi-gradle-plugin](restdocs-openapi-gradle-plugin) - adds a gradle plugin that aggregates the `resource.json` files produced  by `ResourceSnippet` into one `OpenAPI` file for the whole project.

### Build configuration

```groovy
buildscript {
    repositories {
    //..
            maven { url = uri("https://jitpack.io") } //1
    }
    dependencies {
        //..
        classpath("com.github.epages-de.restdocs-openapi:restdocs-openapi-gradle-plugin:0.2.1) //2
    }
}
//..
apply plugin: 'com.epages.restdocs-openapi' //3

repositories { //4
    maven { url 'https://jitpack.io' }
}

//..

dependencies {
    //..
    testCompile 'com.github.epages-de.restdocs-openapi:restdocs-openapi:0.2.1' //5
    testCompile 'org.json:json:20170516' //6
}

openapi { //7
    host = 'localhost:8080'
    basePath = '/api'
    title = 'My API'
    version = '1.0.0'
    format = 'json'
}
```

1. add [jitpack](https://jitpack.io) repository to `buildscript` to resolve the `restdocs-raml-openapi-plugin`
2. add the dependency to `restdocs-raml-openapi-plugin`
3. apply `restdocs-raml-openapi-plugin`
4. add repositories used for dependency resolution. We use [jitpack](https://jitpack.io) here.
5. add the actual `restdocs-openapi` dependency to the test scope
6. `spring-boot` specifies an old version of `org.json:json`. We use [everit-org/json-schema](https://github.com/everit-org/json-schema) to generate json schema files. This project depends on a newer version of `org.json:json`. As versions from BOM always override transitive versions coming in through maven dependencies, you need to add an explicit dependency to `org.json:json:20170516`
7. add configuration options for restdocs-openapi-gradle-plugin see [Gradle plugin configuration](#gradle-plugin-configuration)

See the [build.gradle](samples/restdocs-openapi-sample/build.gradle) for the setup used in the sample project.

### Usage with Spring REST Docs

The class [ResourceDocumentation](restdocs-openapi/src/main/kotlin/com/epages/restdocs/openapi/ResourceDocumentation.kt) contains the entry point for using the [ResourceSnippet](restdocs-openapi/src/main/kotlin/com/epages/restdocs/openapi/ResourceSnippet.kt).

The most basic form does not take any parameters:

```java
mockMvc
  .perform(post("/carts"))
  .andDo(document("carts-create", resource("Create a cart")));
```

This test will produce the `resource.json` file in the snippets directory. 
This file just contains all the information that we can collect about the resource.
The format of this file is OpenAPI agnostic.

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

Just like you are used to do with Spring REST Docs we can also describe request fields, response fields, path variables, parameters, headers, and links.
Furthermore you can add a text description and a summary for your resource.
The extension also discovers `JWT` tokens in the `Authorization` header and will document the required scopes from it.

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

Please see the [CartIntegrationTest](samples/restdocs-openapi-sample/src/test/java/com/epages/restdocs/openapi/sample/CartIntegrationTest.java) in the sample application for a detailed example.

**:warning: Use `template URIs` to refer to path variables in your request**

Note how we use the `urlTemplate` to build the request with [`RestDocumentationRequestBuilders`](https://docs.spring.io/spring-restdocs/docs/current/api/org/springframework/restdocs/mockmvc/RestDocumentationRequestBuilders.html#get-java.lang.String-java.lang.Object...-). 
This makes the `urlTemplate` available in the snippet and we can depend on the non-expanded template when generating the OpenAPI file. 

 ```java
mockMvc.perform(get("/carts/{id}", cartId)
 ```

### Documenting Bean Validation constraints 

Similar to the way Spring REST Docs allows to use [bean validation constraints](https://docs.spring.io/spring-restdocs/docs/current/reference/html5/#documenting-your-api-constraints) to enhance your documentation, you can also use the constraints from your model classes to let `restdocs-openapi` enrich the generated JsonSchemas. 
`restdocs-openapi` provides the class [com.epages.restdocs.openapi.ConstrainedFields](restdocs-openapi/src/main/kotlin/com/epages/restdocs/openapi/ConstrainedFields.kt) to generate `FieldDescriptor`s that contain information about the constraints on this field. 

Currently the following constraints are considered when generating JsonSchema from `FieldDescriptor`s that have been created via `com.epages.restdocs.openapi.ConstrainedFields`
- `NotNull`, `NotEmpty`, and `NotBlank` annotated fields become required fields in the JsonSchema
- for String fields annotated with `NotEmpty`, and `NotBlank` the `minLength` constraint in JsonSchema is set to 1
- for String fields annotated with `Length` the `minLength` and `maxLength` constraints in JsonSchema are set to the value of the corresponding attribute of the annotation

If you already have your own `ConstraintFields` implementation you can also add the logic from `com.epages.restdocs.openapi.ConstrainedFields` to your own class. 
Here it is important to add the constraints under the key `validationConstraints` into the attributes map if the `FieldDescriptor`.

### Migrate existing Spring REST Docs tests

For convenience when applying `restdocs-openapi` to an existing project that uses Spring REST Docs, we introduced [com.epages.restdocs.openapi.MockMvcRestDocumentationWrapper](restdocs-openapi/src/main/kotlin/com/epages/restdocs/openapi/MockMvcRestDocumentationWrapper.kt).

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

This will do exactly the same as using `MockMvcRestDocumentation.document` without `restdocs-openapi`.
Additionally it will add a `ResourceSnippet` with the descriptors you provided in the `RequestFieldsSnippet`, `ResponseFieldsSnippet`, and `LinksSnippet`.

### Security Definitions

The project has limited suport for describing security requirements of an API. 
Currently we only suppert Oauth2 with [JWT](https://jwt.io/) tokens.

`restdocs-openapi` inspects the `AUTHORIZATION` header of a request for a `JWT` token. 
If such a token is found the scopes are extracted and added to the `resource.json` snippet.

The `restdocs-openapi-gradle-plugin` will consider this information if the `oauth2SecuritySchemeDefinition` configuration option is set (see [Gradle plugin configuration](#gradle-plugin-configuration)). 
This will result in a top-level `securityDefinitions` in the OpenAPI definition. 
Additionally the required scopes will be added in the `security` section of an `operation`.

### Running the gradle plugin

`restdocs-openapi-gradle-plugin` is responsible for picking up the generated `resource.json` files and aggregate them into an OpenAPI specification (at the moment we support 2.0 only).
For this purpose we use the `openapi` task:

```
./gradlew openapi
```

For our [sample project](samples/restdocs-openapi-sample) this creates a `openapi.json` file in the output directory (`build/openapi`).

### Gradle plugin configuration

The `restdocs-openapi-gradle-plugin` takes the following configuration options - all are optional.

Name | Description | Default value
---- | ----------- | -------------
title | The title of the application. Used for the `title` attribute in the [Info object](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#info-object) | `API documentation`
version | The version of the api. Used for the `version` attribute in the [Info object](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#info-object) | project version
host | The host serving the API - corresponds to the attribute with the same name in the [OpenAPI root object](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#swagger-object)| `localhost`
basePath | The base path on which the API is served - corresponds to the attribute with the same name in the [OpenAPI root object](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#swagger-object) | null
schemes | The supported transfer protocols of the API - corresponds to the attribute with the same name in the [OpenAPI root object](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#swagger-object) | `['http'"]`
format | The format of the output OpenAPI file - supported values are `json` and `yaml` | `json`
separatePublicApi | Should the plugin generate an additional OpenAPI specification file that does not contain the resources marked as private | `false`
outputDirectory | The output directory | `build/openapi`
outputFileNamePrefix | The file name prefix of the output file. | `api` which results in e.g. `api.json`
snippetsDirectory | The directory Spring REST Docs generated the snippets to | `build/generated-snippets`
oauth2SecuritySchemeDefinition | Closure containing information to generate the [securityDefinitions](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#securityDefinitionsObject) object in the `OpenAPI` specification. | empty
oauth2SecuritySchemeDefinition.flows | The Oauth2 flows the API supports. Use valid values from the [securityDefinitions](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#securityDefinitionsObject) specification. | no default - required if `oauth2SecuritySchemeDefinition` is set.
oauth2SecuritySchemeDefinition.tokenUrl | The Oauth2 tokenUrl | no default - required for the flows `password`, `application`, `accessCode`.
oauth2SecuritySchemeDefinition.authorizationUrl | The Oauth2 authorizationUrl | no default - required for the flows `implicit`, `accessCode`.
oauth2SecuritySchemeDefinition.scopeDescriptionsPropertiesFile | A yaml file mapping scope names to descriptions. These are used in the `securityDefinitions` as the [scope description](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#scopesObject) | no default - if not provided the scope descriptions default to `No description`.

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

The `scopeDescriptionsPropertiesFile` is supposed to be a yaml file:
```yaml
scope-name: A description
```

## Generate HTML

We can use [redoc](https://github.com/Rebilly/ReDoc) to generate HTML API reference from our OpenAPI specification.

The [redoc-cli](https://www.npmjs.com/package/redoc-cli) can be used to serve this API reference
```
npm install -g redoc-cli
redoc-cli serve build/openapi/openapi.json
```

## Convert to RAML

This project supersedes [restdocs-raml](https://github.com/ePages-de/restdocs-raml). 
So if you are coming from `restdocs-raml` you might want to switch to `restdocs-openapi`. 

The API of both projects is fairly similar and it is easy to migrate.

Also there are several ways to convert an OpenAPI specification to RAML.
There are converters around that can help you to achieve this conversion.

- [oas-raml-converter](https://github.com/mulesoft/oas-raml-converter) - an npm project that provides a CLI to convert between OpenAPI and RAML - it also provides an [online converter](https://mulesoft.github.io/oas-raml-converter/)
- [api-matic](https://apimatic.io/transformer) - an online converter capable of converting between many api specifications

In the [sample project](samples/restdocs-openapi-sample) you find a build configuration that uses the [oas-raml-converter-docker](https://hub.docker.com/r/zaddo/oas-raml-converter-docker/) docker image and the [gradle-docker-plugin](https://github.com/bmuschko/gradle-docker-plugin) to leverage the `oas-raml-converter` to convert the output of the `openapi` task to RAML. 
Using this approach your gradle build can still output a RAML specification.

See [openapi2raml.gradle](samples/restdocs-openapi-sample/openapi2raml.gradle).

```
./gradlew restdocs-openapi-sample:openapi
./gradlew -b samples/restdocs-openapi-sample/openapi2raml.gradle openapi2raml
```

## Limitations

### Rest Assured

Spring REST Docs also supports REST Assured to write tests that produce documentation. We currently have not tried REST Assured with our project.

### Maven plugin

Currently only a gradle plugin exists.
