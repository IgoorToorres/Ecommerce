package com.ecommerce.application.product.service;

import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.application.product.command.UpdateProductCommand;
import com.ecommerce.application.product.repository.ProductRepository;
import com.ecommerce.application.product.response.ProductResponse;
import com.ecommerce.domain.exception.DomainException;
import com.ecommerce.domain.product.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateProductServiceTest {

    @Test
    void shouldUpdateProduct() {
        Product product = createValidProduct();
        FakeProductRepository productRepository = new FakeProductRepository(product);
        UpdateProductService service = new UpdateProductService(productRepository);
        UpdateProductCommand command = new UpdateProductCommand(
                product.getId(),
                " Teclado ",
                "Teclado mecânico",
                BigDecimal.valueOf(250),
                8
        );

        ProductResponse response = service.update(command);

        assertThat(response.id()).isEqualTo(product.getId());
        assertThat(response.name()).isEqualTo("Teclado");
        assertThat(response.description()).isEqualTo("Teclado mecânico");
        assertThat(response.price()).isEqualByComparingTo(BigDecimal.valueOf(250));
        assertThat(response.stockQuantity()).isEqualTo(8);
        assertThat(response.active()).isTrue();
        assertThat(productRepository.saveWasCalled()).isTrue();
        assertThat(productRepository.savedProduct()).isSameAs(product);
    }

    @Test
    void shouldThrowErrorWhenProductDoesNotExist() {
        FakeProductRepository productRepository = new FakeProductRepository(null);
        UpdateProductService service = new UpdateProductService(productRepository);
        UpdateProductCommand command = new UpdateProductCommand(
                UUID.randomUUID(),
                "Teclado",
                "Teclado mecânico",
                BigDecimal.valueOf(250),
                8
        );

        assertThatThrownBy(() -> service.update(command))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Produto não encontrado.");

        assertThat(productRepository.saveWasCalled()).isFalse();
    }

    @Test
    void shouldNotSaveProductWhenNameIsBlank() {
        UpdateProductCommand command = new UpdateProductCommand(
                UUID.randomUUID(),
                " ",
                "Teclado mecânico",
                BigDecimal.valueOf(250),
                8
        );

        assertInvalidCommandDoesNotSave(command);
    }

    @Test
    void shouldNotSaveProductWhenPriceIsNull() {
        UpdateProductCommand command = new UpdateProductCommand(
                UUID.randomUUID(),
                "Teclado",
                "Teclado mecânico",
                null,
                8
        );

        assertInvalidCommandDoesNotSave(command);
    }

    @Test
    void shouldNotSaveProductWhenStockIsNegative() {
        UpdateProductCommand command = new UpdateProductCommand(
                UUID.randomUUID(),
                "Teclado",
                "Teclado mecânico",
                BigDecimal.valueOf(250),
                -1
        );

        assertInvalidCommandDoesNotSave(command);
    }

    private void assertInvalidCommandDoesNotSave(UpdateProductCommand command) {
        Product product = createValidProduct();
        FakeProductRepository productRepository = new FakeProductRepository(product);
        UpdateProductService service = new UpdateProductService(productRepository);

        UpdateProductCommand commandWithExistingProductId = new UpdateProductCommand(
                product.getId(),
                command.name(),
                command.description(),
                command.price(),
                command.stockQuantity()
        );

        assertThatThrownBy(() -> service.update(commandWithExistingProductId))
                .isInstanceOf(DomainException.class);

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
