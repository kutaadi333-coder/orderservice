package com.company.orderservice.kafka;

import com.company.orderservice.event.OrderSagaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderSagaProducer {

    private static final String TOPIC = "order-saga";

    private final KafkaTemplate<String, OrderSagaEvent> kafkaTemplate;

    public void publishSagaEvent(OrderSagaEvent event) {

        kafkaTemplate.send(
                TOPIC,
                String.valueOf(event.getOrderId()),
                event
        );
    }
}