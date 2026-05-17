package com.checkout.payment.gateway.exception;

/**
 * Thrown when a payment lookup by id returns no stored payment.
 * <p>
 * Translated by {@link CommonExceptionHandler} into an HTTP 404 response.
 */
public class PaymentNotFoundException extends RuntimeException {

  /**
   * Create the exception with a message identifying the missing payment.
   *
   * @param message human-readable description of the lookup miss
   */
  public PaymentNotFoundException(String message) {
    super(message);
  }
}
