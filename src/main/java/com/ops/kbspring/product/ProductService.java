package com.ops.kbspring.product;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepo repository;
    public ProductService(ProductRepo repository) { this.repository = repository; }

    @Cacheable(value = "products", key = "#id")
    public ProductModel getProduct(Long id) {
        System.out.println(">>> DB HIT (slow path) for product " + id);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) { /* simulate slow DB */ }
        return new ProductModel(id, "Product-" + id, 9.99);
    }

    @Cacheable(value = "products-db", key = "#id", sync = true)
    public ProductModel getProductDb(Long id) {
        System.out.println(">>> DB read for product " + id);
        return repository.findById(id)
                .map(e -> new ProductModel(e.getId(), e.getName(), e.getPrice()))
                .orElse(null);
    }

    @CacheEvict(value = "products-db", key = "#id")
    public void deleteProduct(Long id) {
        repository.deleteById(id);
    }

    @CachePut(value = "products-db", key = "#id")
    public ProductModel updateProduct(Long id, double newPrice) {
        ProductEntity e = repository.findById(id).orElseThrow();
        e.setPrice(newPrice);
        repository.save(e);
        return new ProductModel(e.getId(), e.getName(), e.getPrice());  // this becomes the new cache value
    }


}
