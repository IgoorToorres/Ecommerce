package com.ecommerce.application.product;

import com.ecommerce.domain.exception.DomainException;
import com.ecommerce.domain.product.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateProductHandlerTest {

    @Test
    void shouldCreateProduct() {
        FakeProductRepository productRepository = new FakeProductRepository();
        CreateProductHandler handler = new CreateProductHandler(productRepository);
        CreateProductCommand command = new CreateProductCommand(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                5
        );

        ProductResponse response = handler.handle(command);

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Mouse");
        assertThat(response.description()).isEqualTo("Mouse sem fio");
        assertThat(response.price()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(response.stockQuantity()).isEqualTo(5);
        assertThat(response.active()).isTrue();
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();

        assertThat(productRepository.saveWasCalled()).isTrue();
        assertThat(productRepository.savedProduct()).isNotNull();
        assertThat(productRepository.savedProduct().getName()).isEqualTo("Mouse");
        assertThat(productRepository.savedProduct().getDescription()).isEqualTo("Mouse sem fio");
        assertThat(productRepository.savedProduct().getPrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(productRepository.savedProduct().getStockQuantity()).isEqualTo(5);
        assertThat(productRepository.savedProduct().isActive()).isTrue();
    }

    @Test
    void shouldNotSaveProductWhenNameIsBlank() {
        CreateProductCommand command = new CreateProductCommand(
                " ",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                5
        );

        assertInvalidCommandDoesNotSave(command);
    }

    @Test
    void shouldNotSaveProductWhenNameIsNull() {
        CreateProductCommand command = new CreateProductCommand(
                null,
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                5
        );

        assertInvalidCommandDoesNotSave(command);
    }

    @Test
    void shouldNotSaveProductWhenPriceIsNull() {
        CreateProductCommand command = new CreateProductCommand(
                "Mouse",
                "Mouse sem fio",
                null,
                5
        );

        assertInvalidCommandDoesNotSave(command);
    }

    @Test
    void shouldNotSaveProductWhenPriceIsZero() {
        CreateProductCommand command = new CreateProductCommand(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.ZERO,
                5
        );

        assertInvalidCommandDoesNotSave(command);
    }

    @Test
    void shouldNotSaveProductWhenPriceIsNegative() {
        CreateProductCommand command = new CreateProductCommand(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(-1),
                5
        );

        assertInvalidCommandDoesNotSave(command);
    }

    @Test
    void shouldNotSaveProductWhenStockIsNegative() {
        CreateProductCommand command = new CreateProductCommand(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                -1
        );

        assertInvalidCommandDoesNotSave(command);
    }

    private void assertInvalidCommandDoesNotSave(CreateProductCommand command) {
        FakeProductRepository productRepository = new FakeProductRepository();
        CreateProductHandler handler = new CreateProductHandler(productRepository);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(DomainException.class);

        assertThat(productRepository.saveWasCalled()).isFalse();
    }

    private static final class FakeProductRepository implements ProductRepository {

        private Product savedProduct;

        @Override
        public Product save(Product product) {
            this.savedProduct = product;
            return product;
        }

        private boolean saveWasCalled() {
            return savedProduct != null;
        }

        private Product savedProduct() {
            return savedProduct;
        }
    }
}
