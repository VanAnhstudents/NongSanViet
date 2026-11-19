package com.nsv.backend.product.service;

import com.nsv.backend.product.dto.ProductSearchCriteria;
import com.nsv.backend.product.dto.ProductSearchResponse;
import com.nsv.backend.product.dto.SearchFilterOptionsResponse;

public interface ProductSearchService {
    ProductSearchResponse searchAndFilter(ProductSearchCriteria criteria);
    SearchFilterOptionsResponse getFilterOptions();
}
