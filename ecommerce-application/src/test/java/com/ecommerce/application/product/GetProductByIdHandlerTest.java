package com.ecommerce.application.product;

import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.domain.product.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetProductByIdHandlerTest {

    @Test
    void shouldGetProductById() {
        Product product = new Product(
                "Teclado",
                "Teclado mecânico",
                BigDecimal.valueOf(250),
                8
        );
        FakeProductRepository productRepository = new FakeProductRepository(product);
        GetProductByIdHandler handler = new GetProductByIdHandler(productRepository);

        ProductResponse response = handler.handle(product.getId());

        assertThat(response.id()).isEqualTo(product.getId());
        assertThat(response.name()).isEqualTo("Teclado");
        assertThat(response.description()).isEqualTo("Teclado mecânico");
        assertThat(response.price()).isEqualByComparingTo(BigDecimal.valueOf(250));
        assertThat(response.stockQuantity()).isEqualTo(8);
        assertThat(response.active()).isTrue();
        assertThat(response.createdAt()).isEqualTo(product.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(product.getUpdatedAt());
    }

    @Test
    void shouldThrowErrorWhenProductDoesNotExist() {
        FakeProductRepository productRepository = new FakeProductRepository(null);
        GetProductByIdHandler handler = new GetProductByIdHandler(productRepository);

        assertThatThrownBy(() -> handler.handle(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Produto não encontrado.");
    }

    private static final class FakeProductRepository implements ProductRepository {

        private final Product product;

        private FakeProductRepository(Product product) {
            this.product = product;
        }

        @Override
        public Product save(Product product) {
            return product;
        }

        @Override
        public Optional<Product> findById(UUID id) {
            return Optional.ofNullable(product);
        }

        @Override
        public List<Product> findAll() {
            return List.of();
        }
    }
}
