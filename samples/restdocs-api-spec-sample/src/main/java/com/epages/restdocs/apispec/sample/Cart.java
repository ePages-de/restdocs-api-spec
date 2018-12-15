package com.epages.restdocs.apispec.sample;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.hateoas.Identifiable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
public class Cart implements Identifiable<Long> {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToMany
    private List<Product> products = new ArrayList<>();

    @JsonIgnore
    private boolean ordered;

    @Override
    public Long getId() {
        return id;
    }

    public List<Product> getProducts() {
        return products;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public BigDecimal getTotal() {
        return products.stream()
                .map(Product::getPrice)
                .reduce(BigDecimal::add)
                .orElse(ZERO);
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }
}
