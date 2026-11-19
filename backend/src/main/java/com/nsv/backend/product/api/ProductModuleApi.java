package com.nsv.backend.product.api;

import com.nsv.backend.product.api.dto.ProductBriefDto;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface ProductModuleApi {
    Optional<ProductBriefDto> findProductBrief(Long productId);
    Map<Long, ProductBriefDto> findProductBriefsByIds(Collection<Long> productIds);
    boolean isProductOwnedByFarmer(Long productId, Long farmerId);
}
