package com.nsv.backend.trace.service;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.inventory.entity.Batch;
import com.nsv.backend.inventory.repository.BatchRepository;
import com.nsv.backend.product.entity.Product;
import com.nsv.backend.product.repository.ProductRepository;
import com.nsv.backend.trace.dto.*;
import com.nsv.backend.trace.entity.FarmingLog;
import com.nsv.backend.trace.repository.FarmingLogRepository;
import com.nsv.backend.user.entity.Farm;
import com.nsv.backend.user.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TraceabilityServiceImpl implements TraceabilityService {

    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final FarmRepository farmRepository;
    private final FarmingLogRepository farmingLogRepository;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.frontend.base-url:http://localhost:5500}")
    private String frontendBaseUrl;

    @Override
    @Transactional(readOnly = true)
    public TraceabilityResponse getTraceabilityByCode(String traceabilityCodeRaw) {
        UUID traceabilityCode = parseUuid(traceabilityCodeRaw);

        Batch batch = batchRepository.findByTraceabilityCode(traceabilityCode)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "TRACEABILITY_CODE_NOT_FOUND",
                "Không tìm thấy thông tin truy xuất nguồn gốc cho mã QR này"
            ));

        return buildTraceabilityResponse(batch);
    }

    @Override
    @Transactional(readOnly = true)
    public TraceabilityResponse getTraceabilityByBatchId(Long batchId) {
        Batch batch = batchRepository.findById(batchId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "BATCH_NOT_FOUND",
                "Không tìm thấy lô sản phẩm"
            ));

        return buildTraceabilityResponse(batch);
    }

    @Override
    @Transactional
    public FarmingLogResponse addFarmingLog(Long farmerId, Long batchId, CreateFarmingLogRequest request) {
        Batch batch = batchRepository.findById(batchId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "BATCH_NOT_FOUND",
                "Không tìm thấy lô sản phẩm"
            ));

        Product product = productRepository.findById(batch.getProductId())
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "PRODUCT_NOT_FOUND",
                "Không tìm thấy sản phẩm của lô hàng"
            ));

        if (!product.getFarmerId().equals(farmerId)) {
            throw new BusinessException(
                HttpStatus.FORBIDDEN,
                "BATCH_ACCESS_DENIED",
                "Bạn không có quyền ghi nhật ký canh tác cho lô hàng này"
            );
        }

        FarmingLog log = FarmingLog.builder()
            .batchId(batch.getId())
            .logDate(request.logDate())
            .activity(request.activity().trim())
            .note(StringUtils.hasText(request.note()) ? request.note().trim() : null)
            .imageUrl(StringUtils.hasText(request.imageUrl()) ? request.imageUrl().trim() : null)
            .build();

        FarmingLog savedLog = farmingLogRepository.save(log);
        return FarmingLogResponse.from(savedLog);
    }

    private TraceabilityResponse buildTraceabilityResponse(Batch batch) {
        Product product = productRepository.findById(batch.getProductId())
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "PRODUCT_NOT_FOUND",
                "Không tìm thấy thông tin sản phẩm gắn với lô hàng"
            ));

        Farm farm = farmRepository.findByUserId(product.getFarmerId())
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "FARM_NOT_FOUND",
                "Không tìm thấy hồ sơ trang trại của sản phẩm"
            ));

        List<FarmingLogResponse> logResponses = farmingLogRepository
            .findAllByBatchIdOrderByLogDateAscIdAsc(batch.getId())
            .stream()
            .map(FarmingLogResponse::from)
            .toList();

        String categoryName = querySingleString("SELECT name FROM categories WHERE id = ?", product.getCategoryId());
        String primaryImageUrl = querySingleString(
            "SELECT image_url FROM product_images WHERE product_id = ? ORDER BY sort_order ASC, id ASC LIMIT 1",
            product.getId()
        );
        String farmerName = querySingleString("SELECT full_name FROM users WHERE id = ?", farm.getUserId());
        String provinceName = querySingleString("SELECT name FROM provinces WHERE id = ?", farm.getProvinceId());

        BatchTraceInfoResponse batchInfo = new BatchTraceInfoResponse(
            batch.getId(),
            batch.getTraceabilityCode(),
            buildQrScanUrl(batch.getTraceabilityCode()),
            batch.getHarvestDate(),
            batch.getStatus(),
            batch.getUnitPrice(),
            batch.getTotalQuantity(),
            batch.getRemainingQuantity()
        );

        ProductTraceInfoResponse productInfo = new ProductTraceInfoResponse(
            product.getId(),
            product.getName(),
            categoryName,
            product.getUnit(),
            product.getDescription(),
            primaryImageUrl
        );

        FarmTraceInfoResponse farmInfo = new FarmTraceInfoResponse(
            farm.getId(),
            farm.getFarmName(),
            farmerName,
            provinceName,
            farm.getAddressLine(),
            farm.getCertificateStatus(),
            farm.getCertificateUrl()
        );

        return new TraceabilityResponse(
            batchInfo,
            productInfo,
            farmInfo,
            logResponses,
            logResponses.size(),
            OffsetDateTime.now()
        );
    }

    private UUID parseUuid(String rawCode) {
        if (!StringUtils.hasText(rawCode)) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_TRACEABILITY_CODE",
                "Mã QR truy xuất nguồn gốc không được để trống"
            );
        }
        try {
            return UUID.fromString(rawCode.trim());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_TRACEABILITY_CODE",
                "Định dạng mã QR truy xuất nguồn gốc không hợp lệ"
            );
        }
    }

    private String buildQrScanUrl(UUID traceabilityCode) {
        String base = StringUtils.hasText(frontendBaseUrl) ? frontendBaseUrl.replaceAll("/+$", "") : "http://localhost:5500";
        return base + "/pages/buyer/trace.html?code=" + traceabilityCode;
    }

    private String querySingleString(String sql, Object param) {
        return jdbcTemplate.query(
            sql,
            ps -> ps.setObject(1, param),
            rs -> rs.next() ? rs.getString(1) : null
        );
    }
}
