package com.nsv.backend.product.service;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.product.dto.CreateProductRequest;
import com.nsv.backend.product.dto.ProductSummaryResponse;
import com.nsv.backend.product.entity.Product;
import com.nsv.backend.product.entity.ProductImage;
import com.nsv.backend.product.repository.ProductImageRepository;
import com.nsv.backend.product.repository.ProductRepository;
import com.nsv.backend.product.spi.ProductBatchPort;
import com.nsv.backend.product.spi.ProductBatchSnapshot;
import com.nsv.backend.user.entity.Farm;
import com.nsv.backend.user.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final List<String> ACTIVE_SALE_STATUSES = List.of("AVAILABLE", "PRE_ORDER");

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductBatchPort productBatchPort;
    private final FarmRepository farmRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public ProductSummaryResponse createProductInQuickFlow(Long farmerId, CreateProductRequest request) {
        // 1. Kiểm tra Farmer đã có hồ sơ trang trại
        Farm farm = farmRepository.findByUserId(farmerId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.BAD_REQUEST,
                "FARM_PROFILE_REQUIRED",
                "Vui lòng tạo hồ sơ trang trại trước khi đăng bán sản phẩm"
            ));

        // 2. Xác thực danh mục sản phẩm tồn tại
        String categoryName = querySingleString("SELECT name FROM categories WHERE id = ?", request.categoryId());
        if (categoryName == null) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "CATEGORY_NOT_FOUND",
                "Danh mục sản phẩm không tồn tại"
            );
        }

        // 3. Lưu thông tin sản phẩm (Bước 1)
        Product product = Product.builder()
            .farmerId(farmerId)
            .categoryId(request.categoryId())
            .name(request.name().trim())
            .description(StringUtils.hasText(request.description()) ? request.description().trim() : null)
            .unit(request.unit().trim())
            .build();

        Product savedProduct = productRepository.save(product);

        // 4. Lưu danh sách hình ảnh sản phẩm theo thứ tự hiển thị (Bước 2)
        List<ProductImage> imagesToSave = new ArrayList<>(request.imageUrls().size());
        for (int i = 0; i < request.imageUrls().size(); i++) {
            imagesToSave.add(ProductImage.builder()
                .productId(savedProduct.getId())
                .imageUrl(request.imageUrls().get(i).trim())
                .sortOrder(i)
                .build());
        }
        List<ProductImage> savedImages = productImageRepository.saveAll(imagesToSave);

        // 5. Tạo lô hàng mở bán đầu tiên kèm mã QR duy nhất, giá và số lượng (Bước 3)
        BigDecimal normalizedPrice = request.unitPrice().setScale(2, RoundingMode.HALF_UP);
        BigDecimal normalizedQuantity = request.quantity().setScale(2, RoundingMode.HALF_UP);
        LocalDate harvestDate = request.harvestDate() != null ? request.harvestDate() : LocalDate.now();
        String saleStatus = StringUtils.hasText(request.saleStatus())
            ? request.saleStatus().trim().toUpperCase()
            : "AVAILABLE";

        ProductBatchSnapshot savedBatch = productBatchPort.createInitialBatch(
            savedProduct.getId(),
            normalizedPrice,
            normalizedQuantity,
            harvestDate,
            saleStatus
        );

        recordAuditLog(farmerId, "CREATE_PRODUCT", "products", savedProduct.getId());

        String provinceName = querySingleString("SELECT name FROM provinces WHERE id = ?", farm.getProvinceId());
        List<String> imageUrls = savedImages.stream().map(ProductImage::getImageUrl).toList();

        return new ProductSummaryResponse(
            savedProduct.getId(),
            savedProduct.getName(),
            savedProduct.getCategoryId(),
            categoryName,
            savedProduct.getUnit(),
            savedProduct.getDescription(),
            imageUrls.isEmpty() ? null : imageUrls.get(0),
            imageUrls,
            savedBatch.id(),
            savedBatch.traceabilityCode(),
            savedBatch.unitPrice(),
            savedBatch.remainingQuantity(),
            savedBatch.status(),
            savedBatch.harvestDate(),
            farm.getId(),
            farm.getFarmName(),
            provinceName,
            "VERIFIED".equals(farm.getCertificateStatus()),
            savedProduct.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> getPublicProducts(
            String keyword,
            Long categoryId,
            Long provinceId,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        List<Product> products = productRepository.searchPublicProducts(
            normalizedKeyword,
            categoryId,
            provinceId,
            minPrice,
            maxPrice
        );

        return enrichProductSummaries(products);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductSummaryResponse getPublicProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "PRODUCT_NOT_FOUND",
                "Không tìm thấy sản phẩm"
            ));

        List<ProductSummaryResponse> enriched = enrichProductSummaries(List.of(product));
        if (enriched.isEmpty()) {
            throw new BusinessException(
                HttpStatus.NOT_FOUND,
                "PRODUCT_NOT_AVAILABLE",
                "Sản phẩm hiện không còn lô hàng mở bán"
            );
        }
        return enriched.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> getFarmerProducts(Long farmerId) {
        List<Product> products = productRepository.findAllByFarmerIdOrderByCreatedAtDesc(farmerId);
        return enrichProductSummaries(products);
    }

    private List<ProductSummaryResponse> enrichProductSummaries(List<Product> products) {
        if (products.isEmpty()) {
            return List.of();
        }

        Set<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toSet());
        Set<Long> farmerIds = products.stream().map(Product::getFarmerId).collect(Collectors.toSet());

        Map<Long, List<String>> imagesByProductId = productImageRepository
            .findAllByProductIdInOrderBySortOrderAscIdAsc(productIds)
            .stream()
            .collect(Collectors.groupingBy(
                ProductImage::getProductId,
                Collectors.mapping(ProductImage::getImageUrl, Collectors.toList())
            ));

        Map<Long, ProductBatchSnapshot> latestActiveBatchByProductId =
            productBatchPort.findLatestActiveBatches(productIds, ACTIVE_SALE_STATUSES);

        Map<Long, Farm> farmByUserId = farmRepository.findAll().stream()
            .filter(f -> farmerIds.contains(f.getUserId()))
            .collect(Collectors.toMap(Farm::getUserId, Function.identity(), (a, b) -> a));

        Map<Long, String> categoryNameCache = new HashMap<>();
        Map<Long, String> provinceNameCache = new HashMap<>();

        List<ProductSummaryResponse> results = new ArrayList<>(products.size());
        for (Product product : products) {
            ProductBatchSnapshot activeBatch = latestActiveBatchByProductId.get(product.getId());
            if (activeBatch == null) {
                continue;
            }

            Farm farm = farmByUserId.get(product.getFarmerId());
            List<String> urls = imagesByProductId.getOrDefault(product.getId(), List.of());

            String categoryName = categoryNameCache.computeIfAbsent(
                product.getCategoryId(),
                cid -> querySingleString("SELECT name FROM categories WHERE id = ?", cid)
            );
            String provinceName = (farm != null)
                ? provinceNameCache.computeIfAbsent(
                    farm.getProvinceId(),
                    pid -> querySingleString("SELECT name FROM provinces WHERE id = ?", pid)
                )
                : null;

            results.add(new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getCategoryId(),
                categoryName,
                product.getUnit(),
                product.getDescription(),
                urls.isEmpty() ? null : urls.get(0),
                urls,
                activeBatch.id(),
                activeBatch.traceabilityCode(),
                activeBatch.unitPrice(),
                activeBatch.remainingQuantity(),
                activeBatch.status(),
                activeBatch.harvestDate(),
                farm != null ? farm.getId() : null,
                farm != null ? farm.getFarmName() : null,
                provinceName,
                farm != null && "VERIFIED".equals(farm.getCertificateStatus()),
                product.getCreatedAt()
            ));
        }

        return results;
    }

    private void recordAuditLog(Long actorId, String action, String entityType, Long entityId) {
        jdbcTemplate.update(
            "INSERT INTO audit_logs (actor_id, action, entity_type, entity_id) VALUES (?, ?, ?, ?)",
            actorId,
            action,
            entityType,
            entityId
        );
    }

    private String querySingleString(String sql, Object param) {
        return jdbcTemplate.query(
            sql,
            ps -> ps.setObject(1, param),
            rs -> rs.next() ? rs.getString(1) : null
        );
    }
}
