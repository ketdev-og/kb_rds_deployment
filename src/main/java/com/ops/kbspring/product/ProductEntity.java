package com.ops.kbspring.product;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.io.Serializable;

@Entity
public class ProductEntity implements Serializable {
    @Id
    private Long id;
    private String name;
    private double price;

    protected ProductEntity() {}                 // JPA needs this

    public ProductEntity(Long id, String name, double price) {
        this.id = id; this.name = name; this.price = price;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
