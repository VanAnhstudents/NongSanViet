package com.nsv.backend.product;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.product.dto.ProductSearchCriteria;
import com.nsv.backend.product.dto.ProductSearchResponse;
import com.nsv.backend.product.dto.ProductSummaryResponse;
import com.nsv.backend.product.dto.SearchFilterOptionsResponse;
import com.nsv.backend.product.dto.SearchFilterOptionsResponse.FilterOptionItem;
import com.nsv.backend.product.repository.ProductSearchRepository;
import com.nsv.backend.product.service.ProductSearchServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TcSearch01Test {

    @Mock
    private ProductSearchRepository productSearchRepository;

    @InjectMocks
    private ProductSearchServiceImpl productSearchService;

    private ProductSummaryResponse sampleProduct(
        Long id,
        String name,
        Long categoryId,
        String categoryName,
        BigDecimal price,
        String provinceName,
        boolean verified
    ) {
        return new ProductSummaryResponse(
            id,
            name,
            categoryId,
            categoryName,
            "kg",
            "Nông sản tươi sạch thu hoạch trong ngày",
            "https://cdn.nongsanviet.vn/products/" + id + ".jpg",
            List.of("https://cdn.nongsanviet.vn/products/" + id + ".jpg"),
            5000L + id,
            UUID.randomUUID(),
            price,
            new BigDecimal("100.00"),
            "AVAILABLE",
            LocalDate.of(2026, 6, 15),
            10L + id,
            "Trang trại " + provinceName,
            provinceName,
            verified,
            OffsetDateTime.now()
        );
    }

    @Test
    @DisplayName("TC-SEARCH-01 (1a): Tìm kiếm theo từ khóa trả về đúng các sản phẩm chứa từ khóa trong tên")
    void searchByKeyword_MatchingProductNames_ReturnsFilteredItems() {
        ProductSummaryResponse p1 = sampleProduct(
            1L, "Vải thiều Thanh Hà chính vụ", 2L, "Trái cây", new BigDecimal("42000.00"), "Hải Phòng", true
        );
        ProductSummaryResponse p2 = sampleProduct(
            2L, "Vải thiều u hồng chín sớm", 2L, "Trái cây", new BigDecimal("48000.00"), "Bắc Giang", true
        );

        ProductSearchCriteria criteria = new ProductSearchCriteria(
            "  Vải thiều  ", null, null, null, null, false, "NEWEST", 0, 12
        );

        when(productSearchRepository.countProducts(criteria)).thenReturn(2L);
        when(productSearchRepository.searchProducts(criteria)).thenReturn(List.of(p1, p2));

        ProductSearchResponse response = productSearchService.searchAndFilter(criteria);

        assertEquals(2L, response.totalElements());
        assertEquals(1, response.totalPages());
        assertEquals(2, response.items().size());
        assertEquals("Vải thiều", response.appliedFilters().keyword());
        assertTrue(response.items().stream().allMatch(item -> item.name().toLowerCase().contains("vải thiều")));
    }

    @Test
    @DisplayName("TC-SEARCH-01 (1b): Tìm kiếm từ khóa không có sản phẩm phù hợp trả về danh sách rỗng mà không gọi truy vấn trang")
    void searchByKeyword_NoMatches_ReturnsEmptyPageWithoutExtraQuery() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
            "Sầu riêng Musang King", null, null, null, null, false, "NEWEST", 0, 12
        );

        when(productSearchRepository.countProducts(criteria)).thenReturn(0L);

        ProductSearchResponse response = productSearchService.searchAndFilter(criteria);

        assertEquals(0L, response.totalElements());
        assertEquals(0, response.totalPages());
        assertTrue(response.items().isEmpty());
        verify(productSearchRepository, never()).searchProducts(any());
    }

    @Test
    @DisplayName("TC-SEARCH-01 (2a): Kết hợp 2 bộ lọc đồng thời (loại sản phẩm + khoảng giá) trả về đúng kết quả")
    void combineCategoryAndPriceRangeFilters_AppliesBothFiltersSimultaneously() {
        Long fruitCategoryId = 2L;
        BigDecimal minPrice = new BigDecimal("30000.00");
        BigDecimal maxPrice = new BigDecimal("50000.00");

        ProductSummaryResponse matched1 = sampleProduct(
            10L, "Ổi bo Hải Phòng", fruitCategoryId, "Trái cây", new BigDecimal("32000.00"), "Hải Phòng", true
        );
        ProductSummaryResponse matched2 = sampleProduct(
            11L, "Na dai Đông Triều", fruitCategoryId, "Trái cây", new BigDecimal("45000.00"), "Quảng Ninh", false
        );

        ProductSearchCriteria criteria = new ProductSearchCriteria(
            null, fruitCategoryId, null, minPrice, maxPrice, false, "PRICE_ASC", 0, 12
        );

        when(productSearchRepository.countProducts(criteria)).thenReturn(2L);
        when(productSearchRepository.searchProducts(criteria)).thenReturn(List.of(matched1, matched2));

        ProductSearchResponse response = productSearchService.searchAndFilter(criteria);

        ArgumentCaptor<ProductSearchCriteria> captor = ArgumentCaptor.forClass(ProductSearchCriteria.class);
        verify(productSearchRepository).searchProducts(captor.capture());

        ProductSearchCriteria passed = captor.getValue();
        assertEquals(fruitCategoryId, passed.categoryId());
        assertEquals(minPrice, passed.minPrice());
        assertEquals(maxPrice, passed.maxPrice());
        assertEquals("PRICE_ASC", passed.normalizedSortBy());

        assertEquals(2L, response.totalElements());
        assertEquals(fruitCategoryId, response.appliedFilters().categoryId());
        assertEquals(minPrice, response.appliedFilters().minPrice());
        assertEquals(maxPrice, response.appliedFilters().maxPrice());
        assertTrue(response.items().stream().allMatch(
            item -> item.categoryId().equals(fruitCategoryId)
                && item.unitPrice().compareTo(minPrice) >= 0
                && item.unitPrice().compareTo(maxPrice) <= 0
        ));
    }

    @Test
    @DisplayName("TC-SEARCH-01 (2b): Kết hợp đầy đủ 4 bộ lọc (tên + loại + khoảng giá + khu vực) cùng lúc")
    void combineAllFourFilters_KeywordCategoryPriceProvince_ReturnsExactMatch() {
        ProductSummaryResponse exactMatch = sampleProduct(
            21L, "Vải thiều Thanh Hà VietGAP", 2L, "Trái cây", new BigDecimal("42000.00"), "Hải Phòng", true
        );

        ProductSearchCriteria criteria = new ProductSearchCriteria(
            "Vải thiều",
            2L,
            30L, // Hải Phòng
            new BigDecimal("35000.00"),
            new BigDecimal("50000.00"),
            true,
            "PRICE_ASC",
            0,
            10
        );

        when(productSearchRepository.countProducts(criteria)).thenReturn(1L);
        when(productSearchRepository.searchProducts(criteria)).thenReturn(List.of(exactMatch));

        ProductSearchResponse response = productSearchService.searchAndFilter(criteria);

        assertEquals(1L, response.totalElements());
        assertEquals("Vải thiều Thanh Hà VietGAP", response.items().get(0).name());
        assertEquals("Hải Phòng", response.items().get(0).provinceName());
        assertTrue(response.items().get(0).farmVerified());
    }

    @Test
    @DisplayName("TC-SEARCH-01 (2c): Chặn khoảng giá không hợp lệ (minPrice > maxPrice hoặc giá âm)")
    void invalidPriceRange_ThrowsBadRequest() {
        ProductSearchCriteria invertedRange = new ProductSearchCriteria(
            null, 2L, null, new BigDecimal("80000.00"), new BigDecimal("30000.00"), false, "NEWEST", 0, 12
        );

        BusinessException ex1 = assertThrows(
            BusinessException.class,
            () -> productSearchService.searchAndFilter(invertedRange)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex1.getStatus());
        assertEquals("INVALID_PRICE_RANGE", ex1.getCode());

        ProductSearchCriteria negativePrice = new ProductSearchCriteria(
            null, null, null, new BigDecimal("-5000.00"), null, false, "NEWEST", 0, 12
        );

        BusinessException ex2 = assertThrows(
            BusinessException.class,
            () -> productSearchService.searchAndFilter(negativePrice)
        );
        assertEquals("INVALID_PRICE_RANGE", ex2.getCode());
        verify(productSearchRepository, never()).countProducts(any());
    }

    @Test
    @DisplayName("TC-SEARCH-01 (2d): Lấy danh sách tùy chọn bộ lọc (danh mục và tỉnh/thành phố)")
    void getFilterOptions_ReturnsCategoriesAndProvinces() {
        SearchFilterOptionsResponse mockOptions = new SearchFilterOptionsResponse(
            List.of(new FilterOptionItem(1L, "Rau củ"), new FilterOptionItem(2L, "Trái cây")),
            List.of(new FilterOptionItem(1L, "Hà Nội"), new FilterOptionItem(30L, "Hải Phòng"))
        );

        when(productSearchRepository.findAvailableFilterOptions()).thenReturn(mockOptions);

        SearchFilterOptionsResponse result = productSearchService.getFilterOptions();

        assertEquals(2, result.categories().size());
        assertEquals(2, result.provinces().size());
    }
}
