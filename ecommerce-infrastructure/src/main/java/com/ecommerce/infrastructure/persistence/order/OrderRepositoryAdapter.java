package com.ecommerce.infrastructure.persistence.order;

import com.ecommerce.application.order.repository.OrderRepository;
import com.ecommerce.domain.order.Order;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    public OrderRepositoryAdapter(OrderJpaRepository orderJpaRepository){
        this.orderJpaRepository = orderJpaRepository;
    }


    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orderJpaRepository.findById(id);
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAll();
    }
}
