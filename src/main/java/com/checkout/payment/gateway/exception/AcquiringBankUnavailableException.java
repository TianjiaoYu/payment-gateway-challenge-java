package com.checkout.payment.gateway.exception;

/**
 * Thrown when the acquiring bank cannot be reached or returns an unexpected error
 * (e.g. connection refused, timeout, 5xx status).
 * <p>
 * Translated by {@link CommonExceptionHandler} into an HTTP 502 (Bad Gateway) response
 * so the merchant can distinguish an infrastructure failure from a validation or
 * business-level decline.
 */
public class AcquiringBankUnavailableException extends RuntimeException {

  /**
   * Create the exception with a diagnostic message and the underlying cause.
   *
   * @param message short description of what went wrong when calling the bank
   * @param cause   the underlying exception thrown by the HTTP client
   */
  public AcquiringBankUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
