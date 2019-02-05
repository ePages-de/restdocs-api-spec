# restdocs-api-spec-sample-web-test-client

This sample shows how to use restdocs-api-spec with `WebTestClient`.

We can use the `resource` snippet just like any other snippet provided by spring-restdocs.

Note how we depend on `com.epages:restdocs-api-spec` instead of `com.epages:restdocs-api-spec-mockmvc` because we do not want `MockMvc` on the classpath here.

## Run the sample

Run the following command from the root directory of restdocs-api-spec

```
./gradlew restdocs-api-spec-sample-web-test-client:openapi restdocs-api-spec-sample-web-test-client:openapi3 restdocs-api-spec-sample-web-test-client:postman
```

After running the command above you will find the following api specification files in `build/api-spec`:
- openapi.yaml
- openapi3.yaml
- postman-collection.json