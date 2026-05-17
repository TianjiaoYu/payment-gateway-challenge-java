package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.exception.AcquiringBankUnavailableException;
import com.checkout.payment.gateway.model.BankResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


/**
 * Client responsible for calling the acquiring bank API to authorize/decline payments.
 * Wraps RestTemplate and converts HTTP errors into domain exceptions.
 */
@Service
public class AcquiringBankClient {
  private static final Logger LOG = LoggerFactory.getLogger(AcquiringBankClient.class);
  private static final String ACQUIRING_BANK_URL = "http://localhost:8080/payments";

  private final RestTemplate restTemplate;

  /**
   * Construct the client with an injected RestTemplate.
   */
  public AcquiringBankClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  /**
   * Sends the payment request to the acquiring bank and returns the bank response.
   * When the bank is unreachable or returns an error, an AcquiringBankUnavailableException
   * is thrown for the global handler to translate into a 502 response.
   */
  public BankResponse processPayment(PostPaymentRequest postPaymentRequest) {
    try {
      return restTemplate.postForObject(ACQUIRING_BANK_URL, postPaymentRequest, BankResponse.class);

    } catch (RestClientException ex) {
      LOG.error("Acquiring bank call failed", ex);

      throw new AcquiringBankUnavailableException(
          "Acquiring bank call failed", ex);
    }
  }

}
