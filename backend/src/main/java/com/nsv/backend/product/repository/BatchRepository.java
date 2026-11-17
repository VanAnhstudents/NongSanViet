package com.nsv.backend.product.repository;

import com.nsv.backend.product.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    @Query("select b from Batch b join fetch b.product p where p.farmerId = :farmerId "
            + "order by b.harvestDate asc, b.id asc")
    List<Batch> findAllByFarmer(@Param("farmerId") Long farmerId);

    @Query("select b from Batch b join fetch b.product p where p.farmerId = :farmerId "
            + "and b.harvestDate >= :fromDate order by b.harvestDate asc, b.id asc")
    List<Batch> findUpcomingByFarmer(@Param("farmerId") Long farmerId, @Param("fromDate") LocalDate fromDate);

    @Query("select b from Batch b join fetch b.product p where b.id = :id and p.farmerId = :farmerId")
    Optional<Batch> findByIdAndFarmer(@Param("id") Long id, @Param("farmerId") Long farmerId);
}
