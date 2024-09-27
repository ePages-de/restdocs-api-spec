package com.epages.restdocs.apispec.sample;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import org.springframework.hateoas.RepresentationModel;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
public class Product extends RepresentationModel<Product> {

    protected Product() {
    }

    public Product(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @NotEmpty
    private String name;

    @NotNull
    @Positive
    private BigDecimal price;

    public Long getId() {
        return this.id;
    }

    public @NotEmpty String getName() {
        return this.name;
    }

    public @NotNull
    @Positive BigDecimal getPrice() {
        return this.price;
    }

    public void setName(@NotEmpty String name) {
        this.name = name;
    }

    public void setPrice(@NotNull @Positive BigDecimal price) {
        this.price = price;
    }
}
