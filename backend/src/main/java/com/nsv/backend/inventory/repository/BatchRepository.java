package com.nsv.backend.inventory.repository;

import com.nsv.backend.inventory.entity.Batch;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    Optional<Batch> findByTraceabilityCode(UUID traceabilityCode);

    List<Batch> findAllByIdIn(Collection<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Batch b WHERE b.id IN :ids ORDER BY b.id ASC")
    List<Batch> findAllByIdInOrderByIdAscForUpdate(@Param("ids") Collection<Long> ids);

    @Query("select b from Batch b join fetch b.product p where p.farmerId = :farmerId "
            + "order by b.harvestDate asc, b.id asc")
    List<Batch> findAllByFarmer(@Param("farmerId") Long farmerId);

    @Query("select b from Batch b join fetch b.product p where p.farmerId = :farmerId "
            + "and b.harvestDate >= :fromDate order by b.harvestDate asc, b.id asc")
    List<Batch> findUpcomingByFarmer(@Param("farmerId") Long farmerId, @Param("fromDate") LocalDate fromDate);

    @Query("select b from Batch b join fetch b.product p where b.id = :id and p.farmerId = :farmerId")
    Optional<Batch> findByIdAndFarmer(@Param("id") Long id, @Param("farmerId") Long farmerId);
}
