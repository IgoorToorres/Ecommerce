package com.ecommerce.infrastructure.persistence.order;

import com.ecommerce.application.order.repository.OrderRepository;
import com.ecommerce.application.order.response.PageResponse;
import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
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

    @Override
    public PageResponse<Order> findAll(UUID customerId, OrderStatus status, int page, int size) {
        Specification<Order> specification = Specification
                .where(OrderSpecifications.customerIdEquals(customerId))
                .and(OrderSpecifications.statusEquals(status));

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Order> orderPage = orderJpaRepository.findAll(specification, pageRequest);

        return new PageResponse<>(
                orderPage.getContent(),
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );
    }


}
