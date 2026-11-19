package com.nsv.backend.product.service;

import com.nsv.backend.product.dto.CreateProductRequest;
import com.nsv.backend.product.dto.ProductSummaryResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    ProductSummaryResponse createProductInQuickFlow(Long farmerId, CreateProductRequest request);
    List<ProductSummaryResponse> getPublicProducts(
        String keyword,
        Long categoryId,
        Long provinceId,
        BigDecimal minPrice,
        BigDecimal maxPrice
    );
    ProductSummaryResponse getPublicProductDetail(Long productId);
    List<ProductSummaryResponse> getFarmerProducts(Long farmerId);
}
