package com.checkout.payment.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot application entry point for the Payment Gateway service.
 */
@SpringBootApplication
public class PaymentGatewayApplication {

  /**
   * Application main — starts the Spring context.
   */
  public static void main(String[] args) {
    SpringApplication.run(PaymentGatewayApplication.class, args);
  }

}
