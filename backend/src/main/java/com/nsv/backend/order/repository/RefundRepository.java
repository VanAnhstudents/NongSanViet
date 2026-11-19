package com.nsv.backend.order.repository;

import com.nsv.backend.order.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< HEAD
import java.util.Collection;
import java.util.List;
=======
import java.util.List;
import java.util.Set;
>>>>>>> origin/develop

public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findAllByPaymentIdOrderByCreatedAtAsc(Long paymentId);

<<<<<<< HEAD
    List<Refund> findAllByPaymentIdInOrderByCreatedAtAsc(Collection<Long> paymentIds);
=======
    List<Refund> findAllByPaymentIdInOrderByCreatedAtAsc(Set<Long> paymentIds);
>>>>>>> origin/develop
}
