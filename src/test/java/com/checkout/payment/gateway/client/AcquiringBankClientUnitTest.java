package com.checkout.payment.gateway.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.exception.AcquiringBankUnavailableException;
import com.checkout.payment.gateway.model.BankResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Unit tests for {@link AcquiringBankClient} verifying both the happy path and the
 * mapping of every RestTemplate failure into {@link AcquiringBankUnavailableException}.
 */
@ExtendWith(MockitoExtension.class)
class AcquiringBankClientUnitTest {

  private static final String ACQUIRING_BANK_URL = "http://localhost:8080/payments";

  @Mock
  private RestTemplate restTemplate;

  private AcquiringBankClient client;

  @BeforeEach
  void setUp() {
    client = new AcquiringBankClient(restTemplate);
  }

  @Test
  void processPayment_returnsBankResponse_onSuccess() {
    BankResponse bankResponse = new BankResponse();
    bankResponse.setAuthorized(true);
    bankResponse.setAuthorizationCode("abc-123");
    when(restTemplate.postForObject(eq(ACQUIRING_BANK_URL), any(), eq(BankResponse.class)))
        .thenReturn(bankResponse);

    BankResponse result = client.processPayment(new PostPaymentRequest());

    assertThat(result.isAuthorized()).isTrue();
    assertThat(result.getAuthorizationCode()).isEqualTo("abc-123");
  }

  @Test
  void processPayment_wrapsConnectionFailure_asBankUnavailable() {
    when(restTemplate.postForObject(eq(ACQUIRING_BANK_URL), any(), eq(BankResponse.class)))
        .thenThrow(new ResourceAccessException("Connection refused"));

    assertThatThrownBy(() -> client.processPayment(new PostPaymentRequest()))
        .isInstanceOf(AcquiringBankUnavailableException.class)
        .hasCauseInstanceOf(ResourceAccessException.class);
  }

  @Test
  void processPayment_wraps5xx_asBankUnavailable() {
    when(restTemplate.postForObject(eq(ACQUIRING_BANK_URL), any(), eq(BankResponse.class)))
        .thenThrow(HttpServerErrorException.create(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Service Unavailable",
            HttpHeaders.EMPTY,
            new byte[0],
            null));

    assertThatThrownBy(() -> client.processPayment(new PostPaymentRequest()))
        .isInstanceOf(AcquiringBankUnavailableException.class)
        .hasCauseInstanceOf(HttpServerErrorException.class);
  }
}
