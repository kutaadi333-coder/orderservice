package com.company.orderservice.kafka;

import com.company.orderservice.event.OrderCompensationEvent;
import com.company.orderservice.entity.Order;
import com.company.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderCompensationConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "order-compensation",
            groupId = "order-compensation-group"
    )
    @Transactional
    public void handleCompensation(
            OrderCompensationEvent event) {

        System.out.println(
                "Compensation received for Order ID: "
                        + event.getOrderId()
        );

        Order order = orderRepository
                .findById(event.getOrderId())
                .orElse(null);

        if (order == null) {
            System.out.println(
                    "Order not found for compensation: "
                            + event.getOrderId()
            );
            return;
        }

        order.setStatus("CANCELLED");

        orderRepository.save(order);

        System.out.println(
                "Order " + event.getOrderId()
                        + " compensated successfully. Reason: "
                        + event.getReason()
        );
    }
}