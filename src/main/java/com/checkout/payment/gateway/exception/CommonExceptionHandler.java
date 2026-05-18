package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.ErrorResponse;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler mapping domain exceptions to HTTP responses.
 *
 * Validation failures return a payment-shaped response with status=Rejected so the
 * merchant can parse one response shape across the three outcomes (Authorized,
 * Declined, Rejected). True errors (not found, bank down, unexpected) return an
 * {@link ErrorResponse} with an appropriate HTTP status.
 */
@ControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  /**
   * Converts a {@link PaymentValidationException} into an HTTP 200 response carrying a
   * {@link PostPaymentResponse} with status {@link PaymentStatus#REJECTED}. This keeps the
   * response shape uniform with Authorized/Declined outcomes.
   *
   * @param ex the validation failure raised by the service layer
   * @return 200 OK with a Rejected payment response
   */
  @ExceptionHandler(PaymentValidationException.class)
  public ResponseEntity<PostPaymentResponse> handleValidation(PaymentValidationException ex) {
    LOG.warn("Payment rejected by gateway: {}", ex.getMessage());
    PostPaymentResponse response = new PostPaymentResponse();
    response.setStatus(PaymentStatus.REJECTED);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  /**
   * Converts a {@link PaymentNotFoundException} into an HTTP 404 response with a
   * generic "Payment not found" body. Logged at INFO since lookup misses are normal traffic.
   *
   * @param ex the not-found signal raised by the service layer
   * @return 404 Not Found with a generic error body
   */
  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(PaymentNotFoundException ex) {
    LOG.info("Payment lookup miss: {}", ex.getMessage());
    return new ResponseEntity<>(new ErrorResponse("Payment not found"), HttpStatus.NOT_FOUND);
  }

  /**
   * Converts an {@link AcquiringBankUnavailableException} into an HTTP 502 (Bad Gateway)
   * response. Logged at ERROR since bank-availability problems should page operators.
   *
   * @param ex the failure raised by the acquiring bank client
   * @return 502 Bad Gateway with a retry-suggestion error body
   */
  @ExceptionHandler(AcquiringBankUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleBankUnavailable(AcquiringBankUnavailableException ex) {
    LOG.error("Acquiring bank unavailable", ex);
    return new ResponseEntity<>(
        new ErrorResponse("Acquiring bank is currently unavailable. Please try again later."),
        HttpStatus.BAD_GATEWAY);
  }

  /**
   * Catch-all for any unexpected exception, returning HTTP 500 with a generic body so we
   * never leak stack traces to merchants. Logged at ERROR with the full exception.
   *
   * @param ex any exception not matched by a more specific handler above
   * @return 500 Internal Server Error with a generic error body
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
    LOG.error("Unhandled exception", ex);
    return new ResponseEntity<>(
        new ErrorResponse("An unexpected error occurred."),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
