package com.nsv.backend.product.service;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.common.exception.FieldErrorDetail;
import com.nsv.backend.product.dto.BatchCreateRequest;
import com.nsv.backend.product.dto.BatchResponse;
import com.nsv.backend.product.dto.BatchUpdateRequest;
import com.nsv.backend.product.entity.Batch;
import com.nsv.backend.product.entity.BatchStatus;
import com.nsv.backend.product.entity.Product;
import com.nsv.backend.product.repository.BatchRepository;
import com.nsv.backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** PBI-04: khai báo sản lượng dự kiến và lịch thu hoạch thông qua lô hàng (batches). */
@Service
@RequiredArgsConstructor
public class BatchService {

    /** Ngày thu hoạch dự kiến tối đa cách hôm nay bao nhiêu ngày (chặn nhập nhầm năm). */
    static final int MAX_DAYS_AHEAD = 365;

    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final Clock clock;

    @Transactional
    public BatchResponse create(Long farmerId, BatchCreateRequest request) {
        validateHarvestDate(request.harvestDate());
        Product product = productRepository.findByIdAndFarmerId(request.productId(), farmerId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy sản phẩm của bạn"));

        Batch batch = Batch.builder()
                .product(product)
                .traceabilityCode(UUID.randomUUID())
                .harvestDate(request.harvestDate())
                .unitPrice(request.unitPrice())
                .totalQuantity(request.totalQuantity())
                .status(BatchStatus.PRE_ORDER)
                .build();
        return toResponse(batchRepository.save(batch));
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> list(Long farmerId, boolean upcomingOnly) {
        List<Batch> batches = upcomingOnly
                ? batchRepository.findUpcomingByFarmer(farmerId, LocalDate.now(clock))
                : batchRepository.findAllByFarmer(farmerId);
        return batches.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BatchResponse get(Long farmerId, Long id) {
        return toResponse(findOwned(farmerId, id));
    }

    @Transactional
    public BatchResponse update(Long farmerId, Long id, BatchUpdateRequest request) {
        Batch batch = findOwned(farmerId, id);
        if (batch.getStatus() != BatchStatus.PRE_ORDER) {
            throw new ApiException(HttpStatus.CONFLICT, "INVALID_STATE",
                    "Chỉ được sửa lô hàng đang ở trạng thái đặt trước (PRE_ORDER)", null);
        }
        validateHarvestDate(request.harvestDate());
        batch.setTotalQuantity(request.totalQuantity());
        batch.setUnitPrice(request.unitPrice());
        batch.setHarvestDate(request.harvestDate());
        return toResponse(batchRepository.save(batch));
    }

    /** Chỉ chủ sở hữu mới thấy lô; lô của người khác trả 404 để không lộ sự tồn tại. */
    private Batch findOwned(Long farmerId, Long id) {
        return batchRepository.findByIdAndFarmer(id, farmerId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy lô hàng"));
    }

    private void validateHarvestDate(LocalDate date) {
        LocalDate today = LocalDate.now(clock);
        if (date.isBefore(today)) {
            throw ApiException.badRequest("Ngày thu hoạch không hợp lệ", List.of(new FieldErrorDetail(
                    "harvestDate", "Ngày thu hoạch dự kiến không được ở trong quá khứ")));
        }
        if (date.isAfter(today.plusDays(MAX_DAYS_AHEAD))) {
            throw ApiException.badRequest("Ngày thu hoạch không hợp lệ", List.of(new FieldErrorDetail(
                    "harvestDate", "Ngày thu hoạch dự kiến không được quá " + MAX_DAYS_AHEAD + " ngày kể từ hôm nay")));
        }
    }

    private BatchResponse toResponse(Batch b) {
        Product p = b.getProduct();
        return new BatchResponse(b.getId(), p.getId(), p.getName(), p.getUnit(), b.getTraceabilityCode(),
                b.getHarvestDate(), b.getUnitPrice(), b.getTotalQuantity(), b.getStatus(), b.getCreatedAt());
    }
}
