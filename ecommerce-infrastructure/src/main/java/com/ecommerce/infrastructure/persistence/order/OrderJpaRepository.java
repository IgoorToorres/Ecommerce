package com.ecommerce.infrastructure.persistence.order;

import com.ecommerce.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface OrderJpaRepository extends
        JpaRepository<Order, UUID>,
        JpaSpecificationExecutor<Order>
{
}
