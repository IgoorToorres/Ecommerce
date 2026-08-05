package com.ecommerce.application.order.service;

import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.application.order.mapper.OrderResponseMapper;
import com.ecommerce.application.order.repository.OrderRepository;
import com.ecommerce.application.order.response.OrderResponse;
import com.ecommerce.domain.order.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetOrderByIdService {
    private final OrderRepository orderRepository;
    private final OrderResponseMapper orderResponseMapper;

    public GetOrderByIdService(
            OrderRepository orderRepository,
            OrderResponseMapper orderResponseMapper
    ){
        this.orderRepository = orderRepository;
        this.orderResponseMapper = orderResponseMapper;
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id){
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        return orderResponseMapper.toResponse(order);
    }

}
