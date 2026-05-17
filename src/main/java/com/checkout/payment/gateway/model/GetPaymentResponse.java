package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;
import java.util.UUID;

/**
 * Response returned when retrieving payment details by id. Mirrors stored payment
 * information excluding sensitive full card data.
 */
public class GetPaymentResponse {

  private UUID id;
  private PaymentStatus status;
  private String cardNumberLastFour;
  private int expiryMonth;
  private int expiryYear;
  private String currency;
  private int amount;

  /**
   * @return the unique identifier assigned to this payment
   */
  public UUID getId() {
    return id;
  }

  /**
   * @param id the unique identifier assigned to this payment
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   * @return the payment status (Authorized, Declined, or Rejected)
   */
  public PaymentStatus getStatus() {
    return status;
  }

  /**
   * @param status the payment status (Authorized, Declined, or Rejected)
   */
  public void setStatus(PaymentStatus status) {
    this.status = status;
  }

  /**
   * @return the masked card number containing only the last four digits
   */
  public String getCardNumberLastFour() {
    return cardNumberLastFour;
  }

  /**
   * @param cardNumberLastFour the masked card number containing only the last four digits
   */
  public void setCardNumberLastFour(String cardNumberLastFour) {
    this.cardNumberLastFour = cardNumberLastFour;
  }

  /**
   * @return the card expiry month (1-12)
   */
  public int getExpiryMonth() {
    return expiryMonth;
  }

  /**
   * @param expiryMonth the card expiry month (1-12)
   */
  public void setExpiryMonth(int expiryMonth) {
    this.expiryMonth = expiryMonth;
  }

  /**
   * @return the card expiry year (four-digit calendar year)
   */
  public int getExpiryYear() {
    return expiryYear;
  }

  /**
   * @param expiryYear the card expiry year (four-digit calendar year)
   */
  public void setExpiryYear(int expiryYear) {
    this.expiryYear = expiryYear;
  }

  /**
   * @return the ISO 4217 currency code (e.g. "USD", "EUR", "GBP")
   */
  public String getCurrency() {
    return currency;
  }

  /**
   * @param currency the ISO 4217 currency code (e.g. "USD", "EUR", "GBP")
   */
  public void setCurrency(String currency) {
    this.currency = currency;
  }

  /**
   * @return the amount in the smallest currency unit (e.g. cents)
   */
  public int getAmount() {
    return amount;
  }

  /**
   * @param amount the amount in the smallest currency unit (e.g. cents)
   */
  public void setAmount(int amount) {
    this.amount = amount;
  }

  /**
   * Compact string representation used for debugging.
   */
  @Override
  public String toString() {
    return "GetPaymentResponse{" +
        "id=" + id +
        ", status=" + status +
        ", cardNumberLastFour=" + cardNumberLastFour +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        '}';
  }
}
