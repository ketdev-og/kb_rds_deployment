package com.ops.kbspring;

import com.ops.kbspring.product.ProductEntity;
import com.ops.kbspring.product.ProductModel;
import com.ops.kbspring.product.ProductRepo;
import com.ops.kbspring.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBeans;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class ProductCacheTest {
    @Autowired
    ProductService productService;
    @Autowired
    CacheManager cacheManager;

    @MockitoSpyBean
    ProductRepo productRepo;

    @BeforeEach
    void clearCache() {
        productRepo.save(new ProductEntity(1L, "Keyboard", 49.99));
        Objects.requireNonNull(cacheManager.getCache("products")).clear();
        Objects.requireNonNull(cacheManager.getCache("products-db")).clear();
    }

    @Test
    void secondCallIsCached() {
        assertThat(Objects.requireNonNull(cacheManager.getCache("products")).get(1L)).isNull();
        ProductModel miss =  productService.getProduct(1L);
        ProductModel hit = productService.getProduct(1L);

       assertThat(cacheManager.getCache("products").get(1L, ProductModel.class)).isEqualTo(miss);
       assertThat(miss).isEqualTo(hit);
    }


    private long time(Runnable r){
        long s = System.currentTimeMillis();
        r.run();
        return System.currentTimeMillis() - s;
    }

    @Test
    void secondReadIsServedFromCache() {
        productService.getProductDb(1L);
        productService.getProductDb(1L);

        verify(productRepo, times(1)).findById(1L);
    }

    @Test
    void secondReadIsServedFromCacheForNullDbRecord() {
        productService.getProductDb(999L);
        productService.getProductDb(999L);

        verify(productRepo, times(1)).findById(999L);
    }

    @Test
    void updateKeepsCacheFresh() {
        productService.getProductDb(1L);                 // caches 49.99
        productService.updateProduct(1L, 79.99);         // @CachePut overwrites entry
        ProductModel p = productService.getProductDb(1L); // HIT, but fresh
        assertThat(p.price()).isEqualTo(79.99);          // not stale
    }

}

