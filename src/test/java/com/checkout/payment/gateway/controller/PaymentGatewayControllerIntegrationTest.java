package com.checkout.payment.gateway.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.client.AcquiringBankClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.AcquiringBankUnavailableException;
import com.checkout.payment.gateway.model.BankResponse;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for {@link PaymentGatewayController} using the full Spring context.
 * The {@link AcquiringBankClient} is mocked so tests do not depend on the external bank
 * simulator running.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private PaymentsRepository paymentsRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private AcquiringBankClient acquiringBankClient;

  /**
   * Returns a payment request body that passes every validator.
   */
  private Map<String, Object> validRequestBody() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("card_number", "4111111111111111");
    body.put("expiry_month", 12);
    body.put("expiry_year", YearMonth.now().getYear() + 1);
    body.put("currency", "USD");
    body.put("amount", 100);
    body.put("cvv", "123");
    return body;
  }

  private String json(Map<String, Object> body) throws Exception {
    return objectMapper.writeValueAsString(body);
  }

  @Test
  void whenPaymentWithIdExistThenCorrectPaymentIsReturned() throws Exception {
    PostPaymentResponse payment = new PostPaymentResponse();
    payment.setId(UUID.randomUUID());
    payment.setAmount(10);
    payment.setCurrency("USD");
    payment.setStatus(PaymentStatus.AUTHORIZED);
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2027);
    payment.setCardNumberLastFour("4321");
    paymentsRepository.add(payment);

    mvc.perform(get("/api/payment/" + payment.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(payment.getStatus().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(payment.getCardNumberLastFour()))
        .andExpect(jsonPath("$.expiryMonth").value(payment.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(payment.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(payment.getCurrency()))
        .andExpect(jsonPath("$.amount").value(payment.getAmount()));
  }

  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {
    mvc.perform(get("/api/payment/" + UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Payment not found"));
  }

  @Test
  void postPayment_whenBankAuthorizes_returnsAuthorizedWithMaskedCard() throws Exception {
    BankResponse bankResponse = new BankResponse();
    bankResponse.setAuthorized(true);
    when(acquiringBankClient.processPayment(any())).thenReturn(bankResponse);

    mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(validRequestBody())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.cardNumberLastFour").value("1111"))
        .andExpect(jsonPath("$.amount").value(100))
        .andExpect(jsonPath("$.currency").value("USD"));
  }

  @Test
  void postPayment_whenBankDeclines_returnsDeclined() throws Exception {
    BankResponse bankResponse = new BankResponse();
    bankResponse.setAuthorized(false);
    when(acquiringBankClient.processPayment(any())).thenReturn(bankResponse);

    Map<String, Object> body = validRequestBody();
    body.put("card_number", "4111111111111112");

    mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(body)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Declined"))
        .andExpect(jsonPath("$.cardNumberLastFour").value("1112"));
  }

  @Test
  void postPayment_whenCardInvalid_returnsRejectedAndSkipsBankCall() throws Exception {
    Map<String, Object> body = validRequestBody();
    body.put("card_number", "abc");

    mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(body)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Rejected"));

    verify(acquiringBankClient, never()).processPayment(any());
  }

  @Test
  void postPayment_whenCurrencyUnsupported_returnsRejected() throws Exception {
    Map<String, Object> body = validRequestBody();
    body.put("currency", "JPY");

    mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(body)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Rejected"));
  }

  @Test
  void postPayment_whenAmountNonPositive_returnsRejected() throws Exception {
    Map<String, Object> body = validRequestBody();
    body.put("amount", 0);

    mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(body)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Rejected"));
  }

  @Test
  void postPayment_whenBankUnavailable_returns502() throws Exception {
    when(acquiringBankClient.processPayment(any()))
        .thenThrow(new AcquiringBankUnavailableException("bank down", new RuntimeException()));

    mvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(validRequestBody())))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.message")
            .value("Acquiring bank is currently unavailable. Please try again later."));
  }
}
