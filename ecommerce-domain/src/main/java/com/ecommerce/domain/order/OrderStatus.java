package com.ecommerce.domain.order;

public enum OrderStatus {
    //Pedido criado, aguardando pagamento.
    PENDING_PAYMENT,

    //Pagamento aprovado.
    PAID,

    //Pedido em preparação.
    PREPARING,

    //Pedido enviado.
    SHIPPED,

    //Pedido entregue.
    DELIVERED,

    //Pedido cancelado.
    CANCELLED
}
