package com.epages.restdocs.apispec.sample;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.constraints.NotNull;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CartResourceResourceAssembler extends RepresentationModelAssemblerSupport<Cart, CartResourceResourceAssembler.CartResource> {

    private final EntityLinks entityLinks;

    public CartResourceResourceAssembler(EntityLinks entityLinks) {
        super(CartController.class, CartResource.class);
        this.entityLinks = entityLinks;
    }

    @Override
    protected CartResource instantiateModel(Cart cart) {
        return new CartResource(
                cart.getProducts().stream()
                        .collect(Collectors.groupingBy(Product::getId)).values().stream()
                        .map(products -> new ProductLineItem(products.size(), products.get(0)))
                        .map(this::toProductLineItemResource)
                        .collect(Collectors.toList())
                ,cart.getTotal());
    }

    private EntityModel<ProductLineItem> toProductLineItemResource(ProductLineItem pli) {
        EntityModel<ProductLineItem> resource = EntityModel.of(pli);
        resource.add(entityLinks.linkForItemResource(Product.class, pli.getProduct().getId()).withRel("product"));
        return resource;
    }

    @Override
    public CartResource toModel(Cart cart) {

        CartResource resource = super.createModelWithId(cart.getId(), cart);

        if (!cart.isOrdered()) {
            resource.add(linkTo(methodOn(CartController.class).order(cart.getId())).withRel("order"));
        }
        return resource;
    }

    static class CartResource extends RepresentationModel<CartResource> {

        private final BigDecimal total;
        private final List<EntityModel<ProductLineItem>> products;

        public CartResource(List<EntityModel<ProductLineItem>> products, BigDecimal total) {
            this.total = total;
            this.products = products;
        }

        public BigDecimal getTotal() {
            return this.total;
        }

        public List<EntityModel<ProductLineItem>> getProducts() {
            return this.products;
        }
    }

    static class ProductLineItem extends RepresentationModel<ProductLineItem> {
        private final int quantity;

        @NotNull
        private final Product product;

        public ProductLineItem(int quantity, @NotNull Product product) {
            this.quantity = quantity;
            this.product = product;
        }

        public int getQuantity() {
            return quantity;
        }

        public Product getProduct() {
            return product;
        }
    }
}
