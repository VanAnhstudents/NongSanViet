package com.nsv.backend.product.repository;

import com.nsv.backend.product.dto.ProductSearchCriteria;
import com.nsv.backend.product.dto.ProductSummaryResponse;
import com.nsv.backend.product.dto.SearchFilterOptionsResponse;
import com.nsv.backend.product.dto.SearchFilterOptionsResponse.FilterOptionItem;
import com.nsv.backend.product.entity.ProductImage;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcProductSearchRepository implements ProductSearchRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final ProductImageRepository productImageRepository;

    @Override
    public List<ProductSummaryResponse> searchProducts(ProductSearchCriteria criteria) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String whereClause = buildDynamicWhereClause(criteria, params);

        String orderByClause = switch (criteria.normalizedSortBy()) {
            case "PRICE_ASC" -> "ORDER BY ap.unit_price ASC, ap.created_at DESC, ap.id DESC";
            case "PRICE_DESC" -> "ORDER BY ap.unit_price DESC, ap.created_at DESC, ap.id DESC";
            default -> "ORDER BY ap.created_at DESC, ap.id DESC";
        };

        int offset = criteria.page() * criteria.size();
        params.addValue("limit", criteria.size());
        params.addValue("offset", offset);

        String sql = """
            WITH active_products AS (
                SELECT DISTINCT ON (p.id)
                    p.id,
                    p.name,
                    p.category_id,
                    c.name AS category_name,
                    p.unit,
                    p.description,
                    p.created_at,
                    b.id AS batch_id,
                    b.traceability_code,
                    b.unit_price,
                    b.remaining_quantity,
                    b.status AS batch_status,
                    b.harvest_date,
                    f.id AS farm_id,
                    f.farm_name,
                    pr.name AS province_name,
                    (f.certificate_status = 'VERIFIED') AS farm_verified
                FROM products p
                JOIN categories c ON c.id = p.category_id
                JOIN farms f ON f.user_id = p.farmer_id
                JOIN provinces pr ON pr.id = f.province_id
                JOIN batches b ON b.product_id = p.id
                WHERE b.status IN ('AVAILABLE', 'PRE_ORDER')
                  AND b.remaining_quantity > 0
                  %s
                ORDER BY p.id, b.created_at DESC, b.id DESC
            )
            SELECT *
            FROM active_products ap
            %s
            LIMIT :limit OFFSET :offset
            """.formatted(whereClause, orderByClause);

        List<ProductRowProjection> rows = namedJdbcTemplate.query(
            sql,
            params,
            (rs, rowNum) -> new ProductRowProjection(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getLong("category_id"),
                rs.getString("category_name"),
                rs.getString("unit"),
                rs.getString("description"),
                rs.getLong("batch_id"),
                rs.getObject("traceability_code", UUID.class),
                rs.getBigDecimal("unit_price"),
                rs.getBigDecimal("remaining_quantity"),
                rs.getString("batch_status"),
                rs.getObject("harvest_date", LocalDate.class),
                rs.getLong("farm_id"),
                rs.getString("farm_name"),
                rs.getString("province_name"),
                rs.getBoolean("farm_verified"),
                rs.getObject("created_at", OffsetDateTime.class)
            )
        );

        if (rows.isEmpty()) {
            return List.of();
        }

        Set<Long> productIds = rows.stream()
            .map(ProductRowProjection::id)
            .collect(Collectors.toSet());

        Map<Long, List<String>> imagesByProductId = productImageRepository
            .findAllByProductIdInOrderBySortOrderAscIdAsc(productIds)
            .stream()
            .collect(Collectors.groupingBy(
                ProductImage::getProductId,
                Collectors.mapping(ProductImage::getImageUrl, Collectors.toList())
            ));

        return rows.stream()
            .map(row -> {
                List<String> urls = imagesByProductId.getOrDefault(row.id(), List.of());
                return new ProductSummaryResponse(
                    row.id(),
                    row.name(),
                    row.categoryId(),
                    row.categoryName(),
                    row.unit(),
                    row.description(),
                    urls.isEmpty() ? null : urls.get(0),
                    urls,
                    row.batchId(),
                    row.traceabilityCode(),
                    row.unitPrice(),
                    row.remainingQuantity(),
                    row.batchStatus(),
                    row.harvestDate(),
                    row.farmId(),
                    row.farmName(),
                    row.provinceName(),
                    row.farmVerified(),
                    row.createdAt()
                );
            })
            .toList();
    }

    @Override
    public long countProducts(ProductSearchCriteria criteria) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String whereClause = buildDynamicWhereClause(criteria, params);

        String countSql = """
            SELECT COUNT(DISTINCT p.id)
            FROM products p
            JOIN farms f ON f.user_id = p.farmer_id
            JOIN batches b ON b.product_id = p.id
            WHERE b.status IN ('AVAILABLE', 'PRE_ORDER')
              AND b.remaining_quantity > 0
              %s
            """.formatted(whereClause);

        Long total = namedJdbcTemplate.queryForObject(countSql, params, Long.class);
        return total != null ? total : 0L;
    }

    @Override
    public SearchFilterOptionsResponse findAvailableFilterOptions() {
        List<FilterOptionItem> categories = namedJdbcTemplate.query(
            "SELECT id, name FROM categories ORDER BY name ASC",
            (rs, rowNum) -> new FilterOptionItem(rs.getLong("id"), rs.getString("name"))
        );

        List<FilterOptionItem> provinces = namedJdbcTemplate.query(
            "SELECT id, name FROM provinces ORDER BY name ASC",
            (rs, rowNum) -> new FilterOptionItem(rs.getLong("id"), rs.getString("name"))
        );

        return new SearchFilterOptionsResponse(categories, provinces);
    }

    private String buildDynamicWhereClause(ProductSearchCriteria criteria, MapSqlParameterSource params) {
        StringBuilder sb = new StringBuilder();

        String keyword = criteria.normalizedKeyword();
        if (keyword != null) {
            String escaped = escapeLikeWildcards(keyword.toLowerCase(Locale.ROOT));
            sb.append(" AND LOWER(p.name) LIKE :keywordPattern ESCAPE '\\' ");
            params.addValue("keywordPattern", "%" + escaped + "%");
        }

        if (criteria.categoryId() != null) {
            sb.append(" AND p.category_id = :categoryId ");
            params.addValue("categoryId", criteria.categoryId());
        }

        if (criteria.provinceId() != null) {
            sb.append(" AND f.province_id = :provinceId ");
            params.addValue("provinceId", criteria.provinceId());
        }

        if (criteria.minPrice() != null) {
            sb.append(" AND b.unit_price >= :minPrice ");
            params.addValue("minPrice", criteria.minPrice());
        }

        if (criteria.maxPrice() != null) {
            sb.append(" AND b.unit_price <= :maxPrice ");
            params.addValue("maxPrice", criteria.maxPrice());
        }

        if (Boolean.TRUE.equals(criteria.verifiedOnly())) {
            sb.append(" AND f.certificate_status = 'VERIFIED' ");
        }

        return sb.toString();
    }

    private String escapeLikeWildcards(String input) {
        return input
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    }

    private record ProductRowProjection(
        Long id,
        String name,
        Long categoryId,
        String categoryName,
        String unit,
        String description,
        Long batchId,
        UUID traceabilityCode,
        BigDecimal unitPrice,
        BigDecimal remainingQuantity,
        String batchStatus,
        LocalDate harvestDate,
        Long farmId,
        String farmName,
        String provinceName,
        boolean farmVerified,
        OffsetDateTime createdAt
    ) {}
}
