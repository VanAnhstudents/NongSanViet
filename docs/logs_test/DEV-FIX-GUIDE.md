# Hướng dẫn sửa để toàn bộ test Pass — Dành cho dev

Tài liệu này đi kèm `docs/TestReport.md` (bảng Pass/NotPass) và `docs/TestCases-Manual-Frontend.md`.
Làm theo thứ tự Bước 1 → 2 → 3.

---

## Bước 1 — Sửa main cho build được (BẮT BUỘC, đang chặn mọi test)

`mvn test-compile` đang lỗi ở pha biên dịch **main** (không phải test). Nguyên nhân: một lần
refactor dở dang — migrasi enum `OrderStatus/PaymentStatus/RefundStatus`, tách
`RefundQueryRepository/RefundPaymentRepository`, bỏ field `auditLogService` — nhưng nhiều call
site chưa cập nhật.

### 1.1 Sửa bắt buộc (cả 2 hướng dưới đều cần)

**`order/repository/RefundRepository.java`** — thêm 2 method query (hiện interface trống):
```java
import java.util.List;
import java.util.Set;
// ...
List<Refund> findAllByPaymentIdOrderByCreatedAtAsc(Long paymentId);
List<Refund> findAllByPaymentIdInOrderByCreatedAtAsc(Set<Long> paymentIds);
```

**`common/contract/JdbcAuditAndNotificationAdapter.java`** — class đang dùng biến `auditLogService`
chưa khai báo (dòng 69, 84). Thêm field (interface có `recordAdminAction`/`recordFinancialTransaction`)
vào các final field để `@RequiredArgsConstructor` inject:
```java
private final <đúng interface AuditLogService> auditLogService;
```

### 1.2 Chọn 1 hướng nhất quán cho kiểu `status` của Order/Payment/Refund

#### Hướng A — Hoàn trả về String (nhanh, ít file) — khuyến nghị nếu cần chạy gấp
- **`order/entity/Refund.java`**: `status` `RefundStatus` → `String`; `createdAt`/`processedAt`
  `Instant` → `OffsetDateTime` (bỏ `@Enumerated`, đổi import `Instant`→`OffsetDateTime`).
- **`order/dto/ShortageRefundResponse.java`**: `OrderStatus orderStatus` → `String orderStatus`.
- **`order/service/PreorderRefundService.java`**: đổi `PaymentStatus.PAID`→`"PAID"`,
  `OrderStatus.CANCELLED`→`"CANCELLED"`, `RefundStatus.COMPLETED`→`"COMPLETED"`,
  `PaymentStatus.REFUNDED`→`"REFUNDED"`; `Instant now = Instant.now(clock)` →
  `OffsetDateTime now = OffsetDateTime.now(clock)`.
- **`order/repository/RefundPaymentRepository.java`**: `findByOrderIdAndStatus(Long, PaymentStatus)`
  → `findByOrderIdAndStatus(Long, String)`.
- **Cập nhật test `TcRefund01Test`** sang String: `assertThat(...).isEqualTo(OrderStatus.CANCELLED)`
  → `"CANCELLED"`; `PaymentStatus.PAID`→`"PAID"`; `RefundStatus.COMPLETED`→`"COMPLETED"`;
  `findByOrderIdAndStatus(ORDER_ID, "PAID")`; `Payment.builder().status("PAID")`;
  `new ShortageRefundResponse(orderId, "CANCELLED", …)`.
- `RefundResponse`, `FinancialTransactionServiceImpl`, `DisputeServiceImpl` **tự hết** (đang dùng String).

#### Hướng B — Hoàn tất sang enum (khớp enum đã tạo, nhiều file hơn)
- Migrasi `Order.status`→`OrderStatus`, `Payment.status`→`PaymentStatus`; `OrderResponse.orderStatus`
  →`OrderStatus`; cập nhật `OrderServiceImpl` (so sánh/`FARMER_ALLOWED_TRANSITIONS`/`setStatus`),
  `FinancialTransactionServiceImpl` (`"PAID".equals(p.getStatus())`→`p.getStatus()==PaymentStatus.PAID`,
  `refund.setStatus("COMPLETED")`→`RefundStatus.COMPLETED`, `processedAt`→`Instant`),
  `DisputeServiceImpl` (Refund/Payment/Order enum), `RefundResponse.status`→`RefundStatus`.
- Cập nhật test sang enum: `TcOrd01Test`, `TcOrd02Test`, `TcOrd03Test`, `TcPay01Test`, `TcNoti01Test`,
  `TcFin01Test`, `TcDispute01Test` (test 3), `TcComplain01Test`. **Giữ nguyên `TcRefund01Test`**.

> Ghi chú: các file enum (`OrderStatus`/`PaymentStatus`/`RefundStatus`) đã được tạo và
> `Refund`/`PreorderRefundService`/`ShortageRefundResponse`/`TcRefund01Test` đã dùng enum → Hướng B
> có vẻ là intent ban đầu; nhưng Hướng A ít công hơn. Đội tự chọn 1 hướng và làm hết.

### 1.3 Kiểm tra
```
cd backend
.\mvnw.cmd test-compile
```
Phải không còn lỗi compile mới sang Bước 2.

---

## Bước 2 — Sửa logic cho TC NotPass (không phụ thuộc hướng ở Bước 1)

### TC-CHECK-02 (PBI-26) — chưa có code backend
- `order/entity/OrderCoInspection.java`: thêm cột `result` (PASS/FAIL), `result_note`,
  `completed_by`, `completed_at` + migration Flyway.
- `order/service/CoInspectionServiceImpl.java`: thêm `recordCoInspectionResult(buyerId, orderId,
  result, note)` — chỉ cho khi `status=ACKNOWLEDGED`; set `status=COMPLETED`+kết quả; khoá không sửa lại.
- `order/service/OrderServiceImpl.java`: thêm `confirmDelivery(buyerId, orderId)` chuyển
  `DELIVERED→COMPLETED` (hiện chỉ Farmer đẩy tới DELIVERED).
- Sau đó viết `TcCheck02Test` (theo mẫu `TcCheck01Test`).

### TC-COMPLAIN-01 (PBI-29) — thiếu kiểm tra hạn 24h
Trong `dispute/service/DisputeServiceImpl.java::createComplaint`, sau đoạn kiểm tra trạng thái đơn, thêm:
```java
if (order.getDeliveredAt() != null &&
    OffsetDateTime.now().isAfter(order.getDeliveredAt().plusHours(24))) {
    throw new BusinessException(HttpStatus.BAD_REQUEST, "COMPLAINT_WINDOW_EXPIRED",
        "Đã quá 24h kể từ khi nhận hàng, không thể khiếu nại");
}
```
Test `TcComplain01Test` (test 5) đã sẵn → sẽ Pass. (Tối thiểu 1 ảnh/video đã bắt bằng `@NotEmpty`
trên `CreateComplaintRequest.evidences` qua `@Valid` ở `DisputeController`.)

### TC-UI-02 (PBI-07) — font < 16px (frontend)
`frontend/css/main.css`, `style.css`, `farm-profile.css`, `post-product.css`: nâng mọi
`font-size` < 16px (12/13/14/15px) lên ≥ 16px (đặc biệt `.field .opt`, `.muted`, `.muted-p`).

### TC-ONB-01 (PBI-13) — chưa có onboarding (frontend)
Thêm `frontend/js/components/onboarding.js` (wizard/tooltip từng bước + `localStorage` cho "lần
đầu"/"xem lại"/"bỏ qua").

### TC-NET-01 (PBI-05) — nén ảnh chưa đủ (frontend)
`frontend/js/pages/post-product-step1.js`, `post-product-step2.js`: thay
`toDataURL("image/jpeg",0.8)` bằng `toBlob("image/jpeg", quality)` + vòng lặp giảm `quality` tới khi
≤ 500KB; thêm retry (đếm + backoff) quanh `fetch` tải ảnh.

### TC-UI-01, TC-I18N-01, TC-PERF-01, TC-AVAIL-01 — kiểm thủ công/ops
Xem `docs/TestCases-Manual-Frontend.md`.

---

## Bước 3 — Chạy & xác nhận
```
cd backend
.\mvnw.cmd test
```
Kỳ vọng sau khi sửa: ✅ 27 giữ nguyên; ⛔ 4 (TC-FIN-01, TC-REFUND-01, TC-COMPLAIN-01, TC-DISPUTE-01)
Pass sau Bước 1; ❌ (TC-CHECK-02, TC-COMPLAIN-01-24h, TC-UI-02, TC-ONB-01, TC-NET-01) Pass sau Bước 2.

Bảng đầy đủ trạng thái từng TC ở `docs/TestReport.md`.
