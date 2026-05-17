package com.checkout.payment.gateway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.client.AcquiringBankClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.exception.PaymentValidationException;
import com.checkout.payment.gateway.model.BankResponse;
import com.checkout.payment.gateway.model.GetPaymentResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link PaymentGatewayService} with mocked repository and bank client.
 * Validation rules are exercised by toggling individual fields on a known-good request.
 */
@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceUnitTest {

  @Mock
  private PaymentsRepository paymentsRepository;

  @Mock
  private AcquiringBankClient acquiringBankClient;

  @InjectMocks
  private PaymentGatewayService service;

  /**
   * Returns a request that passes every validator. Tests mutate one field at a time
   * to assert a specific validator triggers.
   */
  private PostPaymentRequest validRequest() {
    PostPaymentRequest req = new PostPaymentRequest();
    req.setCardNumber("4111111111111111");
    req.setExpiryMonth(12);
    req.setExpiryYear(YearMonth.now().getYear() + 1);
    req.setCurrency("USD");
    req.setAmount(100);
    req.setCvv("123");
    return req;
  }

  private BankResponse bankResponse(boolean authorized) {
    BankResponse r = new BankResponse();
    r.setAuthorized(authorized);
    return r;
  }

  @Test
  void getPaymentById_returnsResponse_whenFound() {
    UUID id = UUID.randomUUID();
    PostPaymentResponse stored = new PostPaymentResponse();
    stored.setId(id);
    stored.setStatus(PaymentStatus.AUTHORIZED);
    stored.setCardNumberLastFour("1111");
    stored.setExpiryMonth(12);
    stored.setExpiryYear(2030);
    stored.setCurrency("USD");
    stored.setAmount(100);
    when(paymentsRepository.get(id)).thenReturn(Optional.of(stored));

    GetPaymentResponse response = service.getPaymentById(id);

    assertThat(response.getId()).isEqualTo(id);
    assertThat(response.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(response.getCardNumberLastFour()).isEqualTo("1111");
    assertThat(response.getAmount()).isEqualTo(100);
    assertThat(response.getCurrency()).isEqualTo("USD");
  }

  @Test
  void getPaymentById_throwsNotFound_whenMissing() {
    UUID id = UUID.randomUUID();
    when(paymentsRepository.get(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getPaymentById(id))
        .isInstanceOf(PaymentNotFoundException.class)
        .hasMessageContaining(id.toString());
  }

  @Test
  void processPayment_authorized_persistsAndReturnsAuthorized() {
    when(acquiringBankClient.processPayment(any())).thenReturn(bankResponse(true));

    PostPaymentResponse response = service.processPayment(validRequest());

    assertThat(response.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(response.getCardNumberLastFour()).isEqualTo("1111");
    assertThat(response.getId()).isNotNull();

    ArgumentCaptor<PostPaymentResponse> captor = ArgumentCaptor.forClass(PostPaymentResponse.class);
    verify(paymentsRepository).add(captor.capture());
    assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(captor.getValue().getId()).isEqualTo(response.getId());
  }

  @Test
  void processPayment_declined_persistsAndReturnsDeclined() {
    when(acquiringBankClient.processPayment(any())).thenReturn(bankResponse(false));

    PostPaymentResponse response = service.processPayment(validRequest());

    assertThat(response.getStatus()).isEqualTo(PaymentStatus.DECLINED);
    verify(paymentsRepository).add(any());
  }

  @Test
  void processPayment_masksCardNumberToLastFour() {
    when(acquiringBankClient.processPayment(any())).thenReturn(bankResponse(true));

    PostPaymentRequest req = validRequest();
    req.setCardNumber("4242424242424242");

    PostPaymentResponse response = service.processPayment(req);

    assertThat(response.getCardNumberLastFour()).isEqualTo("4242");
  }

  @Test
  void processPayment_throwsValidation_whenCardNumberNull() {
    PostPaymentRequest req = validRequest();
    req.setCardNumber(null);

    assertThatThrownBy(() -> service.processPayment(req))
        .isInstanceOf(PaymentValidationException.class)
        .hasMessage("Invalid card number");
    verify(acquiringBankClient, never()).processPayment(any());
  }

  @Test
  void processPayment_throwsValidation_whenCardNumberTooShort() {
    PostPaymentRequest req = validRequest();
    req.setCardNumber("1234567890123"); // 13 digits

    assertThatThrownBy(() -> service.processPayment(req))
        .isInstanceOf(PaymentValidationException.class)
        .hasMessage("Invalid card number");
  }

  @Test
  void processPayment_throwsValidation_whenCardNumberContainsNonDigits() {
    PostPaymentRequest req = validRequest();
    req.setCardNumber("4111-1111-1111-1111");

    assertThatThrownBy(() -> service.processPayment(req))
        .isInstanceOf(PaymentValidationException.class)
        .hasMessage("Invalid card number");
  }

  @Test
  void processPayment_throwsValidation_whenExpiryMonthOutOfRange() {
    PostPaymentRequest req = validRequest();
    req.setExpiryMonth(13);

    assertThatThrownBy(() -> service.processPayment(req))
        .isInstanceOf(PaymentValidationException.class)
        .hasMessage("Invalid expiry month");
  }

  @Test
  void processPayment_throwsValidation_whenCardExpired() {
    PostPaymentRequest req = validRequest();
    req.setExpiryMonth(1);
    req.setExpiryYear(2000);

    assertThatThrownBy(() -> service.processPayment(req))
        .isInstanceOf(PaymentValidationException.class)
        .hasMessage("Card has expired");
  }

  @Test
  void processPayment_throwsValidation_whenCurrencyUnsupported() {
    PostPaymentRequest req = validRequest();
    req.setCurrency("JPY");

    assertThatThrownBy(() -> service.processPayment(req))
        .isInstanceOf(PaymentValidationException.class)
        .hasMessage("Unsupported currency");
  }

  @Test
  void processPayment_throwsValidation_whenCurrencyNull() {
    PostPaymentRequest req = validRequest();
    req.setCurrency(null);

    assertThatThrownBy(() -> service.processPayment(req))
        .isInstanceOf(PaymentValidationException.class)
        .hasMessage("Unsupported currency");
  }

  @Test
  void processPayment_throwsValidation_whenAmountZero() {
    PostPaymentRequest req = validRequest();
    req.setAmount(0);

    assertThatThrownBy(() -> service.processPayment(req))
        .isInstanceOf(PaymentValidationException.class)
        .hasMessage("Invalid amount");
  }

  @Test
  void processPayment_throwsValidation_whenAmountNegative() {
    PostPaymentRequest req = validRequest();
    req.setAmount(-1);

    assertThatThrownBy(() -> service.processPayment(req))
        .isInstanceOf(PaymentValidationException.class)
        .hasMessage("Invalid amount");
  }

  @Test
  void processPayment_throwsValidation_whenCvvTooShort() {
    PostPaymentRequest req = validRequest();
    req.setCvv("12");

    assertThatThrownBy(() -> service.processPayment(req))
        .isInstanceOf(PaymentValidationException.class)
        .hasMessage("Invalid CVV");
  }

  @Test
  void processPayment_throwsValidation_whenCvvNull() {
    PostPaymentRequest req = validRequest();
    req.setCvv(null);

    assertThatThrownBy(() -> service.processPayment(req))
        .isInstanceOf(PaymentValidationException.class)
        .hasMessage("Invalid CVV");
  }

  @Test
  void processPayment_doesNotCallBank_whenValidationFails() {
    PostPaymentRequest req = validRequest();
    req.setAmount(-1);

    assertThatThrownBy(() -> service.processPayment(req))
        .isInstanceOf(PaymentValidationException.class);
    verify(acquiringBankClient, never()).processPayment(any());
    verify(paymentsRepository, never()).add(any());
  }
}
