package com.epages.restdocs.apispec.sample;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.restdocs.constraints.Constraint;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Demonstrates how to attach Bean Validation constraints to path/query parameters so that the
 * generated OpenAPI specification includes {@code minLength}, {@code maxLength}, {@code pattern},
 * {@code minimum} and {@code maximum} on the parameter schemas.
 *
 * The key ingredient is the {@code "validationConstraints"} attribute on a parameter descriptor.
 * The OpenAPI 3 generator's {@code ParameterConstraintResolver} reads these constraints and
 * translates them into the appropriate JSON Schema keywords.
 *
 * <p>Use {@link org.springframework.restdocs.snippet.Attributes#key} to attach constraints:
 * <pre>{@code
 * parameterWithName("id")
 *     .description("User ID")
 *     .attributes(key("validationConstraints").value(
 *         List.of(new Constraint("javax.validation.constraints.Size",
 *                 Map.of("min", 1, "max", 36)))
 *     ))
 * }</pre>
 */
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ProductSearchIntegrationTest extends BaseIntegrationTest {

    private static final String SIZE = "jakarta.validation.constraints.Size";
    private static final String NOT_BLANK = "jakarta.validation.constraints.NotBlank";
    private static final String PATTERN = "jakarta.validation.constraints.Pattern";
    private static final String MIN = "jakarta.validation.constraints.Min";
    private static final String MAX = "jakarta.validation.constraints.Max";

    /**
     * GET /product-search?name=&page=&currency=
     *
     * <p>Shows how {@code @NotBlank + @Size(max=50)} on a {@code String} query parameter
     * becomes {@code minLength: 1, maxLength: 50} in OpenAPI, and how {@code @Min + @Max} on an
     * {@code Integer} becomes {@code minimum: 0, maximum: 100}.
     */
    @Test
    public void should_document_product_search_with_constrained_query_parameters() throws Exception {
        givenProduct("Fancy Shirt", "15.10");

        resultActions = mockMvc.perform(get("/product-search")
                .param("name", "Fancy")
                .param("page", "0")
                .param("currency", "EUR"))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[0].price", notNullValue()))
                .andDo(document("product-search",
                        resource(ResourceSnippetParameters.builder()
                                .summary("Search products")
                                .description("Returns products whose name contains the given search term.")
                                .queryParameters(

                                        // required – @NotBlank + @Size(max=50) on String
                                        // → OpenAPI: type: string, minLength: 1, maxLength: 50
                                        parameterWithName("name")
                                                .description("Case-insensitive product name filter (required, 1–50 characters).")
                                                .attributes(key("validationConstraints").value(
                                                        List.of(
                                                                new Constraint(NOT_BLANK, Map.of()),
                                                                new Constraint(SIZE, Map.of("max", 50))
                                                        )
                                                )),

                                        // optional – @Min(0) + @Max(100) on Integer
                                        // → OpenAPI: type: integer, minimum: 0, maximum: 100
                                        parameterWithName("page")
                                                .type(SimpleType.INTEGER)
                                                .optional()
                                                .description("Zero-based page number (default 0, range 0–100).")
                                                .attributes(key("validationConstraints").value(
                                                        List.of(
                                                                new Constraint(MIN, Map.of("value", 0)),
                                                                new Constraint(MAX, Map.of("value", 100))
                                                        )
                                                )),

                                        // optional – @Pattern(regexp="[A-Z]{3}") on String
                                        // → OpenAPI: type: string, pattern: "[A-Z]{3}"
                                        parameterWithName("currency")
                                                .optional()
                                                .description("ISO 4217 three-letter currency code, e.g. EUR, USD, PLN.")
                                                .attributes(key("validationConstraints").value(
                                                        List.of(
                                                                new Constraint(PATTERN, Map.of("regexp", "[A-Z]{3}"))
                                                        )
                                                ))
                                )
                                .responseFields(
                                        fieldWithPath("[].name").description("Product name."),
                                        fieldWithPath("[].price").description("Product price as a plain decimal string."),
                                        fieldWithPath("[].currency").description("ISO 4217 currency code passed in the request; null when not supplied.").optional()
                                )
                                .build()
                        )
                ));
    }

    /**
     * GET /product-search/{sku}
     *
     * <p>Shows how {@code @Size(min=8, max=8) + @Pattern(regexp="[A-Z]{3}[0-9]{5}")} on a
     * {@code String} path variable becomes {@code minLength: 8, maxLength: 8,
     * pattern: "[A-Z]{3}[0-9]{5}"} in OpenAPI.
     */
    @Test
    public void should_document_get_product_by_sku_with_constrained_path_variable() throws Exception {
        givenProduct();

        resultActions = mockMvc.perform(get("/product-search/{sku}", "SHR00001"))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", notNullValue()))
                .andExpect(jsonPath("$.price", notNullValue()))
                .andDo(document("product-get-by-sku",
                        resource(ResourceSnippetParameters.builder()
                                .summary("Get product by SKU")
                                .description("Retrieves a product identified by its 8-character SKU.")
                                .pathParameters(

                                        // @Size(min=8, max=8) + @Pattern on String
                                        // → OpenAPI: type: string, minLength: 8, maxLength: 8,
                                        //            pattern: "[A-Z]{3}[0-9]{5}"
                                        parameterWithName("sku")
                                                .description("Stock-keeping unit: exactly 8 characters — 3 uppercase letters followed by 5 digits (e.g. CAP00001).")
                                                .attributes(key("validationConstraints").value(
                                                        List.of(
                                                                new Constraint(SIZE, Map.of("min", 8, "max", 8)),
                                                                new Constraint(PATTERN, Map.of("regexp", "[A-Z]{3}[0-9]{5}"))
                                                        )
                                                ))
                                )
                                .responseFields(
                                        fieldWithPath("name").description("Product name."),
                                        fieldWithPath("price").description("Product price as a plain decimal string."),
                                        fieldWithPath("currency").description("Currency code; null when not requested.").optional()
                                )
                                .build()
                        )
                ));
    }

    @Test
    public void should_return_400_when_name_is_blank() throws Exception {
        resultActions = mockMvc.perform(get("/product-search")
                .param("name", ""))
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andDo(document("product-search-name-blank",
                        resource(ResourceSnippetParameters.builder()
                                .summary("Search products – blank name (400)")
                                .description("Returns 400 when the required `name` parameter is blank (violates @NotBlank).")
                                .queryParameters(
                                        parameterWithName("name")
                                                .description("Blank value — violates @NotBlank constraint.")
                                                .attributes(key("validationConstraints").value(
                                                        List.of(new Constraint(NOT_BLANK, Map.of()))
                                                ))
                                )
                                .build()
                        )
                ));
    }

    @Test
    public void should_return_400_when_name_exceeds_max_length() throws Exception {
        String tooLongName = "A".repeat(51);

        resultActions = mockMvc.perform(get("/product-search")
                .param("name", tooLongName))
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andDo(document("product-search-name-too-long",
                        resource(ResourceSnippetParameters.builder()
                                .summary("Search products – name too long (400)")
                                .description("Returns 400 when `name` exceeds 50 characters (violates @Size(max=50)).")
                                .queryParameters(
                                        parameterWithName("name")
                                                .description("Value of 51+ characters — violates @Size(max=50) constraint.")
                                                .attributes(key("validationConstraints").value(
                                                        List.of(new Constraint(SIZE, Map.of("max", 50)))
                                                ))
                                )
                                .build()
                        )
                ));
    }

    @Test
    public void should_return_400_when_page_is_negative() throws Exception {
        resultActions = mockMvc.perform(get("/product-search")
                .param("name", "Shirt")
                .param("page", "-1"))
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andDo(document("product-search-page-negative",
                        resource(ResourceSnippetParameters.builder()
                                .summary("Search products – page below minimum (400)")
                                .description("Returns 400 when `page` is negative (violates @Min(0)).")
                                .queryParameters(
                                        parameterWithName("name")
                                                .description("Product name filter.")
                                                .attributes(key("validationConstraints").value(
                                                        List.of(new Constraint(NOT_BLANK, Map.of()))
                                                )),
                                        parameterWithName("page")
                                                .type(SimpleType.INTEGER)
                                                .optional()
                                                .description("Negative value — violates @Min(0) constraint.")
                                                .attributes(key("validationConstraints").value(
                                                        List.of(new Constraint(MIN, Map.of("value", 0)))
                                                ))
                                )
                                .build()
                        )
                ));
    }

    /**
     * Verifies that a {@code page} value above 100 triggers 400.
     * The {@code @Max(100)} constraint enforces the upper bound.
     */
    @Test
    public void should_return_400_when_page_exceeds_maximum() throws Exception {
        resultActions = mockMvc.perform(get("/product-search")
                .param("name", "Shirt")
                .param("page", "101"))
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andDo(document("product-search-page-too-large",
                        resource(ResourceSnippetParameters.builder()
                                .summary("Search products – page above maximum (400)")
                                .description("Returns 400 when `page` exceeds 100 (violates @Max(100)).")
                                .queryParameters(
                                        parameterWithName("name")
                                                .description("Product name filter.")
                                                .attributes(key("validationConstraints").value(
                                                        List.of(new Constraint(NOT_BLANK, Map.of()))
                                                )),
                                        parameterWithName("page")
                                                .type(SimpleType.INTEGER)
                                                .optional()
                                                .description("Value of 101 — violates @Max(100) constraint.")
                                                .attributes(key("validationConstraints").value(
                                                        List.of(new Constraint(MAX, Map.of("value", 100)))
                                                ))
                                )
                                .build()
                        )
                ));
    }

    @Test
    public void should_return_400_when_currency_does_not_match_pattern() throws Exception {
        resultActions = mockMvc.perform(get("/product-search")
                .param("name", "Shirt")
                .param("currency", "eur"))  // lowercase – violates [A-Z]{3}
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andDo(document("product-search-currency-invalid",
                        resource(ResourceSnippetParameters.builder()
                                .summary("Search products – invalid currency code (400)")
                                .description("Returns 400 when `currency` does not match `[A-Z]{3}` (violates @Pattern).")
                                .queryParameters(
                                        parameterWithName("name")
                                                .description("Product name filter.")
                                                .attributes(key("validationConstraints").value(
                                                        List.of(new Constraint(NOT_BLANK, Map.of()))
                                                )),
                                        parameterWithName("currency")
                                                .optional()
                                                .description("Lowercase value 'eur' — violates @Pattern(regexp=\"[A-Z]{3}\") constraint.")
                                                .attributes(key("validationConstraints").value(
                                                        List.of(new Constraint(PATTERN, Map.of("regexp", "[A-Z]{3}")))
                                                ))
                                )
                                .build()
                        )
                ));
    }

    @Test
    public void should_return_400_when_sku_has_wrong_length() throws Exception {
        resultActions = mockMvc.perform(get("/product-search/{sku}", "CAP001"))  // 6 chars
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andDo(document("product-get-by-sku-wrong-length",
                        resource(ResourceSnippetParameters.builder()
                                .summary("Get product by SKU – wrong length (400)")
                                .description("Returns 400 when the SKU is not exactly 8 characters (violates @Size(min=8, max=8)).")
                                .pathParameters(
                                        parameterWithName("sku")
                                                .description("6-character SKU 'CAP001' — violates @Size(min=8, max=8) constraint.")
                                                .attributes(key("validationConstraints").value(
                                                        List.of(new Constraint(SIZE, Map.of("min", 8, "max", 8)))
                                                ))
                                )
                                .build()
                        )
                ));
    }

    @Test
    public void should_return_400_when_sku_does_not_match_pattern() throws Exception {
        resultActions = mockMvc.perform(get("/product-search/{sku}", "123ABCDE"))  // digits first
                .andDo(print());

        resultActions
                .andExpect(status().isBadRequest())
                .andDo(document("product-get-by-sku-invalid-pattern",
                        resource(ResourceSnippetParameters.builder()
                                .summary("Get product by SKU – invalid format (400)")
                                .description("Returns 400 when the SKU does not match `[A-Z]{3}[0-9]{5}` (violates @Pattern).")
                                .pathParameters(
                                        parameterWithName("sku")
                                                .description("Value '123ABCDE' — violates @Pattern(regexp=\"[A-Z]{3}[0-9]{5}\") constraint.")
                                                .attributes(key("validationConstraints").value(
                                                        List.of(new Constraint(PATTERN, Map.of("regexp", "[A-Z]{3}[0-9]{5}")))
                                                ))
                                )
                                .build()
                        )
                ));
    }
}
