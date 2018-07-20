package com.epages.restdocs.openapi.sample;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class CartResourceResourceAssembler extends ResourceAssemblerSupport<Cart, CartResourceResourceAssembler.CartResource> {

    private EntityLinks entityLinks;

    public CartResourceResourceAssembler(EntityLinks entityLinks) {
        super(CartController.class, CartResource.class);
        this.entityLinks = entityLinks;
    }

    @Override
    protected CartResource instantiateResource(Cart cart) {
        return CartResource.builder()
                .products(cart.getProducts().stream()
                        .collect(Collectors.groupingBy(Product::getId)).values().stream()
                        .map(products -> new ProductLineItem(products.size(), products.get(0)))
                        .map(this::toProductLineItemResource)
                        .collect(Collectors.toList()))
                .total(cart.getTotal())
                .build();
    }

    private Resource<ProductLineItem> toProductLineItemResource(ProductLineItem pli) {
        Resource<ProductLineItem> resource = new Resource<>(pli);
        resource.add(entityLinks.linkForSingleResource(pli.getProduct()).withRel("product"));
        return resource;
    }

    @Override
    public CartResource toResource(Cart cart) {

        CartResource resource = super.createResourceWithId(cart.getId(), cart);

        if (!cart.isOrdered()) {
            resource.add(linkTo(methodOn(CartController.class).order(cart.getId())).withRel("order"));
        }
        return resource;
    }

    @Builder
    @Getter
    static class CartResource extends ResourceSupport {

        private final BigDecimal total;
        private final List<Resource<ProductLineItem>> products;
    }

    @RequiredArgsConstructor
    @Getter
    static class ProductLineItem extends ResourceSupport {
        private final int quantity;
        @NotNull
        private final Product product;
    }
}
