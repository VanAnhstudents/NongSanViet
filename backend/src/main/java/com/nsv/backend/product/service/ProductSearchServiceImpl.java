package com.nsv.backend.product.service;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.product.dto.ProductSearchCriteria;
import com.nsv.backend.product.dto.ProductSearchResponse;
import com.nsv.backend.product.dto.ProductSearchResponse.AppliedFiltersSummary;
import com.nsv.backend.product.dto.ProductSummaryResponse;
import com.nsv.backend.product.dto.SearchFilterOptionsResponse;
import com.nsv.backend.product.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private static final Set<String> VALID_SORT_OPTIONS = Set.of("NEWEST", "PRICE_ASC", "PRICE_DESC");

    private final ProductSearchRepository productSearchRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductSearchResponse searchAndFilter(ProductSearchCriteria criteria) {
        validateCriteria(criteria);

        long totalElements = productSearchRepository.countProducts(criteria);
        List<ProductSummaryResponse> items = totalElements > 0
            ? productSearchRepository.searchProducts(criteria)
            : List.of();

        int totalPages = totalElements == 0
            ? 0
            : (int) Math.ceil((double) totalElements / criteria.size());

        AppliedFiltersSummary summary = new AppliedFiltersSummary(
            criteria.normalizedKeyword(),
            criteria.categoryId(),
            criteria.provinceId(),
            criteria.minPrice(),
            criteria.maxPrice(),
            Boolean.TRUE.equals(criteria.verifiedOnly()),
            criteria.normalizedSortBy()
        );

        return new ProductSearchResponse(
            items,
            totalElements,
            totalPages,
            criteria.page(),
            criteria.size(),
            summary
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SearchFilterOptionsResponse getFilterOptions() {
        return productSearchRepository.findAvailableFilterOptions();
    }

    private void validateCriteria(ProductSearchCriteria criteria) {
        if (criteria.minPrice() != null && criteria.minPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_PRICE_RANGE",
                "Giá tối thiểu không được nhỏ hơn 0"
            );
        }
        if (criteria.maxPrice() != null && criteria.maxPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_PRICE_RANGE",
                "Giá tối đa không được nhỏ hơn 0"
            );
        }
        if (criteria.minPrice() != null && criteria.maxPrice() != null
                && criteria.minPrice().compareTo(criteria.maxPrice()) > 0) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_PRICE_RANGE",
                "Giá tối thiểu không được lớn hơn giá tối đa"
            );
        }
        if (criteria.page() < 0) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_PAGINATION",
                "Số trang (page) phải lớn hơn hoặc bằng 0"
            );
        }
        if (criteria.size() < 1 || criteria.size() > 100) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_PAGINATION",
                "Kích thước trang (size) phải nằm trong khoảng từ 1 đến 100"
            );
        }
        if (!VALID_SORT_OPTIONS.contains(criteria.normalizedSortBy())) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_SORT_OPTION",
                "Tiêu chí sắp xếp chỉ hỗ trợ: NEWEST, PRICE_ASC, PRICE_DESC"
            );
        }
    }
}
