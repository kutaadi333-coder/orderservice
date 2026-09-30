package com.company.orderservice.config;

import com.company.orderservice.event.OrderCompensationEvent;
import com.company.orderservice.event.OrderCreatedEvent;
import com.company.orderservice.event.OrderSagaEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    private Map<String, Object> producerProperties() {

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "192.168.177.99:9092"
        );

        properties.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        properties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JacksonJsonSerializer.class
        );

        return properties;
    }

    @Bean
    public ProducerFactory<String, OrderCreatedEvent>
    producerFactory() {

        return new DefaultKafkaProducerFactory<>(
                producerProperties()
        );
    }

    @Bean
    public KafkaTemplate<String, OrderCreatedEvent>
    kafkaTemplate(
            ProducerFactory<String, OrderCreatedEvent>
                    producerFactory) {

        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, OrderCompensationEvent>
    compensationProducerFactory() {

        return new DefaultKafkaProducerFactory<>(
                producerProperties()
        );
    }

    @Bean
    public KafkaTemplate<String, OrderCompensationEvent>
    compensationKafkaTemplate(
            ProducerFactory<String, OrderCompensationEvent>
                    compensationProducerFactory) {

        return new KafkaTemplate<>(
                compensationProducerFactory
        );
    }

    @Bean
    public ProducerFactory<String, OrderSagaEvent>
    sagaProducerFactory() {

        return new DefaultKafkaProducerFactory<>(
                producerProperties()
        );
    }

    @Bean
    public KafkaTemplate<String, OrderSagaEvent>
    sagaKafkaTemplate(
            ProducerFactory<String, OrderSagaEvent>
                    sagaProducerFactory) {

        return new KafkaTemplate<>(
                sagaProducerFactory
        );
    }
}