package com.checkout.payment.gateway.service;

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
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Core service that validates payment requests, delegates authorization to the
 * acquiring bank client, and persists retrieval-friendly payment responses.
 */
@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;
  private final AcquiringBankClient acquiringBankClient;

  private static final Set<String> SUPPORTED_CURRENCIES =
      Set.of("USD", "EUR", "GBP");

  /**
   * Construct service with required dependencies.
   */
  public PaymentGatewayService(PaymentsRepository paymentsRepository,
      AcquiringBankClient acquiringBankClient) {
    this.acquiringBankClient = acquiringBankClient;
    this.paymentsRepository = paymentsRepository;
  }

  /**
   * Retrieve a stored payment by id; throws PaymentNotFoundException if not found.
   */
  public GetPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to payment with ID {}", id);
    PostPaymentResponse paymentResponse = paymentsRepository.get(id)
        .orElseThrow(() -> new PaymentNotFoundException("No payment found with id " + id));

    return buildGetPaymentResponse(paymentResponse);
  }

  /**
   * Validates and processes a payment request, stores the result and returns the
   * processed payment response.
   */
  public PostPaymentResponse processPayment(PostPaymentRequest paymentRequest) {
    validatePaymentRequest(paymentRequest);

    BankResponse bankResponse = acquiringBankClient.processPayment(paymentRequest);

    PostPaymentResponse paymentResponse = buildPostPaymentResponse(paymentRequest, bankResponse);

    paymentsRepository.add(paymentResponse);

    return paymentResponse;
  }

  /**
   * Runs all validation checks for a payment request and throws a
   * PaymentValidationException for any invalid input.
   */
  private void validatePaymentRequest(PostPaymentRequest paymentRequest) {
    validateCardNumber(paymentRequest.getCardNumber());

    validateExpiryDate(
        paymentRequest.getExpiryMonth(),
        paymentRequest.getExpiryYear());

    validateCurrency(paymentRequest.getCurrency());

    validateAmount(paymentRequest.getAmount());

    validateCvv(paymentRequest.getCvv());
  }

  /**
   * Validates the card number format (14-19 digits).
   */
  private void validateCardNumber(String cardNumber) {
    if (cardNumber == null
        || !cardNumber.matches("\\d{14,19}")) {

      throw new PaymentValidationException(
          "Invalid card number");
    }
  }

  /**
   * Validates expiry month/year and ensures the card has not expired.
   */
  private void validateExpiryDate(
      int expiryMonth,
      int expiryYear) {
    if (expiryMonth < 1 || expiryMonth > 12) {

      throw new PaymentValidationException(
          "Invalid expiry month");
    }

    YearMonth expiryDate =
        YearMonth.of(expiryYear, expiryMonth);

    if (expiryDate.isBefore(YearMonth.now())) {

      throw new PaymentValidationException(
          "Card has expired");
    }
  }

  /**
   * Ensures currency is supported by the gateway.
   */
  private void validateCurrency(String currency) {
    if (currency == null
        || !SUPPORTED_CURRENCIES.contains(currency)) {

      throw new PaymentValidationException(
          "Unsupported currency");
    }
  }

  /**
   * Validates that amount is a positive integer (in the smallest currency unit).
   */
  private void validateAmount(int amount) {
    if (amount <= 0) {
      throw new PaymentValidationException(
          "Invalid amount");
    }
  }

  /**
   * Validates the CVV has 3 or 4 digits.
   */
  private void validateCvv(String cvv) {
    if (cvv == null
        || !cvv.matches("\\d{3,4}")) {

      throw new PaymentValidationException(
          "Invalid CVV");
    }
  }


  /**
   * Constructs a PostPaymentResponse from the request and bank response.
   */
  private PostPaymentResponse buildPostPaymentResponse(
      PostPaymentRequest paymentRequest,
      BankResponse bankResponse) {

    PostPaymentResponse response = new PostPaymentResponse();

    response.setId(UUID.randomUUID());

    response.setStatus(
        bankResponse.isAuthorized()
            ? PaymentStatus.AUTHORIZED
            : PaymentStatus.DECLINED);

    response.setCardNumberLastFour(extractLastFour(paymentRequest));

    response.setExpiryMonth(paymentRequest.getExpiryMonth());

    response.setExpiryYear(paymentRequest.getExpiryYear());

    response.setCurrency(paymentRequest.getCurrency());

    response.setAmount(paymentRequest.getAmount());

    return response;
  }

  /**
   * Builds a GetPaymentResponse suitable for returning from the API from an internal
   * PostPaymentResponse.
   */
  private GetPaymentResponse buildGetPaymentResponse(
      PostPaymentResponse paymentResponse) {

    GetPaymentResponse response = new GetPaymentResponse();

    response.setId(paymentResponse.getId());

    response.setStatus(paymentResponse.getStatus());

    response.setCardNumberLastFour(paymentResponse.getCardNumberLastFour());

    response.setExpiryMonth(paymentResponse.getExpiryMonth());

    response.setExpiryYear(paymentResponse.getExpiryYear());

    response.setCurrency(paymentResponse.getCurrency());

    response.setAmount(paymentResponse.getAmount());

    return response;
  }

  /**
   * Extracts the last four digits of the card number for display/storage.
   */
  private String extractLastFour(
      PostPaymentRequest paymentRequest) {

    String cardNumber = paymentRequest.getCardNumber();

    return cardNumber.substring(cardNumber.length() - 4);
  }
}
