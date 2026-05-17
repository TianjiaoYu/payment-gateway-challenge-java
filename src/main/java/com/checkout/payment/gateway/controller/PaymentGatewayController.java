package com.checkout.payment.gateway.controller;

import com.checkout.payment.gateway.model.GetPaymentResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing endpoints to process payments and retrieve payment details.
 */
@RestController
@RequestMapping("/api")
public class PaymentGatewayController {

  private final PaymentGatewayService paymentGatewayService;

  /**
   * Controller constructor with injected service.
   */
  public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
    this.paymentGatewayService = paymentGatewayService;
  }

  /**
   * Retrieves a payment by id.
   * @param id payment UUID
   * @return HTTP 200 with payment details or a 404 handled by the exception handler.
   */
  @GetMapping("/payment/{id}")
  public ResponseEntity<GetPaymentResponse> getPaymentById(@PathVariable UUID id) {
    return new ResponseEntity<>(paymentGatewayService.getPaymentById(id), HttpStatus.OK);
  }

  /**
   * Processes a payment request and returns the stored payment response.
   * @param paymentRequest incoming payment payload
   */
  @PostMapping("/payments")
  public ResponseEntity<PostPaymentResponse> processPayment(@RequestBody PostPaymentRequest paymentRequest) {
    return new ResponseEntity<>(paymentGatewayService.processPayment(paymentRequest), HttpStatus.OK);
  }
}
