package com.ecommerce.infrastructure.persistence.product;

import com.ecommerce.domain.product.Product;
import com.ecommerce.infrastructure.persistence.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRepositoryAdapterIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private ProductRepositoryAdapter productRepositoryAdapter;

    @Test
    void shouldSaveAndFindProductById() {
        Product product = new Product(
                "Notebook",
                "Notebook para desenvolvimento",
                BigDecimal.valueOf(4500),
                10
        );

        Product savedProduct = productRepositoryAdapter.save(product);
        Optional<Product> foundProduct = productRepositoryAdapter.findById(savedProduct.getId());

        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getId()).isEqualTo(savedProduct.getId());
        assertThat(foundProduct.get().getName()).isEqualTo("Notebook");
        assertThat(foundProduct.get().getPrice()).isEqualByComparingTo("4500.00");
        assertThat(foundProduct.get().getStockQuantity()).isEqualTo(10);
        assertThat(foundProduct.get().isActive()).isTrue();
    }

    @Test
    void shouldListAllProducts() {
        Product notebook = new Product("Notebook", "Notebook para desenvolvimento", BigDecimal.valueOf(4500), 10);
        Product mouse = new Product("Mouse", "Mouse sem fio", BigDecimal.valueOf(120), 30);

        productRepositoryAdapter.save(notebook);
        productRepositoryAdapter.save(mouse);

        List<Product> products = productRepositoryAdapter.findAll();

        assertThat(products)
                .hasSize(2)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Notebook", "Mouse");
    }
}
