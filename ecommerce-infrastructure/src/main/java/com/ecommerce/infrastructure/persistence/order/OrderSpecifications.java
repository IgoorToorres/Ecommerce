package com.ecommerce.infrastructure.persistence.order;

import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.order.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    public static Specification<Order> customerIdEquals(UUID customerId) {
        return (root, query, criteriaBuilder) -> {
            if (customerId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("customerId"), customerId);
        };
    }

    public static Specification<Order> statusEquals(OrderStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("status"), status);
        };
    }
}
