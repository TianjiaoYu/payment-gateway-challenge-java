package com.checkout.payment.gateway.exception;

/**
 * Thrown when a payment request fails gateway-side validation and is rejected
 * before being forwarded to the acquiring bank.
 *
 * Translated by {@link CommonExceptionHandler} into an HTTP 200 response carrying
 * a {@link com.checkout.payment.gateway.model.PostPaymentResponse} with
 * {@link com.checkout.payment.gateway.enums.PaymentStatus#REJECTED} so the merchant
 * sees a single response shape across the Authorized, Declined, and Rejected outcomes.
 */
public class PaymentValidationException extends RuntimeException {

  /**
   * Create the exception with a short, merchant-safe reason (e.g. "Invalid CVV").
   *
   * @param message human-readable description of the validation failure
   */
  public PaymentValidationException(String message) {
    super(message);
  }
}
