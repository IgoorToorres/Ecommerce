package com.ecommerce.application.payment.service;

import com.ecommerce.application.exception.ForbiddenException;
import com.ecommerce.application.exception.ResourceNotFoundException;
import com.ecommerce.application.order.repository.OrderRepository;
import com.ecommerce.application.payment.command.ProcessPaymentCommand;
import com.ecommerce.application.payment.mapper.PaymentResponseMapper;
import com.ecommerce.application.payment.repository.PaymentRepository;
import com.ecommerce.application.payment.response.PaymentResponse;
import com.ecommerce.domain.exception.DomainException;
import com.ecommerce.domain.order.Order;
import com.ecommerce.domain.order.OrderStatus;
import com.ecommerce.domain.payment.Payment;
import com.ecommerce.domain.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProcessPaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentResponseMapper paymentResponseMapper;

    public ProcessPaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            PaymentResponseMapper paymentResponseMapper){
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentResponseMapper = paymentResponseMapper;
    }

    @Transactional
    public PaymentResponse process(ProcessPaymentCommand command){
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado."));

        if(command.authenticatedUserRole() == UserRole.CUSTOMER && !order.getCustomerId().equals(command.authenticatedUserId())){
            throw new ForbiddenException("Você não tem permissão para pagar este pedido.");
        }

        return paymentRepository
                .findByOrderIdAndIdempotencyKey(command.orderId(), command.idempotencyKey())
                .map(paymentResponseMapper::toResponse)
                .orElseGet(() -> processNewPayment(command, order));

    }

    private PaymentResponse processNewPayment(ProcessPaymentCommand command, Order order){
        if(order.getStatus() != OrderStatus.PENDING_PAYMENT){
            throw new DomainException("O pedido só pode ser pago quando estiver aguardando pagamento.");
        }

        Payment payment = new Payment(
                order.getId(),
                command.method(),
                order.getTotalAmount(),
                command.idempotencyKey()
        );

        String externalReference = "SIMULATED-" + UUID.randomUUID();

        if(command.approved()){
            payment.approve(externalReference);
            order.markAsPaid();
        }else{
            payment.reject(externalReference);
        }

        Payment savedPayment = paymentRepository.save(payment);
        orderRepository.save(order);

        return paymentResponseMapper.toResponse(savedPayment);
    }
}
