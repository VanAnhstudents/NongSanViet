package com.nsv.backend.product.service;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.product.dto.ComboItemRequest;
import com.nsv.backend.product.dto.ComboItemResponse;
import com.nsv.backend.product.dto.ComboResponse;
import com.nsv.backend.product.dto.CreateComboRequest;
import com.nsv.backend.product.entity.Combo;
import com.nsv.backend.product.entity.ComboItem;
import com.nsv.backend.product.entity.Product;
import com.nsv.backend.product.repository.ComboRepository;
import com.nsv.backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComboServiceImpl implements ComboService {

    private final ComboRepository comboRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ComboResponse createCombo(Long farmerId, CreateComboRequest request) {
        if (request.items() == null || request.items().size() < 2) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INSUFFICIENT_COMBO_ITEMS",
                "Combo phải bao gồm ít nhất 2 sản phẩm"
            );
        }

        Set<Long> uniqueProductIds = request.items().stream()
            .map(ComboItemRequest::productId)
            .collect(Collectors.toSet());

        if (uniqueProductIds.size() != request.items().size()) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "DUPLICATE_COMBO_PRODUCT",
                "Không được chọn trùng sản phẩm trong cùng một combo"
            );
        }

        List<Product> ownedProducts = productRepository.findAllByIdInAndFarmerId(uniqueProductIds, farmerId);
        if (ownedProducts.size() != uniqueProductIds.size()) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "INVALID_COMBO_PRODUCTS",
                "Một hoặc nhiều sản phẩm không tồn tại hoặc không thuộc trang trại của bạn"
            );
        }

        Combo combo = Combo.builder()
            .farmerId(farmerId)
            .name(request.name().trim())
            .description(request.description() != null ? request.description().trim() : null)
            .comboPrice(request.comboPrice())
            .status("ACTIVE")
            .build();

        for (ComboItemRequest itemReq : request.items()) {
            ComboItem item = ComboItem.builder()
                .productId(itemReq.productId())
                .quantity(itemReq.quantity())
                .build();
            combo.addItem(item);
        }

        Combo savedCombo = comboRepository.save(combo);
        Map<Long, Product> productMap = ownedProducts.stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

        return toResponse(savedCombo, productMap);
    }

    @Override
    @Transactional(readOnly = true)
    public ComboResponse getComboById(Long comboId) {
        Combo combo = comboRepository.findWithItemsById(comboId)
            .orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND,
                "COMBO_NOT_FOUND",
                "Không tìm thấy combo"
            ));

        Set<Long> productIds = combo.getItems().stream()
            .map(ComboItem::getProductId)
            .collect(Collectors.toSet());

        Map<Long, Product> productMap = productRepository.findAllByIdIn(productIds).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

        return toResponse(combo, productMap);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComboResponse> getActiveCombos() {
        List<Combo> combos = comboRepository.findAllByStatusOrderByCreatedAtDesc("ACTIVE");
        Set<Long> allProductIds = combos.stream()
            .flatMap(c -> c.getItems().stream())
            .map(ComboItem::getProductId)
            .collect(Collectors.toSet());

        Map<Long, Product> productMap = allProductIds.isEmpty()
            ? Map.of()
            : productRepository.findAllByIdIn(allProductIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        return combos.stream()
            .map(combo -> toResponse(combo, productMap))
            .toList();
    }

    private ComboResponse toResponse(Combo combo, Map<Long, Product> productMap) {
        List<ComboItemResponse> itemResponses = combo.getItems().stream()
            .map(item -> {
                Product product = productMap.get(item.getProductId());
                return new ComboItemResponse(
                    item.getProductId(),
                    product != null ? product.getName() : null,
                    product != null ? product.getUnit() : null,
                    item.getQuantity()
                );
            })
            .toList();

        return new ComboResponse(
            combo.getId(),
            combo.getFarmerId(),
            combo.getName(),
            combo.getDescription(),
            combo.getComboPrice(),
            combo.getStatus(),
            combo.getCreatedAt(),
            itemResponses
        );
    }
}
