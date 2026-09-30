package com.company.orderservice.kafka;

import com.company.orderservice.entity.Order;
import com.company.orderservice.event.OrderCompensationEvent;
import com.company.orderservice.event.OrderSagaEvent;
import com.company.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderSagaConsumer {

    private final OrderRepository orderRepository;
    private final OrderCompensationProducer orderCompensationProducer;

    @KafkaListener(
            topics = "order-saga",
            groupId = "order-saga-group"
    )
    @Transactional
    public void handleSagaEvent(OrderSagaEvent event) {

        System.out.println(
                "Saga event received for Order ID: "
                        + event.getOrderId()
                        + ", Event Type: "
                        + event.getEventType()
        );

        Order order = orderRepository
                .findById(event.getOrderId())
                .orElse(null);

        if (order == null) {
            System.out.println(
                    "Order not found for Saga processing: "
                            + event.getOrderId()
            );
            return;
        }

        if ("SAGA_SUCCESS".equals(event.getEventType())) {

            order.setStatus("COMPLETED");

            orderRepository.save(order);

            System.out.println(
                    "Saga completed successfully for Order ID: "
                            + event.getOrderId()
            );

        } else if ("SAGA_FAILED".equals(event.getEventType())) {

            System.out.println(
                    "Saga failed for Order ID: "
                            + event.getOrderId()
                            + ". Starting compensation."
            );

            OrderCompensationEvent compensationEvent =
                    new OrderCompensationEvent(
                            event.getOrderId(),
                            event.getReason(),
                            "ORDER_COMPENSATION"
                    );

            orderCompensationProducer.publishCompensationEvent(
                    compensationEvent
            );
        }
    }
}