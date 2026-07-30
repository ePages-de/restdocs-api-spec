package com.epages.restdocs.apispec.sample;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.stream.StreamSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demonstrates Bean Validation constraints on {@code @RequestParam} and {@code @PathVariable}
 * parameters. When tests document these endpoints with
 * {@code ResourceDocumentation.parameterWithName(...)} and the
 * {@code "validationConstraints"} attribute, the generated OpenAPI specification reflects the
 * constraints as {@code minLength}, {@code maxLength}, {@code pattern}, {@code minimum} and
 * {@code maximum} fields on the parameter schemas — thanks to
 * {@code ParameterConstraintResolver} in the OpenAPI 3 generator.
 */
@RestController
@RequestMapping("/product-search")
public class ProductSearchController {

    private final ProductRepository productRepository;

    public ProductSearchController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProductView>> search(
            @RequestParam @NotBlank @Size(max = 50) String name,
            @RequestParam(required = false, defaultValue = "0") @Min(0) @Max(100) Integer page,
            @RequestParam(required = false) @Pattern(regexp = "[A-Z]{3}") String currency) {

        List<ProductView> results = StreamSupport
                .stream(productRepository.findAll().spliterator(), false)
                .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()))
                .map(p -> new ProductView(p.getName(), p.getPrice().toPlainString(), currency))
                .toList();

        return ResponseEntity.ok(results);
    }

    @GetMapping("/{sku}")
    public ResponseEntity<ProductView> getBySku(
            @PathVariable @Size(min = 8, max = 8) @Pattern(regexp = "[A-Z]{3}[0-9]{5}") String sku) {

        return StreamSupport.stream(productRepository.findAll().spliterator(), false)
                .findFirst()
                .map(p -> new ProductView(p.getName(), p.getPrice().toPlainString(), null))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record ProductView(String name, String price, String currency) {}
}
