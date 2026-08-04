package com.ecommerce.application.product.service;

import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.application.product.repository.ProductRepository;
import com.ecommerce.domain.product.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeleteProductServiceTest {

    @Test
    void shouldDeactivateProduct() {
        Product product = createValidProduct();
        FakeProductRepository productRepository = new FakeProductRepository(product);
        DeleteProductService service = new DeleteProductService(productRepository);

        service.delete(product.getId());

        assertThat(product.isActive()).isFalse();
        assertThat(productRepository.saveWasCalled()).isTrue();
        assertThat(productRepository.savedProduct()).isSameAs(product);
    }

    @Test
    void shouldThrowErrorWhenProductDoesNotExist() {
        FakeProductRepository productRepository = new FakeProductRepository(null);
        DeleteProductService service = new DeleteProductService(productRepository);

        assertThatThrownBy(() -> service.delete(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Produto não encontrado");

        assertThat(productRepository.saveWasCalled()).isFalse();
    }

    private Product createValidProduct() {
        return new Product(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                5
        );
    }

    private static final class FakeProductRepository implements ProductRepository {

        private final Product product;
        private Product savedProduct;

        private FakeProductRepository(Product product) {
            this.product = product;
        }

        @Override
        public Product save(Product product) {
            this.savedProduct = product;
            return product;
        }

        @Override
        public Optional<Product> findById(UUID id) {
            return Optional.ofNullable(product)
                    .filter(existingProduct -> existingProduct.getId().equals(id));
        }

        @Override
        public List<Product> findAll() {
            return product == null ? List.of() : List.of(product);
        }

        private boolean saveWasCalled() {
            return savedProduct != null;
        }

        private Product savedProduct() {
            return savedProduct;
        }
    }
}
