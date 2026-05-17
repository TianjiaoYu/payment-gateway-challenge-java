package com.checkout.payment.gateway.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the in-memory {@link PaymentsRepository}.
 */
class PaymentsRepositoryUnitTest {

  private final PaymentsRepository repository = new PaymentsRepository();

  private PostPaymentResponse paymentWith(UUID id, int amount) {
    PostPaymentResponse p = new PostPaymentResponse();
    p.setId(id);
    p.setStatus(PaymentStatus.AUTHORIZED);
    p.setAmount(amount);
    return p;
  }

  @Test
  void get_returnsStoredPayment_afterAdd() {
    UUID id = UUID.randomUUID();
    repository.add(paymentWith(id, 100));

    Optional<PostPaymentResponse> result = repository.get(id);

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(id);
    assertThat(result.get().getAmount()).isEqualTo(100);
  }

  @Test
  void get_returnsEmpty_forUnknownId() {
    assertThat(repository.get(UUID.randomUUID())).isEmpty();
  }

  @Test
  void add_overwritesExistingEntryForSameId() {
    UUID id = UUID.randomUUID();
    repository.add(paymentWith(id, 100));
    repository.add(paymentWith(id, 200));

    assertThat(repository.get(id))
        .get()
        .extracting(PostPaymentResponse::getAmount)
        .isEqualTo(200);
  }
}
