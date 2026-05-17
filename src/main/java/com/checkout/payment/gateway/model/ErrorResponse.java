package com.checkout.payment.gateway.model;

/**
 * Standard error response returned by controllers for client-friendly messages.
 */
public class ErrorResponse {
  private final String message;

  /**
   * Create an error response with a human-readable message.
   * @param message brief client-facing error message
   */
  public ErrorResponse(String message) {
    this.message = message;
  }

  /**
   * Returns the client-facing error message.
   */
  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "ErrorResponse{" +
        "message='" + message + '\'' +
        '}';
  }
}
