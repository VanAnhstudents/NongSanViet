package com.nsv.backend.product;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.product.dto.ComboItemRequest;
import com.nsv.backend.product.dto.ComboResponse;
import com.nsv.backend.product.dto.CreateComboRequest;
import com.nsv.backend.product.entity.Combo;
import com.nsv.backend.product.entity.ComboItem;
import com.nsv.backend.product.entity.Product;
import com.nsv.backend.product.repository.ComboRepository;
import com.nsv.backend.product.repository.ProductRepository;
import com.nsv.backend.product.service.ComboServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TcProd02Test {

    @Mock
    private ComboRepository comboRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ComboServiceImpl comboService;

    @Test
    @DisplayName("TC-PROD-02 (1): Farmer tạo combo gồm >= 2 sản phẩm với giá ưu đãi thành công")
    void createCombo_TwoOrMoreProducts_Success() {
        Long farmerId = 10L;
        CreateComboRequest request = new CreateComboRequest(
            "Combo Rau Củ Sạch Cuối Tuần",
            "Gồm cà chua và xà lách hữu cơ",
            new BigDecimal("45000.00"),
            List.of(
                new ComboItemRequest(101L, new BigDecimal("1.00")),
                new ComboItemRequest(102L, new BigDecimal("0.50"))
            )
        );

        Product p1 = Product.builder().id(101L).farmerId(farmerId).name("Cà chua bi").unit("kg").build();
        Product p2 = Product.builder().id(102L).farmerId(farmerId).name("Xà lách xoăn").unit("kg").build();

        when(productRepository.findAllByIdInAndFarmerId(Set.of(101L, 102L), farmerId))
            .thenReturn(List.of(p1, p2));
        when(comboRepository.save(any(Combo.class))).thenAnswer(inv -> {
            Combo saved = inv.getArgument(0);
            saved.setId(50L);
            return saved;
        });

        ComboResponse response = comboService.createCombo(farmerId, request);

        assertNotNull(response);
        assertEquals(50L, response.id());
        assertEquals("Combo Rau Củ Sạch Cuối Tuần", response.name());
        assertEquals(new BigDecimal("45000.00"), response.comboPrice());
        assertEquals(2, response.items().size());
        assertEquals("Cà chua bi", response.items().get(0).productName());
        assertEquals("Xà lách xoăn", response.items().get(1).productName());
        verify(comboRepository, times(1)).save(any(Combo.class));
    }

    @Test
    @DisplayName("TC-PROD-02 (1b): Từ chối tạo combo nếu số lượng sản phẩm thành phần < 2")
    void createCombo_LessThanTwoProducts_ThrowsBadRequest() {
        Long farmerId = 10L;
        CreateComboRequest request = new CreateComboRequest(
            "Combo Lỗi",
            null,
            new BigDecimal("20000.00"),
            List.of(new ComboItemRequest(101L, new BigDecimal("1.00")))
        );

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> comboService.createCombo(farmerId, request)
        );

        assertEquals("INSUFFICIENT_COMBO_ITEMS", ex.getCode());
        verify(comboRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-PROD-02 (2): Buyer xem chi tiết combo hiển thị đúng sản phẩm thành phần và giá")
    void getComboById_BuyerView_ReturnsExactItemsAndPrice() {
        Combo combo = Combo.builder()
            .id(50L)
            .farmerId(10L)
            .name("Combo Trái Cây Nhiệt Đới")
            .description("Xoài cát + Thanh long ruột đỏ")
            .comboPrice(new BigDecimal("85000.00"))
            .status("ACTIVE")
            .build();
        combo.addItem(ComboItem.builder().id(1L).productId(201L).quantity(new BigDecimal("2.00")).build());
        combo.addItem(ComboItem.builder().id(2L).productId(202L).quantity(new BigDecimal("1.50")).build());

        Product p1 = Product.builder().id(201L).name("Xoài cát Hòa Lộc").unit("kg").build();
        Product p2 = Product.builder().id(202L).name("Thanh long ruột đỏ").unit("kg").build();

        when(comboRepository.findWithItemsById(50L)).thenReturn(Optional.of(combo));
        when(productRepository.findAllByIdIn(Set.of(201L, 202L))).thenReturn(List.of(p1, p2));

        ComboResponse response = comboService.getComboById(50L);

        assertEquals(50L, response.id());
        assertEquals(new BigDecimal("85000.00"), response.comboPrice());
        assertEquals(2, response.items().size());
        assertEquals("Xoài cát Hòa Lộc", response.items().get(0).productName());
        assertEquals(new BigDecimal("2.00"), response.items().get(0).quantity());
        assertEquals("Thanh long ruột đỏ", response.items().get(1).productName());
        assertEquals(new BigDecimal("1.50"), response.items().get(1).quantity());
    }
}
