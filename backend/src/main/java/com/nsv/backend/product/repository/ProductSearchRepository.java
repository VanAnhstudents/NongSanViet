package com.nsv.backend.product.repository;

import com.nsv.backend.product.dto.ProductSearchCriteria;
import com.nsv.backend.product.dto.ProductSummaryResponse;
import com.nsv.backend.product.dto.SearchFilterOptionsResponse;

import java.util.List;

public interface ProductSearchRepository {
    List<ProductSummaryResponse> searchProducts(ProductSearchCriteria criteria);
    long countProducts(ProductSearchCriteria criteria);
    SearchFilterOptionsResponse findAvailableFilterOptions();
}
