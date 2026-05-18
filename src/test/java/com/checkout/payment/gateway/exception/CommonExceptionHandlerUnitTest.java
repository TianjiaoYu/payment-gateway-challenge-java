package com.checkout.payment.gateway.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.ErrorResponse;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for {@link CommonExceptionHandler} verifying each domain exception is
 * mapped to the expected HTTP status and response body.
 */
class CommonExceptionHandlerUnitTest {

  private final CommonExceptionHandler handler = new CommonExceptionHandler();

  @Test
  void handleValidation_returns200_withRejectedStatus() {
    ResponseEntity<PostPaymentResponse> response =
        handler.handleValidation(new PaymentValidationException("Invalid CVV"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(PaymentStatus.REJECTED);
  }

  @Test
  void handleNotFound_returns404_withNotFoundMessage() {
    ResponseEntity<ErrorResponse> response =
        handler.handleNotFound(new PaymentNotFoundException("No payment found with id 123"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Payment not found");
  }

  @Test
  void handleBankUnavailable_returns502_withRetryMessage() {
    ResponseEntity<ErrorResponse> response =
        handler.handleBankUnavailable(
            new AcquiringBankUnavailableException("bank down", new RuntimeException("boom")));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage())
        .isEqualTo("Acquiring bank is currently unavailable. Please try again later.");
  }

  @Test
  void handleUnexpected_returns500_withGenericMessage() {
    ResponseEntity<ErrorResponse> response =
        handler.handleUnexpected(new RuntimeException("boom"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred.");
  }
}
