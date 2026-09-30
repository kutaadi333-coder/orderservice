package com.company.orderservice.config;

import feign.RetryableException;
import feign.Retryer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

public class UserServiceFeignConfig {

    @Bean
    public Retryer userServiceRetryer() {
        return new LoggingRetryer(1000, 3000, 3);
    }

    private static class LoggingRetryer extends Retryer.Default {

        private static final Logger logger =
                LoggerFactory.getLogger(LoggingRetryer.class);

        public LoggingRetryer(long period, long maxPeriod, int maxAttempts) {
            super(period, maxPeriod, maxAttempts);
        }

        @Override
        public void continueOrPropagate(RetryableException e) {

            logger.warn(
                    "Retry attempt for User Service failed: {}",
                    e.getMessage()
            );

            super.continueOrPropagate(e);
        }

        @Override
        public Retryer clone() {
            return new LoggingRetryer(1000, 3000, 3);
        }
    }
}