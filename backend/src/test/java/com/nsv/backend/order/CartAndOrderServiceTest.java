package com.nsv.backend.order;

import com.nsv.backend.inventory.entity.Batch;
import com.nsv.backend.inventory.repository.BatchRepository;
import com.nsv.backend.order.dto.*;
import com.nsv.backend.order.entity.CartItem;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.repository.CartItemRepository;
import com.nsv.backend.order.repository.OrderRepository;
import com.nsv.backend.order.service.CartServiceImpl;
import com.nsv.backend.order.service.OrderServiceImpl;
import com.nsv.backend.product.entity.Product;
import com.nsv.backend.product.repository.ProductRepository;
import com.nsv.backend.user.entity.Address;
import com.nsv.backend.user.repository.AddressRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartAndOrderServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private CartServiceImpl cartService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("TC-ORD-01 (1): Thêm/sửa/xóa sản phẩm trong giỏ hàng cập nhật tổng tiền chính xác")
    void cartOperations_RecalculateTotalAccurately() {
        Long buyerId = 20L;
        Batch batch1 = Batch.builder()
            .id(101L).productId(1L).unitPrice(new BigDecimal("30000.00"))
            .remainingQuantity(new BigDecimal("50.00")).status("AVAILABLE").build();
        Batch batch2 = Batch.builder()
            .id(102L).productId(2L).unitPrice(new BigDecimal("25000.00"))
            .remainingQuantity(new BigDecimal("40.00")).status("AVAILABLE").build();

        Product p1 = Product.builder().id(1L).name("Vải thiều Thanh Hà").unit("kg").build();
        Product p2 = Product.builder().id(2L).name("Ổi bo Hải Phòng").unit("kg").build();

        CartItem item1 = CartItem.builder().id(11L).userId(buyerId).batchId(101L).quantity(new BigDecimal("2.00")).build();
        CartItem item2 = CartItem.builder().id(12L).userId(buyerId).batchId(102L).quantity(new BigDecimal("3.00")).build();

        // 1. Kiểm tra giỏ hàng có 2 sản phẩm: 2*30000 + 3*25000 = 135000.00
        when(cartItemRepository.findAllByUserIdOrderByIdAsc(buyerId)).thenReturn(List.of(item1, item2));
        when(batchRepository.findAllByIdIn(Set.of(101L, 102L))).thenReturn(List.of(batch1, batch2));
        when(productRepository.findAllByIdIn(Set.of(1L, 2L))).thenReturn(List.of(p1, p2));

        CartResponse initialCart = cartService.getCart(buyerId);
        assertEquals(2, initialCart.totalItems());
        assertEquals(new BigDecimal("135000.00"), initialCart.totalAmount());

        // 2. Sửa số lượng item1 từ 2.00 -> 4.00 => 4*30000 + 3*25000 = 195000.00
        when(cartItemRepository.findByIdAndUserId(11L, buyerId)).thenReturn(Optional.of(item1));
        when(batchRepository.findById(101L)).thenReturn(Optional.of(batch1));

        CartResponse updatedCart = cartService.updateItemQuantity(
            buyerId, 11L, new UpdateCartItemRequest(new BigDecimal("4.00"))
        );
        assertEquals(new BigDecimal("195000.00"), updatedCart.totalAmount());

        // 3. Xóa item2 khỏi giỏ => chỉ còn item1: 4*30000 = 120000.00
        when(cartItemRepository.findByIdAndUserId(12L, buyerId)).thenReturn(Optional.of(item2));
        when(cartItemRepository.findAllByUserIdOrderByIdAsc(buyerId)).thenReturn(List.of(item1));
        when(batchRepository.findAllByIdIn(Set.of(101L))).thenReturn(List.of(batch1));
        when(productRepository.findAllByIdIn(Set.of(1L))).thenReturn(List.of(p1));

        CartResponse afterRemoveCart = cartService.removeItem(buyerId, 12L);
        assertEquals(1, afterRemoveCart.totalItems());
        assertEquals(new BigDecimal("120000.00"), afterRemoveCart.totalAmount());
        verify(cartItemRepository).delete(item2);
    }

    @Test
    @DisplayName("TC-ORD-01 (2): Đặt hàng từ giỏ hàng tạo đơn đủ sản phẩm, số lượng, trừ kho và xóa giỏ")
    void checkoutFromCart_MultipleItems_CreatesOrderAndDeductsStock() {
        Long buyerId = 20L;
        CartItem item1 = CartItem.builder().id(11L).userId(buyerId).batchId(101L).quantity(new BigDecimal("2.00")).build();
        CartItem item2 = CartItem.builder().id(12L).userId(buyerId).batchId(102L).quantity(new BigDecimal("1.50")).build();

        Batch batch1 = Batch.builder()
            .id(101L).productId(1L).unitPrice(new BigDecimal("30000.00"))
            .remainingQuantity(new BigDecimal("10.00")).status("AVAILABLE").build();
        Batch batch2 = Batch.builder()
            .id(102L).productId(2L).unitPrice(new BigDecimal("40000.00"))
            .remainingQuantity(new BigDecimal("1.50")).status("AVAILABLE").build();

        Product p1 = Product.builder().id(1L).name("Cà chua hữu cơ").unit("kg").build();
        Product p2 = Product.builder().id(2L).name("Dâu tây Mộc Châu").unit("hộp").build();

        Address address = Address.builder()
            .id(5L).userId(buyerId).provinceId(30L)
            .receiverName("Nguyễn Thế Quyền").receiverPhone("0987654321")
            .addressLine("Kim Thành, Hải Phòng").build();

        when(cartItemRepository.findAllByUserIdOrderByIdAsc(buyerId)).thenReturn(List.of(item1, item2));
        when(addressRepository.findByIdAndUserId(5L, buyerId)).thenReturn(Optional.of(address));
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(30L))).thenReturn("Hải Phòng");
        when(batchRepository.findAllByIdInOrderByIdAscForUpdate(List.of(101L, 102L)))
            .thenReturn(List.of(batch1, batch2));
        when(productRepository.findAllByIdIn(Set.of(1L, 2L))).thenReturn(List.of(p1, p2));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(999L);
            return o;
        });

        CheckoutRequest request = new CheckoutRequest(5L, "COD", new BigDecimal("15000.00"), null);
        OrderResponse response = orderService.checkoutFromCart(buyerId, request);

        assertNotNull(response);
        assertEquals(999L, response.id());
        assertEquals("PENDING", response.status());
        assertEquals(2, response.items().size());
        // 2 * 30000 + 1.5 * 40000 = 120000; + 15000 ship = 135000
        assertEquals(new BigDecimal("120000.00"), response.itemsSubtotal());
        assertEquals(new BigDecimal("135000.00"), response.totalAmount());
        assertEquals("Cà chua hữu cơ", response.items().get(0).productName());
        assertEquals(new BigDecimal("2.00"), response.items().get(0).quantity());
        assertEquals("Dâu tây Mộc Châu", response.items().get(1).productName());
        assertEquals(new BigDecimal("1.50"), response.items().get(1).quantity());

        // Kiểm tra trừ tồn kho và tự động chuyển SOLD_OUT khi về 0
        assertEquals(new BigDecimal("8.00"), batch1.getRemainingQuantity());
        assertEquals("AVAILABLE", batch1.getStatus());
        assertEquals(new BigDecimal("0.00"), batch2.getRemainingQuantity());
        assertEquals("SOLD_OUT", batch2.getStatus());

        // Kiểm tra đã xóa giỏ hàng
        verify(cartItemRepository, times(1)).deleteAll(List.of(item1, item2));
    }
}
