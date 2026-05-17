package com.checkout.payment.gateway.repository;

import com.checkout.payment.gateway.model.PostPaymentResponse;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * In-memory repository for storing processed payments. Not persistent — suitable for
 * tests and simple demos. Backed by a {@link ConcurrentHashMap} so concurrent access
 * from request threads is safe.
 */
@Repository
public class PaymentsRepository {

  private final Map<UUID, PostPaymentResponse> payments = new ConcurrentHashMap<>();

  /**
   * Add a processed payment to the repository, keyed by its id. Existing entries with
   * the same id are overwritten.
   *
   * @param payment the processed payment to store; must have a non-null id
   */
  public void add(PostPaymentResponse payment) {
    payments.put(payment.getId(), payment);
  }

  /**
   * Retrieve a payment by id.
   *
   * @param id the payment id to look up
   * @return the stored payment wrapped in an {@link Optional}, or empty if no payment
   *         with that id is stored
   */
  public Optional<PostPaymentResponse> get(UUID id) {
    return Optional.ofNullable(payments.get(id));
  }

}
