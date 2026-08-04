package com.ecommerce.application.product.service;

import com.ecommerce.application.product.repository.ProductRepository;
import com.ecommerce.application.product.response.ProductResponse;
import com.ecommerce.domain.product.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ListProductsServiceTest {

    @Test
    void shouldListProducts() {
        Product mouse = new Product(
                "Mouse",
                "Mouse sem fio",
                BigDecimal.valueOf(100),
                5
        );
        Product keyboard = new Product(
                "Teclado",
                "Teclado mecânico",
                BigDecimal.valueOf(250),
                8
        );
        FakeProductRepository productRepository = new FakeProductRepository(List.of(mouse, keyboard));
        ListProductsService service = new ListProductsService(productRepository);

        List<ProductResponse> responses = service.findAll();

        assertThat(responses).hasSize(2);
        assertThat(responses.getFirst().id()).isEqualTo(mouse.getId());
        assertThat(responses.getFirst().name()).isEqualTo("Mouse");
        assertThat(responses.getFirst().description()).isEqualTo("Mouse sem fio");
        assertThat(responses.getFirst().price()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(responses.getFirst().stockQuantity()).isEqualTo(5);
        assertThat(responses.getFirst().active()).isTrue();

        assertThat(responses.get(1).id()).isEqualTo(keyboard.getId());
        assertThat(responses.get(1).name()).isEqualTo("Teclado");
        assertThat(responses.get(1).description()).isEqualTo("Teclado mecânico");
        assertThat(responses.get(1).price()).isEqualByComparingTo(BigDecimal.valueOf(250));
        assertThat(responses.get(1).stockQuantity()).isEqualTo(8);
        assertThat(responses.get(1).active()).isTrue();
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoProducts() {
        FakeProductRepository productRepository = new FakeProductRepository(List.of());
        ListProductsService service = new ListProductsService(productRepository);

        List<ProductResponse> responses = service.findAll();

        assertThat(responses).isEmpty();
    }

    private static final class FakeProductRepository implements ProductRepository {

        private final List<Product> products;

        private FakeProductRepository(List<Product> products) {
            this.products = products;
        }

        @Override
        public Product save(Product product) {
            return product;
        }

        @Override
        public Optional<Product> findById(UUID id) {
            return products.stream()
                    .filter(product -> product.getId().equals(id))
                    .findFirst();
        }

        @Override
        public List<Product> findAll() {
            return products;
        }
    }
}
