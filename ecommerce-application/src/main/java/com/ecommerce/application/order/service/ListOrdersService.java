package com.ecommerce.application.order.service;

import com.ecommerce.application.order.command.ListOrdersCommand;
import com.ecommerce.application.order.mapper.OrderResponseMapper;
import com.ecommerce.application.order.repository.OrderRepository;
import com.ecommerce.application.order.response.OrderResponse;
import com.ecommerce.application.order.response.PageResponse;
import com.ecommerce.domain.order.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListOrdersService {

    private final OrderRepository orderRepository;
    private final OrderResponseMapper orderResponseMapper;

    public ListOrdersService(
            OrderRepository orderRepository,
            OrderResponseMapper orderResponseMapper
    ){
        this.orderRepository = orderRepository;
        this.orderResponseMapper = orderResponseMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> findAll(ListOrdersCommand command){
        PageResponse<Order> orderPage = orderRepository.findAll(
                command.customerId(),
                command.status(),
                command.page(),
                command.size()
        );

        List<OrderResponse> content = orderResponseMapper.toResponseList(orderPage.content());

        return new PageResponse<>(
                content,
                orderPage.page(),
                orderPage.size(),
                orderPage.totalElements(),
                orderPage.totalPages()
        );
    }
}
