package com.ecommerce.application.order.repository;

import com.ecommerce.application.order.response.PageResponse;
import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.order.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID id);
    List<Order> findAll();
    PageResponse<Order> findAll(UUID customerId, OrderStatus status, int page, int size);
}
