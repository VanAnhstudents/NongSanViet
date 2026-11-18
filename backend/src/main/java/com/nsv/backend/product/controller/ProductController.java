package com.nsv.backend.product.controller;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.product.dto.CreateProductRequest;
import com.nsv.backend.product.dto.ProductSummaryResponse;
import com.nsv.backend.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Farmer: Đăng bán sản phẩm nhanh gọn trong 1 lần gửi (tối đa 3 bước giao diện)
    @PostMapping("/farmer/products")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<ProductSummaryResponse> createProduct(
            Authentication authentication,
            @Valid @RequestBody CreateProductRequest request
    ) {
        Long farmerId = extractUserId(authentication);
        ProductSummaryResponse response = productService.createProductInQuickFlow(farmerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Farmer: Xem danh sách sản phẩm đã đăng bán của trang trại mình
    @GetMapping("/farmer/products")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<List<ProductSummaryResponse>> getFarmerProducts(Authentication authentication) {
        Long farmerId = extractUserId(authentication);
        return ResponseEntity.ok(productService.getFarmerProducts(farmerId));
    }

    // Public: Hiển thị danh sách sản phẩm trên trang chủ và tìm kiếm theo từ khóa/danh mục/tỉnh/giá
    @GetMapping("/products")
    public ResponseEntity<List<ProductSummaryResponse>> getPublicProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long provinceId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        return ResponseEntity.ok(
            productService.getPublicProducts(keyword, categoryId, provinceId, minPrice, maxPrice)
        );
    }

    // Public: Xem chi tiết sản phẩm từ trang chủ / trang tìm kiếm
    @GetMapping("/products/{id}")
    public ResponseEntity<ProductSummaryResponse> getProductDetail(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getPublicProductDetail(id));
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Chưa xác thực người dùng");
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_SUBJECT", "Thông tin định danh không hợp lệ");
        }
    }
}
