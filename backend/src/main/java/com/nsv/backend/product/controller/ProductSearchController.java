package com.nsv.backend.product.controller;

import com.nsv.backend.product.dto.ProductSearchCriteria;
import com.nsv.backend.product.dto.ProductSearchResponse;
import com.nsv.backend.product.dto.SearchFilterOptionsResponse;
import com.nsv.backend.product.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    // Public: Buyer tìm kiếm theo từ khóa và kết hợp nhiều bộ lọc (loại, khoảng giá, khu vực, chứng nhận)
    @GetMapping("/search")
    public ResponseEntity<ProductSearchResponse> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long provinceId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "false") Boolean verifiedOnly,
            @RequestParam(required = false, defaultValue = "NEWEST") String sortBy,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "12") int size
    ) {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
            keyword,
            categoryId,
            provinceId,
            minPrice,
            maxPrice,
            verifiedOnly,
            sortBy,
            page,
            size
        );
        return ResponseEntity.ok(productSearchService.searchAndFilter(criteria));
    }

    // Public: Lấy danh sách danh mục và tỉnh/thành phố để hiển thị bộ lọc tìm kiếm
    @GetMapping("/filter-options")
    public ResponseEntity<SearchFilterOptionsResponse> getFilterOptions() {
        return ResponseEntity.ok(productSearchService.getFilterOptions());
    }
}
