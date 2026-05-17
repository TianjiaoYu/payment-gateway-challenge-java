package com.checkout.payment.gateway.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of possible payment statuses used across the API and persisted responses.
 * <p>
 * The three statuses align with the merchant-facing outcomes defined by the challenge:
 * <ul>
 *   <li>{@link #AUTHORIZED} — the acquiring bank approved the payment.</li>
 *   <li>{@link #DECLINED} — the acquiring bank rejected the payment.</li>
 *   <li>{@link #REJECTED} — the gateway rejected the request before contacting the bank
 *       (e.g. invalid card, unsupported currency).</li>
 * </ul>
 */
public enum PaymentStatus {
  /** The acquiring bank approved the payment. */
  AUTHORIZED("Authorized"),
  /** The acquiring bank rejected the payment. */
  DECLINED("Declined"),
  /** The gateway rejected the request without contacting the bank. */
  REJECTED("Rejected");

  private final String name;

  PaymentStatus(String name) {
    this.name = name;
  }

  /**
   * Returns the display name used in JSON for this status.
   *
   * @return the human-readable status name as serialized in API responses
   */
  @JsonValue
  public String getName() {
    return this.name;
  }
}
