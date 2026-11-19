package com.nsv.backend.order;

import com.nsv.backend.common.exception.BusinessException;
import com.nsv.backend.inventory.entity.Batch;
import com.nsv.backend.inventory.repository.BatchRepository;
import com.nsv.backend.order.dto.CheckoutRequest;
import com.nsv.backend.order.dto.OrderResponse;
import com.nsv.backend.order.entity.CartItem;
import com.nsv.backend.order.entity.Order;
import com.nsv.backend.order.repository.CartItemRepository;
import com.nsv.backend.order.repository.OrderRepository;
import com.nsv.backend.order.service.OrderServiceImpl;
import com.nsv.backend.product.entity.Product;
import com.nsv.backend.product.repository.ProductRepository;
import com.nsv.backend.user.entity.Address;
import com.nsv.backend.user.entity.Province;
import com.nsv.backend.user.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TC-STOCK-CONCURRENCY: Kiểm tra logic bảo vệ tồn kho khi nhiều Buyer đặt hàng cùng lúc (PBI-22).
 *
 * Mục tiêu (Bảng 2.3, độ đo 6): 0 đơn vượt tồn kho trong mọi lần chạy khi N ≥ 20 yêu cầu
 * đồng thời cho cùng một lô hàng. Cơ chế SELECT ... FOR UPDATE (BatchRepository) đảm bảo
 * tính toàn vẹn ở cấp CSDL; test này xác nhận logic nghiệp vụ từ chối đúng khi tồn kho cạn.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TC-STOCK-CONCURRENCY: Toàn vẹn tồn kho khi đặt hàng đồng thời (PBI-22, Độ đo 6)")
class StockConcurrencyTest {

    private static final Long BATCH_ID = 101L;
    private static final Long PRODUCT_ID = 1L;
    private static final Long ADDRESS_ID = 5L;
    private static final Long PROVINCE_ID = 30L;

    @Mock private CartItemRepository cartItemRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private BatchRepository batchRepository;
    @Mock private ProductRepository productRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Address address;
    private Product product;

    @BeforeEach
    void setUp() {
        address = Address.builder()
            .id(ADDRESS_ID)
            .userId(null) // userId sẽ gán qua findByIdAndUser
            .province(new Province(PROVINCE_ID, "Hà Nội"))
            .receiverName("Nguyễn Thị Test")
            .receiverPhone("0901234567")
            .addressLine("123 Trần Hưng Đạo, Hà Nội")
            .build();

        product = Product.builder()
            .id(PRODUCT_ID)
            .farmerId(10L)
            .name("Vải thiều Thanh Hà")
            .unit("kg")
            .build();

        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(PROVINCE_ID)))
            .thenReturn("Hà Nội");
        when(productRepository.findAllByIdIn(Set.of(PRODUCT_ID))).thenReturn(List.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId((long) (Math.random() * 10000 + 1));
            return o;
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-STOCK-01: Đặt hàng thành công khi tồn kho vừa đủ
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-STOCK-01: Buyer đặt hàng thành công khi tồn kho vừa đủ số lượng yêu cầu")
    void checkout_ExactRemainingStock_Succeeds() {
        // Tồn kho: 5 kg; Buyer đặt: 5 kg → vừa đủ, nên thành công và batch chuyển SOLD_OUT
        BigDecimal stock = new BigDecimal("5.00");
        BigDecimal orderQty = new BigDecimal("5.00");
        Long buyerId = 20L;

        CartItem cartItem = CartItem.builder()
            .id(11L).userId(buyerId).batchId(BATCH_ID).quantity(orderQty).build();
        Batch batch = Batch.builder()
            .id(BATCH_ID).productId(PRODUCT_ID)
            .unitPrice(new BigDecimal("40000.00"))
            .remainingQuantity(stock).status("AVAILABLE").build();

        when(cartItemRepository.findAllByUserIdOrderByIdAsc(buyerId)).thenReturn(List.of(cartItem));
        when(addressRepository.findByIdAndUser(ADDRESS_ID, buyerId)).thenReturn(Optional.of(address));
        when(batchRepository.findAllByIdInOrderByIdAscForUpdate(List.of(BATCH_ID))).thenReturn(List.of(batch));

        OrderResponse response = orderService.checkoutFromCart(buyerId,
            new CheckoutRequest(ADDRESS_ID, "COD", new BigDecimal("15000.00"), null));

        assertNotNull(response);
        assertEquals("PENDING", response.status());
        assertEquals(new BigDecimal("0.00"), batch.getRemainingQuantity());
        assertEquals("SOLD_OUT", batch.getStatus()); // Hết hàng → SOLD_OUT
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-STOCK-02: Từ chối khi tồn kho không đủ (giả lập request thứ hai sau khi hàng đã cạn)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-STOCK-02: Buyer thứ hai bị từ chối khi tồn kho đã cạn sau request đầu tiên")
    void checkout_SecondBuyerAfterStockDepleted_ThrowsInsufficientStock() {
        // Giả lập: Buyer A đã đặt hết 10 kg, Buyer B cố đặt thêm 5 kg
        // BatchRepository (SELECT ... FOR UPDATE) trả về batch với remainingQuantity = 0
        Long buyerA = 20L;
        Long buyerB = 21L;
        BigDecimal initialStock = new BigDecimal("10.00");

        // ── Buyer A đặt thành công ──
        CartItem itemA = CartItem.builder()
            .id(11L).userId(buyerA).batchId(BATCH_ID).quantity(initialStock).build();
        Batch batchForA = Batch.builder()
            .id(BATCH_ID).productId(PRODUCT_ID)
            .unitPrice(new BigDecimal("40000.00"))
            .remainingQuantity(initialStock).status("AVAILABLE").build();

        when(cartItemRepository.findAllByUserIdOrderByIdAsc(buyerA)).thenReturn(List.of(itemA));
        when(addressRepository.findByIdAndUser(ADDRESS_ID, buyerA)).thenReturn(Optional.of(address));
        when(batchRepository.findAllByIdInOrderByIdAscForUpdate(List.of(BATCH_ID))).thenReturn(List.of(batchForA));

        OrderResponse responseA = orderService.checkoutFromCart(buyerA,
            new CheckoutRequest(ADDRESS_ID, "COD", BigDecimal.ZERO, null));
        assertNotNull(responseA);
        assertEquals(new BigDecimal("0.00"), batchForA.getRemainingQuantity());

        // ── Buyer B đặt sau khi hàng đã hết: DB trả batch còn 0 kg (đã bị SELECT ... FOR UPDATE lock & trả về trạng thái mới nhất) ──
        CartItem itemB = CartItem.builder()
            .id(12L).userId(buyerB).batchId(BATCH_ID).quantity(new BigDecimal("5.00")).build();
        Batch batchForB = Batch.builder()
            .id(BATCH_ID).productId(PRODUCT_ID)
            .unitPrice(new BigDecimal("40000.00"))
            .remainingQuantity(BigDecimal.ZERO).status("SOLD_OUT").build();

        when(cartItemRepository.findAllByUserIdOrderByIdAsc(buyerB)).thenReturn(List.of(itemB));
        when(addressRepository.findByIdAndUser(ADDRESS_ID, buyerB)).thenReturn(Optional.of(address));
        when(batchRepository.findAllByIdInOrderByIdAscForUpdate(List.of(BATCH_ID))).thenReturn(List.of(batchForB));

        BusinessException ex = assertThrows(BusinessException.class, () ->
            orderService.checkoutFromCart(buyerB,
                new CheckoutRequest(ADDRESS_ID, "COD", BigDecimal.ZERO, null))
        );

        // Từ chối vì SOLD_OUT
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("BATCH_NOT_AVAILABLE", ex.getCode());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-STOCK-03: Từ chối khi số lượng đặt vượt tồn kho còn lại
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-STOCK-03: Buyer đặt số lượng vượt tồn kho còn lại bị từ chối (INSUFFICIENT_STOCK)")
    void checkout_QuantityExceedsRemainingStock_ThrowsBusinessException() {
        Long buyerId = 20L;
        BigDecimal remaining = new BigDecimal("3.00");
        BigDecimal orderQty = new BigDecimal("5.00"); // Muốn đặt nhiều hơn tồn kho

        CartItem cartItem = CartItem.builder()
            .id(11L).userId(buyerId).batchId(BATCH_ID).quantity(orderQty).build();
        Batch batch = Batch.builder()
            .id(BATCH_ID).productId(PRODUCT_ID)
            .unitPrice(new BigDecimal("40000.00"))
            .remainingQuantity(remaining).status("AVAILABLE").build();

        when(cartItemRepository.findAllByUserIdOrderByIdAsc(buyerId)).thenReturn(List.of(cartItem));
        when(addressRepository.findByIdAndUser(ADDRESS_ID, buyerId)).thenReturn(Optional.of(address));
        when(batchRepository.findAllByIdInOrderByIdAscForUpdate(List.of(BATCH_ID))).thenReturn(List.of(batch));

        BusinessException ex = assertThrows(BusinessException.class, () ->
            orderService.checkoutFromCart(buyerId,
                new CheckoutRequest(ADDRESS_ID, "COD", BigDecimal.ZERO, null))
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("INSUFFICIENT_STOCK", ex.getCode());
        // Tồn kho không thay đổi vì đơn bị từ chối
        assertEquals(remaining, batch.getRemainingQuantity());
        verify(orderRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-STOCK-04: N đơn liên tiếp trên cùng 1 lô - chỉ k đơn đầu thành công, còn lại bị từ chối
    // (Giả lập kết quả mà SELECT ... FOR UPDATE sẽ cung cấp từng bước)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-STOCK-04: Trong N=20 đơn trên cùng 1 lô 10kg, chỉ đúng 5 đơn (2kg/đơn) thành công; 15 đơn bị từ chối - không đơn nào vượt tồn kho")
    void checkout_TwentySequentialRequests_OnlyFiveSucceed_ZeroOverstock() {
        final int TOTAL_REQUESTS = 20;
        final BigDecimal QTY_PER_ORDER = new BigDecimal("2.00");
        final BigDecimal INITIAL_STOCK = new BigDecimal("10.00"); // 10 kg / 2 kg/đơn = 5 đơn thành công

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // Trạng thái tồn kho được chia sẻ giữa các "request" (giả lập SELECT ... FOR UPDATE)
        final BigDecimal[] remaining = {INITIAL_STOCK};

        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            Long buyerId = 20L + i;
            Long cartItemId = 11L + i;
            Long orderId = 1000L + i;

            // Giả lập kết quả của SELECT ... FOR UPDATE: trả về tồn kho hiện tại
            final BigDecimal currentRemaining = remaining[0];
            final String currentStatus = currentRemaining.compareTo(BigDecimal.ZERO) > 0 ? "AVAILABLE" : "SOLD_OUT";

            CartItem cartItem = CartItem.builder()
                .id(cartItemId).userId(buyerId).batchId(BATCH_ID).quantity(QTY_PER_ORDER).build();
            Batch batch = Batch.builder()
                .id(BATCH_ID).productId(PRODUCT_ID)
                .unitPrice(new BigDecimal("40000.00"))
                .remainingQuantity(new BigDecimal(currentRemaining.toPlainString()))
                .status(currentStatus)
                .build();

            final Long finalBuyerId = buyerId;
            when(cartItemRepository.findAllByUserIdOrderByIdAsc(finalBuyerId)).thenReturn(List.of(cartItem));
            when(addressRepository.findByIdAndUser(ADDRESS_ID, finalBuyerId)).thenReturn(Optional.of(address));
            when(batchRepository.findAllByIdInOrderByIdAscForUpdate(List.of(BATCH_ID))).thenReturn(List.of(batch));

            final Long finalOrderId = orderId;
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                o.setId(finalOrderId);
                return o;
            });

            try {
                orderService.checkoutFromCart(buyerId,
                    new CheckoutRequest(ADDRESS_ID, "COD", BigDecimal.ZERO, null));
                successCount.incrementAndGet();
                // Cập nhật tồn kho sau khi đặt thành công (giả lập COMMIT)
                remaining[0] = remaining[0].subtract(QTY_PER_ORDER);
            } catch (BusinessException ex) {
                failCount.incrementAndGet();
                // Kiểm tra lỗi hợp lệ
                assertTrue(
                    "INSUFFICIENT_STOCK".equals(ex.getCode()) || "BATCH_NOT_AVAILABLE".equals(ex.getCode()),
                    "Lỗi phải là INSUFFICIENT_STOCK hoặc BATCH_NOT_AVAILABLE, nhưng là: " + ex.getCode()
                );
            }
        }

        // Kết quả mong đợi theo Bảng 2.3 độ đo 6: 0 đơn vượt tồn kho
        assertEquals(5, successCount.get(),
            "Chỉ 5 đơn (10kg / 2kg) được xác nhận thành công");
        assertEquals(15, failCount.get(),
            "15 đơn còn lại bị từ chối do không đủ tồn kho");
        assertEquals(new BigDecimal("0.00"), remaining[0],
            "Tồn kho sau 5 đơn thành công phải về 0, không vượt âm");

        // Không đơn nào vượt tồn kho (invariant chính của độ đo 6)
        assertTrue(remaining[0].compareTo(BigDecimal.ZERO) >= 0,
            "Tồn kho không được âm (không đơn nào vượt tồn kho)");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-STOCK-05: Batch trạng thái PRE_ORDER cũng kiểm tra tồn kho đúng
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-STOCK-05: Lô hàng PRE_ORDER cũng bị từ chối khi vượt tồn kho dự kiến")
    void checkout_PreOrderBatchInsufficientStock_ThrowsConflict() {
        Long buyerId = 20L;
        BigDecimal remaining = new BigDecimal("1.00");
        BigDecimal orderQty = new BigDecimal("3.00");

        CartItem cartItem = CartItem.builder()
            .id(11L).userId(buyerId).batchId(BATCH_ID).quantity(orderQty).build();
        Batch batch = Batch.builder()
            .id(BATCH_ID).productId(PRODUCT_ID)
            .unitPrice(new BigDecimal("60000.00"))
            .remainingQuantity(remaining).status("PRE_ORDER").build();

        when(cartItemRepository.findAllByUserIdOrderByIdAsc(buyerId)).thenReturn(List.of(cartItem));
        when(addressRepository.findByIdAndUser(ADDRESS_ID, buyerId)).thenReturn(Optional.of(address));
        when(batchRepository.findAllByIdInOrderByIdAscForUpdate(List.of(BATCH_ID))).thenReturn(List.of(batch));

        BusinessException ex = assertThrows(BusinessException.class, () ->
            orderService.checkoutFromCart(buyerId,
                new CheckoutRequest(ADDRESS_ID, "COD", BigDecimal.ZERO, null))
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("INSUFFICIENT_STOCK", ex.getCode());
        // Tồn kho không bị trừ khi đơn bị từ chối
        assertEquals(remaining, batch.getRemainingQuantity());
        assertEquals("PRE_ORDER", batch.getStatus());
    }
}
