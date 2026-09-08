package com.company.orderservice.service;

import com.company.orderservice.client.UserClient;
import com.company.orderservice.dto.CreateOrderRequest;
import com.company.orderservice.dto.OrderResponse;
import com.company.orderservice.dto.UserDto;
import com.company.orderservice.entity.Order;
import com.company.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;

    public OrderResponse createOrder(CreateOrderRequest request) {
        // Step 1: Validate that user exists in user-service via OpenFeign
        try {
            UserDto user = userClient.getUserById(request.getUserId());
            if (user == null) {
                throw new RuntimeException("User not found with id: " + request.getUserId());
            }
        } catch (Exception ex) {
            throw new RuntimeException("Validation failed: User does not exist in user-service with id: " + request.getUserId());
        }

        // Step 2: Persist the order only if user exists
        Order order = Order.builder()
                .userId(request.getUserId())
                .productName(request.getProductName())
                .quantity(request.getQuantity())
                .amount(request.getAmount())
                .status("PENDING")
                .build();

        Order saved = orderRepository.save(order);
        return mapToResponse(saved);
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return mapToResponse(order);
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        order.setStatus(status);
        return mapToResponse(orderRepository.save(order));
    }

    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        orderRepository.delete(order);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .amount(order.getAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}