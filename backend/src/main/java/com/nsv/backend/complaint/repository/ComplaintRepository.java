package com.nsv.backend.complaint.repository;

import com.nsv.backend.complaint.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    /** Đơn hàng có ít nhất một sản phẩm của Farmer này (Farmer là bên bị khiếu nại). */
    @Query(value = "select exists (select 1 from order_items oi "
            + "join batches b on b.id = oi.batch_id "
            + "join products p on p.id = b.product_id "
            + "where oi.order_id = :orderId and p.farmer_id = :farmerId)", nativeQuery = true)
    boolean existsOrderOfFarmer(@Param("orderId") Long orderId, @Param("farmerId") Long farmerId);

    @Query(value = "select c.* from complaints c where exists (select 1 from order_items oi "
            + "join batches b on b.id = oi.batch_id "
            + "join products p on p.id = b.product_id "
            + "where oi.order_id = c.order_id and p.farmer_id = :farmerId) order by c.id desc", nativeQuery = true)
    List<Complaint> findAllOfFarmer(@Param("farmerId") Long farmerId);

    /** Khiếu nại chưa có phản hồi của Farmer, chưa có quyết định của Admin, tạo từ thời điểm deadline trở về trước. */
    List<Complaint> findByFarmerResponseIsNullAndAdminDecisionIsNullAndCreatedAtLessThanEqual(Instant deadline);
}
