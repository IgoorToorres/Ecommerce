package com.ecommerce.domain.product;

import com.ecommerce.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

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
void shouldNotCreateProductWithBlankName(){
        assertThatThrownBy(() -> new Product(
                " ",
                "Produto sem nome",
                BigDecimal.valueOf(100),
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
        Product product = new Product(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                5
        );

        product.increaseStock(3);

        assertThat(product.getStockQuantity()).isEqualTo(8);
    }

    @Test
    void shouldNotIncreaseStockWithZeroQuantity() {
        Product product = new Product(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                5
        );

        assertThatThrownBy(() -> product.increaseStock(0))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldDecreaseStock() {
        Product product = new Product(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                5
        );

        product.decreaseStock(2);

        assertThat(product.getStockQuantity()).isEqualTo(3);
    }

    @Test
    void shouldNotDecreaseStockBeyondAvailableQuantity() {
        Product product = new Product(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                5
        );

        assertThatThrownBy(() -> product.decreaseStock(6))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldChangePrice() {
        Product product = new Product(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                5
        );

        product.changePrice(BigDecimal.valueOf(120));

        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(120));
    }

    @Test
    void shouldDeactivateProduct() {
        Product product = new Product(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                5
        );

        product.deactivate();

        assertThat(product.isActive()).isFalse();
    }

    @Test
    void shouldActivateProduct() {
        Product product = new Product(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                5
        );

        product.deactivate();
        product.activate();

        assertThat(product.isActive()).isTrue();
    }

    @Test
    void shouldReturnTrueWhenHasAvailableStock() {
        Product product = new Product(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                5
        );

        assertThat(product.hasAvailableStock(3)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenDoesNotHaveAvailableStock() {
        Product product = new Product(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                5
        );

        assertThat(product.hasAvailableStock(6)).isFalse();
    }
}