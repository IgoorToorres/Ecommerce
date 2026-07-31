package com.ecommerce.domain.product;

import com.ecommerce.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    void shouldCreateValidProduct() {
        Product product = new Product(
                "Teclado Mecânico",
                "Teclado RGB com switches blue",
                BigDecimal.valueOf(399.90),
                10
        );

        assertThat(product.getId()).isNotNull();
        assertThat(product.getName()).isEqualTo("Teclado Mecânico");
        assertThat(product.getDescription()).isEqualTo("Teclado RGB com switches blue");
        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(399.90));
        assertThat(product.getStockQuantity()).isEqualTo(10);
        assertThat(product.isActive()).isTrue();
        assertThat(product.getCreatedAt()).isNotNull();
        assertThat(product.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldNotCreateProductWithNullName() {
        assertThatThrownBy(() -> new Product(
                null,
                "Produto sem nome",
                BigDecimal.valueOf(100),
                5
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotCreateProductWithBlankName() {
        assertThatThrownBy(() -> new Product(
                " ",
                "Produto sem nome",
                BigDecimal.valueOf(100),
                5
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotCreateProductWithNameLongerThan150Characters() {
        String name = "a".repeat(151);

        assertThatThrownBy(() -> new Product(
                name,
                "Produto com nome muito grande",
                BigDecimal.valueOf(100),
                5
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotCreateProductWithNullPrice() {
        assertThatThrownBy(() -> new Product(
                "Mouse",
                "Mouse sem fio",
                null,
                5
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotCreateProductWithZeroPrice() {
        assertThatThrownBy(() -> new Product(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.ZERO,
                5
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotCreateProductWithNegativePrice() {
        assertThatThrownBy(() -> new Product(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(-1),
                5
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotCreateProductWithNegativeStock() {
        assertThatThrownBy(() -> new Product(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                -1
        )).isInstanceOf(DomainException.class);
    }

    @Test
    void shouldIncreaseStock() {
        Product product = createValidProduct();

        product.increaseStock(3);

        assertThat(product.getStockQuantity()).isEqualTo(8);
    }

    @Test
    void shouldNotIncreaseStockWithZeroQuantity() {
        Product product = createValidProduct();

        assertThatThrownBy(() -> product.increaseStock(0))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotIncreaseStockWithNegativeQuantity() {
        Product product = createValidProduct();

        assertThatThrownBy(() -> product.increaseStock(-1))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldDecreaseStock() {
        Product product = createValidProduct();

        product.decreaseStock(2);

        assertThat(product.getStockQuantity()).isEqualTo(3);
    }

    @Test
    void shouldNotDecreaseStockBeyondAvailableQuantity() {
        Product product = createValidProduct();

        assertThatThrownBy(() -> product.decreaseStock(6))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotDecreaseStockWithZeroQuantity() {
        Product product = createValidProduct();

        assertThatThrownBy(() -> product.decreaseStock(0))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldNotDecreaseStockWithNegativeQuantity() {
        Product product = createValidProduct();

        assertThatThrownBy(() -> product.decreaseStock(-1))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldChangePrice() {
        Product product = createValidProduct();

        product.changePrice(BigDecimal.valueOf(120));

        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(120));
    }

    @Test
    void shouldUpdateProductDetails() {
        Product product = createValidProduct();

        product.updateDetails(
                " Teclado ",
                "Teclado mecânico",
                BigDecimal.valueOf(250),
                8
        );

        assertThat(product.getName()).isEqualTo("Teclado");
        assertThat(product.getDescription()).isEqualTo("Teclado mecânico");
        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(250));
        assertThat(product.getStockQuantity()).isEqualTo(8);
    }

    @Test
    void shouldNotUpdateProductWithNullName() {
        Product product = createValidProduct();

        assertInvalidUpdateDoesNotChangeProduct(
                product,
                null,
                "Teclado mecânico",
                BigDecimal.valueOf(250),
                8
        );
    }

    @Test
    void shouldNotUpdateProductWithBlankName() {
        Product product = createValidProduct();

        assertInvalidUpdateDoesNotChangeProduct(
                product,
                " ",
                "Teclado mecânico",
                BigDecimal.valueOf(250),
                8
        );
    }

    @Test
    void shouldNotUpdateProductWithNameLongerThan150Characters() {
        Product product = createValidProduct();
        String name = "a".repeat(151);

        assertInvalidUpdateDoesNotChangeProduct(
                product,
                name,
                "Teclado mecânico",
                BigDecimal.valueOf(250),
                8
        );
    }

    @Test
    void shouldNotUpdateProductWithNullPrice() {
        Product product = createValidProduct();

        assertInvalidUpdateDoesNotChangeProduct(
                product,
                "Teclado",
                "Teclado mecânico",
                null,
                8
        );
    }

    @Test
    void shouldNotUpdateProductWithZeroPrice() {
        Product product = createValidProduct();

        assertInvalidUpdateDoesNotChangeProduct(
                product,
                "Teclado",
                "Teclado mecânico",
                BigDecimal.ZERO,
                8
        );
    }

    @Test
    void shouldNotUpdateProductWithNegativePrice() {
        Product product = createValidProduct();

        assertInvalidUpdateDoesNotChangeProduct(
                product,
                "Teclado",
                "Teclado mecânico",
                BigDecimal.valueOf(-1),
                8
        );
    }

    @Test
    void shouldNotUpdateProductWithNegativeStock() {
        Product product = createValidProduct();

        assertInvalidUpdateDoesNotChangeProduct(
                product,
                "Teclado",
                "Teclado mecânico",
                BigDecimal.valueOf(250),
                -1
        );
    }

    @Test
    void shouldUpdateUpdatedAtWhenUpdatingProductDetails() throws InterruptedException {
        Product product = createValidProduct();
        Instant previousUpdatedAt = product.getUpdatedAt();

        waitUntilClockAdvances();
        product.updateDetails(
                "Teclado",
                "Teclado mecânico",
                BigDecimal.valueOf(250),
                8
        );

        assertThat(product.getUpdatedAt()).isAfter(previousUpdatedAt);
    }

    @Test
    void shouldUpdateUpdatedAtWhenChangingPrice() throws InterruptedException {
        Product product = createValidProduct();
        Instant previousUpdatedAt = product.getUpdatedAt();

        waitUntilClockAdvances();
        product.changePrice(BigDecimal.valueOf(120));

        assertThat(product.getUpdatedAt()).isAfter(previousUpdatedAt);
    }

    @Test
    void shouldDeactivateProduct() {
        Product product = createValidProduct();

        product.deactivate();

        assertThat(product.isActive()).isFalse();
    }

    @Test
    void shouldActivateProduct() {
        Product product = createValidProduct();

        product.deactivate();
        product.activate();

        assertThat(product.isActive()).isTrue();
    }

    @Test
    void shouldReturnTrueWhenHasAvailableStock() {
        Product product = createValidProduct();

        assertThat(product.hasAvailableStock(3)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenDoesNotHaveAvailableStock() {
        Product product = createValidProduct();

        assertThat(product.hasAvailableStock(6)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenCheckingAvailableStockWithZeroQuantity() {
        Product product = createValidProduct();

        assertThat(product.hasAvailableStock(0)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenCheckingAvailableStockWithNegativeQuantity() {
        Product product = createValidProduct();

        assertThat(product.hasAvailableStock(-1)).isFalse();
    }

    @Test
    void shouldUpdateUpdatedAtWhenIncreasingStock() throws InterruptedException {
        Product product = createValidProduct();
        Instant previousUpdatedAt = product.getUpdatedAt();

        waitUntilClockAdvances();
        product.increaseStock(1);

        assertThat(product.getUpdatedAt()).isAfter(previousUpdatedAt);
    }

    @Test
    void shouldUpdateUpdatedAtWhenDecreasingStock() throws InterruptedException {
        Product product = createValidProduct();
        Instant previousUpdatedAt = product.getUpdatedAt();

        waitUntilClockAdvances();
        product.decreaseStock(1);

        assertThat(product.getUpdatedAt()).isAfter(previousUpdatedAt);
    }

    private Product createValidProduct() {
        return new Product(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                5
        );
    }

    private void assertInvalidUpdateDoesNotChangeProduct(
            Product product,
            String name,
            String description,
            BigDecimal price,
            int stockQuantity
    ) {
        String previousName = product.getName();
        String previousDescription = product.getDescription();
        BigDecimal previousPrice = product.getPrice();
        int previousStockQuantity = product.getStockQuantity();
        Instant previousUpdatedAt = product.getUpdatedAt();

        assertThatThrownBy(() -> product.updateDetails(name, description, price, stockQuantity))
                .isInstanceOf(DomainException.class);

        assertThat(product.getName()).isEqualTo(previousName);
        assertThat(product.getDescription()).isEqualTo(previousDescription);
        assertThat(product.getPrice()).isEqualByComparingTo(previousPrice);
        assertThat(product.getStockQuantity()).isEqualTo(previousStockQuantity);
        assertThat(product.getUpdatedAt()).isEqualTo(previousUpdatedAt);
    }

    private void waitUntilClockAdvances() throws InterruptedException {
        Thread.sleep(1);
    }
}
