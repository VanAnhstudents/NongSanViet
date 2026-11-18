package com.nsv.backend.product.service;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.product.repository.ProductModerationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductModerationService {

    private final ProductModerationRepository productModerationRepository;

    /**
     * Gỡ sản phẩm khỏi sàn: đóng mọi lô (batches.status = CLOSED) để Buyer không còn thấy hoặc mua được.
     * Trả về số lô đã đóng.
     */
    @Transactional
    public int removeFromSale(Long productId) {
        if (!productModerationRepository.productExists(productId)) {
            throw ApiException.notFound("Không tìm thấy sản phẩm");
        }
        return productModerationRepository.closeAllBatches(productId);
    }
}
