package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;
import java.util.UUID;

/**
 * Response returned after processing a payment. Contains safe, display-ready fields
 * (e.g. last four digits of the card) and the payment status.
 */
public class PostPaymentResponse {

  private UUID id;
  private PaymentStatus status;
  private String cardNumberLastFour;
  private int expiryMonth;
  private int expiryYear;
  private String currency;
  private int amount;


  /**
   * @return the unique identifier assigned to this payment, or null for rejected requests
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
   * Returns a compact string representation for logging and diagnostics.
   */
  @Override
  public String toString() {
    return "PostPaymentResponse{" +
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
