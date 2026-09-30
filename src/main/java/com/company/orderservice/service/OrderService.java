package com.company.orderservice.service;

import com.company.orderservice.client.UserClient;
import com.company.orderservice.dto.CreateOrderRequest;
import com.company.orderservice.dto.OrderResponse;
import com.company.orderservice.dto.UserDto;
import com.company.orderservice.entity.Order;
import com.company.orderservice.entity.OrderItem;
import com.company.orderservice.event.OrderCompensationEvent;
import com.company.orderservice.event.OrderCreatedEvent;
import com.company.orderservice.event.OrderSagaEvent;
import com.company.orderservice.kafka.OrderCompensationProducer;
import com.company.orderservice.kafka.OrderEventProducer;
import com.company.orderservice.kafka.OrderSagaProducer;
import com.company.orderservice.repository.OrderItemRepository;
import com.company.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserClient userClient;
    private final OrderEventProducer orderEventProducer;
    private final OrderCompensationProducer orderCompensationProducer;
    private final OrderSagaProducer orderSagaProducer;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        try {
            UserDto user = userClient.getUserById(request.getUserId());

            if (user == null) {
                throw new RuntimeException(
                        "User not found with id: " + request.getUserId()
                );
            }

        } catch (Exception ex) {
            throw new RuntimeException(
                    "Validation failed: User does not exist in user-service with id: "
                            + request.getUserId()
            );
        }

        Order order = Order.builder()
                .userId(request.getUserId())
                .productName(request.getProductName())
                .quantity(request.getQuantity())
                .amount(request.getAmount())
                .status("PENDING")
                .build();

        Order saved = orderRepository.save(order);

        OrderItem orderItem = OrderItem.builder()
                .orderId(saved.getId())
                .productName(request.getProductName())
                .quantity(request.getQuantity())
                .amount(request.getAmount())
                .build();

        orderItemRepository.save(orderItem);

        OrderCreatedEvent event = new OrderCreatedEvent(
                saved.getId(),
                saved.getUserId(),
                saved.getAmount(),
                "ORDER_CREATED"
        );

        orderEventProducer.publishOrderCreatedEvent(event);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found with id: " + id
                        )
                );

        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {

        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, String status) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found with id: " + id
                        )
                );

        order.setStatus(status);

        return mapToResponse(
                orderRepository.save(order)
        );
    }

    @Transactional
    public void cancelOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found with id: " + id
                        )
                );

        orderRepository.delete(order);
    }

    public OrderResponse requestCompensation(
            Long id,
            String reason) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found with id: " + id
                        )
                );

        OrderCompensationEvent compensationEvent =
                new OrderCompensationEvent(
                        order.getId(),
                        reason,
                        "ORDER_COMPENSATION"
                );

        orderCompensationProducer.publishCompensationEvent(
                compensationEvent
        );

        return mapToResponse(order);
    }

    /**
     * Starts a Saga process for an existing order.
     */
    public OrderResponse requestSaga(
            Long id,
            boolean simulateFailure) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found with id: " + id
                        )
                );

        String eventType;
        String status;
        String reason = null;

        if (simulateFailure) {

            eventType = "SAGA_FAILED";
            status = "FAILED";
            reason = "Saga test failure";

        } else {

            eventType = "SAGA_SUCCESS";
            status = "PROCESSING";
        }

        OrderSagaEvent sagaEvent =
                new OrderSagaEvent(
                        order.getId(),
                        order.getUserId(),
                        order.getAmount(),
                        eventType,
                        status,
                        reason
                );

        orderSagaProducer.publishSagaEvent(sagaEvent);

        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {

        UserDto user = userClient.getUserById(
                order.getUserId()
        );

        if (user == null) {
            return OrderResponse.builder()
                    .id(order.getId())
                    .userId(order.getUserId())
                    .productName(order.getProductName())
                    .quantity(order.getQuantity())
                    .amount(order.getAmount())
                    .status(order.getStatus())
                    .createdAt(order.getCreatedAt())
                    .user(null)
                    .message("User service temporarily unavailable")
                    .build();
        }

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .amount(order.getAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .user(user)
                .message(null)
                .build();
    }
}