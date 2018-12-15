package com.epages.restdocs.apispec.sample;

import org.springframework.hateoas.Identifiable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
public class Product implements Identifiable<Long> {

    public Product(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }

    private Product() {
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
