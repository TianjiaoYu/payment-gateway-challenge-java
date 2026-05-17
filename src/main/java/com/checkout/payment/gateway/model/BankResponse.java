package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the acquiring bank's response indicating whether a payment was authorised
 * and an optional authorization code.
 */
public class BankResponse {

  private boolean authorized;
  @JsonProperty("authorization_code")
  private String authorizationCode;

  /**
   * Indicates whether the bank authorized the payment.
   *
   * @return true if the bank approved the payment, false otherwise
   */
  public boolean isAuthorized() {
    return authorized;
  }

  /**
   * Sets the bank's authorization decision.
   *
   * @param authorized true if the bank approved the payment
   */
  public void setAuthorized(boolean authorized) {
    this.authorized = authorized;
  }

  /**
   * Returns the authorization code provided by the bank, if any. Mapped from the
   * JSON field {@code authorization_code}.
   *
   * @return the bank's authorization code, or null when not provided
   */
  public String getAuthorizationCode() {
    return authorizationCode;
  }

  /**
   * Sets the authorization code returned by the bank.
   *
   * @param authorizationCode the bank's authorization code
   */
  public void setAuthorizationCode(String authorizationCode) {
    this.authorizationCode = authorizationCode;
  }
}
