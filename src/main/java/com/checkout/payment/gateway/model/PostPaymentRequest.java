package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Request payload expected by the API when submitting a payment. Field names match the
 * API JSON where annotated.
 */
public class PostPaymentRequest implements Serializable {

  @JsonProperty("card_number")
  private String cardNumber;
  @JsonProperty("expiry_month")
  private int expiryMonth;
  @JsonProperty("expiry_year")
  private int expiryYear;
  private String currency;
  private int amount;
  private String cvv;

  /**
   * @return the full card number as supplied by the merchant. Mapped from the JSON
   *         field {@code card_number}. Treat as sensitive: never log or surface beyond
   *         the gateway boundary.
   */
  public String getCardNumber() {
    return cardNumber;
  }

  /**
   * @param cardNumber the full card number (14-19 digits)
   */
  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  /**
   * @return the card expiry month (1-12). Mapped from the JSON field {@code expiry_month}.
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
   * @return the card expiry year as a four-digit calendar year. Mapped from the JSON
   *         field {@code expiry_year}.
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
   * @return the amount in the smallest currency unit (e.g. cents). Must be positive.
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
   * @return the card verification value (3-4 digits). Treat as sensitive: never log
   *         or persist.
   */
  public String getCvv() {
    return cvv;
  }

  /**
   * @param cvv the card verification value (3-4 digits)
   */
  public void setCvv(String cvv) {
    this.cvv = cvv;
  }

  /**
   * Returns a simple expiry date string ("month/year") emitted under the JSON field
   * {@code expiry_date} for downstream consumers that prefer a single field.
   *
   * @return the formatted expiry date string
   */
  @JsonProperty("expiry_date")
  public String getExpiryDate() {
    return String.format("%d/%d", expiryMonth, expiryYear);
  }

  /**
   * Returns a compact string representation for logging. Sensitive fields may be present;
   * avoid logging in production.
   */
  @Override
  public String toString() {
    return "PostPaymentRequest{" +
        "cardNumber=" + cardNumber +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        ", cvv=" + cvv +
        '}';
  }
}
