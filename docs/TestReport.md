# Báo cáo Test — Nông Sản Việt (FarmDirect)

Vai trò: **Tester**. Không sửa mã nguồn chính (main); chỉ tổ chức test, viết test, chạy/đánh giá
và chỉ ra vị trí cần sửa.

---

## 0. Kết luận chính

1. Đã tổ chức lại toàn bộ test theo đúng quy ước `Tc<Domain>NNTest.java` (đúng như mẫu duy nhất
   ban đầu là `TcAuth01Test`): **rename 30 file**, **sửa 2 file test bị hỏng biên dịch**,
   **viết 3 file test mới**, **thêm doc test case manual/frontend**, **bổ sung `@DisplayName`**.
2. **Phát hiện chặn lớn nhất — `main source KHÔNG biên dịch`**: một lần refactor dở dang (migrasi
   enum `RefundStatus`/`OrderStatus`/`PaymentStatus`, tách `RefundQueryRepository`/
   `RefundPaymentRepository`, bỏ field `auditLogService`) nhưng nhiều call site chưa được cập
   nhật. Do đó `mvn test-compile` lỗi ở pha `default-compile` (main) → **toàn bộ test suite không
   chạy được** cho đến khi main được sửa. Tôi **không sửa main** (đúng vai trò); Mục 1 liệt kê lỗi
   chính xác theo `file:dòng` và cách sửa.
3. Vì build hỏng, các trạng thái Pass/NotPass dưới đây là **đánh giá tĩnh** (đọc code). Sau khi
   sửa main theo Mục 1, chạy `.\mvnw.cmd test` để xác nhận.

---

## 1. Lỗi biên dịch main (chặn toàn bộ test) + cách sửa

Tất cả lỗi nằm ở main, do refactor chưa hoàn chỉnh. **Khuyến nghị chọn 1 hướng nhất quán**:
giữ `status` của `Order`/`Payment`/`Refund` là **String** (khớp với đa số code & test hiện có),
tức là "hoàn trả/refund" phần enum về String. Các sửa cụ thể:

| # | File:dòng | Lỗi | Cách sửa (hướng giữ String) |
|---|-----------|-----|------------------------------|
| 1 | `order/entity/Refund.java` (toàn bộ) | `status` là enum `RefundStatus`; `createdAt`/`processedAt` là `Instant` | Đổi `status` về `String`; `createdAt`/`processedAt` về `OffsetDateTime` (bỏ `@Enumerated`) |
| 2 | `order/dto/RefundResponse.java:25` | `refund.getStatus()` (RefundStatus) gán cho field `String status` | Tự hết sau khi sửa #1 (`getStatus()` trả String) |
| 3 | `order/repository/RefundRepository.java` | Thiếu 2 method query | Thêm: `List<Refund> findAllByPaymentIdOrderByCreatedAtAsc(Long paymentId);` và `List<Refund> findAllByPaymentIdInOrderByCreatedAtAsc(Set<Long> paymentIds);` |
| 4 | `order/service/FinancialTransactionServiceImpl.java:116,200` | Gọi 2 method thiếu ở #3 | Tự hết sau #3 |
| 5 | `order/service/FinancialTransactionServiceImpl.java:146` | `refund.setStatus("COMPLETED")` String vs RefundStatus | Tự hết sau #1 (Refund.status về String) |
| 6 | `dispute/service/DisputeServiceImpl.java:286,441` | Gọi `findAllByPaymentIdInOrderByCreatedAtAsc(Set)` thiếu | Tự hết sau #3 |
| 7 | `order/service/PreorderRefundService.java:78` | `findByOrderIdAndStatus(orderId, PaymentStatus.PAID)` lệch kiểu | Đổi `RefundPaymentRepository.findByOrderIdAndStatus` về `(Long, String)` và truyền `"PAID"` |
| 8 | `order/service/PreorderRefundService.java:84` | `order.setStatus(OrderStatus.CANCELLED)` lệch kiểu (Order.status là String) | Đổi thành `order.setStatus("CANCELLED")` |
| 9 | `order/service/PreorderRefundService.java:85` | `order.setUpdatedAt(now)` với `now` là `Instant` (Order.updatedAt là `OffsetDateTime`) | Đổi `now` sang `OffsetDateTime.now(clock)`; `refund.status("COMPLETED")`/`createdAt(now)`/`processedAt(now)` theo #1 |
| 10 | `order/service/PreorderRefundService.java` (các chỗ dùng enum) | `PaymentStatus.REFUNDED`, `RefundStatus.COMPLETED`, `OrderStatus.CANCELLED`, `new ShortageRefundResponse(orderId, OrderStatus.CANCELLED, …)` | Đổi về String tương ứng (`"REFUNDED"`, `"COMPLETED"`, `"CANCELLED"`) |
| 11 | `common/contract/JdbcAuditAndNotificationAdapter.java:69,84` | Dùng biến `auditLogService` chưa khai báo | Thêm field `private final <đúng interface AuditLogService có recordAdminAction/recordFinancialTransaction> auditLogService;` (sẽ được `@RequiredArgsConstructor` inject) |

Sau khi sửa main theo trên, chạy `.\mvnw.cmd test` để xác nhận.

> Nếu đội chọn hướng ngược lại (migrasi hết sang enum): phải cập nhật thêm nhiều test hiện đang
> dùng String (xem Mục 5). Khuyến nghị hướng "giữ String" vì ít churn hơn.

---

## 2. Việc đã làm với test

### 2.1 Đổi tên file test theo TC-ID (30 file) — kèm đổi tên class
`PasswordResetServiceTest→TcAuth02Test`, `FarmServiceTest→TcFarm01Test`,
`FarmExtendedInfoServiceTest→TcFarm02Test`, `ProductQuickListingTest→TcProd01Test`,
`ComboServiceTest→TcProd02Test`, `BatchServiceTest→TcHarvest01Test` (+`@DisplayName` lớp),
`SecurityNfr04Test→TcSec01Test`, `ModularMonolithArchitectureTest→TcArch01Test`,
`ProductSearchServiceTest→TcSearch01Test`, `CartAndOrderServiceTest→TcOrd01Test`,
`PreOrderServiceTest→TcOrd02Test`, `FarmerOrderManagementTest→TcOrd03Test`,
`PaymentServiceTest→TcPay01Test`, `InAppNotificationServiceTest→TcNoti01Test`,
`FinancialTransactionNfr09Test→TcFin01Test`, `AuditAndFinancialLogNfr13Test→TcLog01Test`,
`ChatServiceTest→TcChat01Test`, `CoInspectionServiceTest→TcCheck01Test`,
`TraceabilityServiceTest→TcQr01Test`, `PreorderRefundServiceTest→TcRefund01Test`,
`EvidenceStorageNfr05Test→TcStorage01Test`, `FarmingLogIntegrityServiceTest→TcIntegrity01Test`,
`ReviewServiceTest→TcReview01Test`, `FarmCertificateVerificationTest→TcVerify01Test`,
`ReputationServiceTest→TcRep01Test`, `BackupServiceTest→TcBackup01Test`,
`AddressServiceTest→TcShip01Test`, `ShippingFeeServiceTest→TcShip01FeeTest`,
`AdminModerationServiceTest→TcAdmin01Test`, `UserAccountServiceTest→TcAdmin01AccountTest`.

### 2.2 Sửa 2 file test bị hỏng biên dịch (rename + fix)
- `complaint/ComplaintServiceTest.java` → `TcComplain02Test.java`: bỏ import trùng
  `dispute.entity.Complaint`, gộp 2 lệnh `return` trong `complaintCreatedHoursAgo`, bỏ dòng
  `eq(deadline)))` mồ côi trong test leo thang.
- `auth/AccountStatusInterceptorTest.java` → `TcAdmin01LockTest.java`: bỏ lệnh `.thenReturn`
  thứ hai mồ côi trong `tc01_activeAccountPasses`.

### 2.3 Viết 3 test mới (cho TC chưa có test tự động)
- `trace/TcDiary01Test.java` (TC-DIARY-01): `TraceabilityServiceImpl.addFarmingLog` — thêm nhật ký
  theo ngày, chặn farmer không sở hữu, lô không tồn tại, hiển thị theo thứ tự thời gian.
- `dispute/TcComplain01Test.java` (TC-COMPLAIN-01): `DisputeServiceImpl.createComplaint` — tạo
  khiếu nại kèm bằng chứng, chặn sai trạng thái/sai chủ/số trùng, và (test 5) kỳ vọng chặn quá
  hạn 24h (chưa hiện thực → fail có ý nghĩa).
- `dispute/TcDispute01Test.java` (TC-DISPUTE-01): `DisputeServiceImpl.resolveDisputeByAdmin` —
  REJECT_COMPLAINT, chặn phân xử lại, FULL_REFUND hoàn đúng 100%.

> 3 test này phụ thuộc `DisputeServiceImpl` biên dịch được (hiện lỗi theo Mục 1). Sau khi sửa
> main, chúng sẽ chạy.

### 2.4 Doc test case manual/frontend/ops
`docs/TestCases-Manual-Frontend.md` — TC-NET-01, TC-UI-01, TC-UI-02, TC-I18N-01, TC-ONB-01,
TC-CHECK-02, TC-PERF-01, TC-AVAIL-01 (frontend không có test runner JS nên viết dạng kịch bản
thủ công + vị trí cần sửa).

### 2.5 Test hỗ trợ (không rename — không gắn 1 TC cụ thể, giữ nguyên)
`BackendApplicationTests`, `auth/AuthServiceTest` (trùng logic TcAuth01Test), `common/JwtServiceTest`,
`common/IdentifierNormalizerTest`, `inventory/BatchRequestValidationTest`, `user/UserServiceTest`,
`order/StockConcurrencyTest` (file rỗng 0 byte — nên xoá nếu muốn gọn).

---

## 3. Bảng Pass/NotPass theo 41 TC

Chú thích: ✅ Pass-dự kiến (test & service ổn, sẽ pass sau khi sửa build) · ⛔ Build-blocked
(service bị hỏng build) · ❌ NotPass (thiếu logic / test fail có ý nghĩa) · 📋 Manual (frontend/ops) · ➕ Mới (test vừa viết)

| TC-ID | PBI | File test | Trạng thái | Ghi chú |
|-------|-----|-----------|-----------|---------|
| TC-AUTH-01 | 01 | `TcAuth01Test` | ✅ | service ổn (AuthService) |
| TC-AUTH-02 | 15 | `TcAuth02Test` | ✅ | PasswordResetService ổn |
| TC-FARM-01 | 02 | `TcFarm01Test` | ✅ | FarmService ổn |
| TC-FARM-02 | 11 | `TcFarm02Test` | ✅ | FarmExtendedInfoService ổn |
| TC-PROD-01 | 03 | `TcProd01Test` | ✅ | ProductQuickListing ổn |
| TC-PROD-02 | 12 | `TcProd02Test` | ✅ | ComboService ổn |
| TC-HARVEST-01 | 04 | `TcHarvest01Test` | ✅ | BatchService ổn |
| TC-NET-01 | 05 | (frontend) | ❌ | nén canvas có nhưng thiếu cap 500KB + retry — Mục 4 |
| TC-UI-01 | 06 | (frontend) | 📋 | có media query, cần thử 320px — doc manual |
| TC-UI-02 | 07 | (frontend) | ❌ | nhiều `font-size` 12–15px < 16px — Mục 4 |
| TC-SEC-01 | 08 | `TcSec01Test` | ✅ | HTTPS redirect/BCrypt/RBAC ổn |
| TC-ARCH-01 | 09 | `TcArch01Test` | ✅ | kiểm package/DAG, không phụ thuộc build |
| TC-I18N-01 | 10 | (frontend) | 📋 | TV ✓; VND dùng hậu tố `đ` (PBI ví dụ `₫`) |
| TC-ONB-01 | 13 | (frontend) | ❌ | chưa có wizard/tooltip — Mục 4 |
| TC-SEARCH-01 | 14 | `TcSearch01Test` | ✅ | ProductSearchService ổn |
| TC-ORD-01 | 16 | `TcOrd01Test` | ✅ | CartService + OrderService (String) ổn |
| TC-ORD-02 | 17 | `TcOrd02Test` | ✅ | PreOrderService ổn |
| TC-ORD-03 | 19 | `TcOrd03Test` | ✅ | OrderServiceImpl.updateOrderStatusByFarmer ổn |
| TC-PAY-01 | 18 | `TcPay01Test` | ✅ | PaymentService ổn |
| TC-NOTI-01 | 20 | `TcNoti01Test` | ✅ | InAppNotificationService ổn |
| TC-SHIP-01 | 21 | `TcShip01Test` + `TcShip01FeeTest` | ✅ | AddressService + ShippingFeeService ổn |
| TC-FIN-01 | 22 | `TcFin01Test` | ⛔ | FinancialTransactionServiceImpl lỗi build (Mục 1 #4,#5) + test dùng method thiếu (#3) |
| TC-LOG-01 | 23 | `TcLog01Test` | ✅ | AuditLogServiceImpl ổn |
| TC-CHAT-01 | 24 | `TcChat01Test` | ✅ | ChatService ổn |
| TC-CHECK-01 | 25 | `TcCheck01Test` | ✅ | CoInspectionServiceImpl (request/ack) ổn |
| TC-CHECK-02 | 26 | — | ❌ | chưa hiện thực backend — Mục 4 |
| TC-DIARY-01 | 27 | `TcDiary01Test` ➕ | ✅ | TraceabilityServiceImpl.addFarmingLog ổn |
| TC-QR-01 | 28 | `TcQr01Test` | ✅ | TraceabilityServiceImpl (getTraceability) ổn |
| TC-COMPLAIN-01 | 29 | `TcComplain01Test` ➕ | ⛔/❌ | DisputeServiceImpl lỗi build; test 5 kỳ vọng hạn 24h chưa hiện thực — Mục 4 |
| TC-COMPLAIN-02 | 30 | `TcComplain02Test` | ✅ | ComplaintService ổn (đã sửa compile) |
| TC-DISPUTE-01 | 31 | `TcDispute01Test` ➕ | ⛔ | DisputeServiceImpl lỗi build (Mục 1 #6,#3) |
| TC-REFUND-01 | 32 | `TcRefund01Test` | ⛔ | PreorderRefundService lỗi build (Mục 1 #7–#10); test đang dùng enum → xem Mục 5 |
| TC-ADMIN-01 | 33 | `TcAdmin01Test` + `TcAdmin01LockTest` + `TcAdmin01AccountTest` | ✅ | AdminModeration/AccountStatusInterceptor/UserAccountService ổn |
| TC-STORAGE-01 | 34 | `TcStorage01Test` | ✅ | EvidenceAccess + PersistentStorage ổn |
| TC-INTEGRITY-01 | 35 | `TcIntegrity01Test` | ✅ | FarmingLogIntegrityService ổn |
| TC-REVIEW-01 | 36 | `TcReview01Test` | ✅ | ReviewService ổn |
| TC-VERIFY-01 | 37 | `TcVerify01Test` | ✅ | FarmCertificateVerification ổn |
| TC-REP-01 | 38 | `TcRep01Test` | ✅ | ReputationService ổn |
| TC-BACKUP-01 | 39 | `TcBackup01Test` | ✅ | BackupService ổn |
| TC-PERF-01 | 40 | (ops) | 📋 | cần đo Lighthouse/Network — doc manual |
| TC-AVAIL-01 | 41 | (ops) | 📋 | cần monitoring uptime — doc manual |

Tổng kết: ✅ 27 · ⛔ 4 (TC-FIN-01, TC-COMPLAIN-01, TC-DISPUTE-01, TC-REFUND-01 — đều do main
hỏng build) · ❌ 4 (TC-NET-01, TC-UI-02, TC-ONB-01, TC-CHECK-02) · 📋 4 (TC-UI-01, TC-I18N-01,
TC-PERF-01, TC-AVAIL-01) · ➕ 3 test mới.

---

## 4. Hướng dẫn sửa cụ thể cho từng TC NotPass / thiếu logic

### TC-CHECK-02 (PBI-26) — chưa hiện thực backend
- `order/entity/OrderCoInspection.java`: thêm cột `result` (PASS/FAIL), `result_note`,
  `completed_by`, `completed_at`.
- `order/service/CoInspectionServiceImpl.java`: thêm `recordCoInspectionResult(buyerId, orderId,
  result, note)` — chỉ cho khi `status=ACKNOWLEDGED`, set `status=COMPLETED` + kết quả, khoá
  không cho sửa lại.
- `order/service/OrderServiceImpl.java`: thêm `confirmDelivery(buyerId, orderId)` chuyển đơn
  `DELIVERED → COMPLETED` (hiện chỉ có Farmer đẩy tới DELIVERED).
- Thêm migration Flyway ở `src/main/resources/db/migration`.

### TC-COMPLAIN-01 (PBI-29) — thiếu kiểm tra hạn 24h
- `dispute/service/DisputeServiceImpl.java`, trong `createComplaint`, sau kiểm tra trạng thái
  đơn: nếu `order.getDeliveredAt() != null` và `now > deliveredAt + 24h` → ném
  `BusinessException(BAD_REQUEST, "COMPLAINT_WINDOW_EXPIRED", …)`. Test `TcComplain01Test` test (5)
  đã sẵn, sẽ pass sau khi bổ sung.
- "Tối thiểu 1 ảnh/video" đã được bắt ở controller qua `@NotEmpty` trên
  `CreateComplaintRequest.evidences` (kiểm tra qua `@Valid` ở `DisputeController`).

### TC-UI-02 (PBI-07) — cỡ chữ < 16px
- `frontend/css/main.css`, `frontend/style.css`, `frontend/css/farm-profile.css`,
  `frontend/css/post-product.css`: nâng các `font-size: 12px/13px/14px/15px` lên tối thiểu 16px
  (đặc biệt `.field .opt`, `.muted`, `.muted-p`).

### TC-ONB-01 (PBI-13) — chưa có onboarding
- Thêm `frontend/js/components/onboarding.js` (wizard/tooltip từng bước cho Farmer lần đầu),
  đánh dấu `localStorage` để phân biệt "lần đầu" và cho phép "xem lại"/"bỏ qua".

### TC-NET-01 (PBI-05) — nén ảnh chưa đủ
- `frontend/js/pages/post-product-step1.js`, `post-product-step2.js`: thay
  `toDataURL("image/jpeg",0.8)` bằng `toBlob("image/jpeg", quality)` kèm vòng lặp giảm `quality`
  tới khi ≤ 500KB; thêm retry (đếm + backoff) quanh `fetch` tải ảnh.

---

## 5. Các test cần cập nhật sau khi chọn hướng sửa main

Tuỳ hướng sửa main ở Mục 1:
- **Hướng khuyến nghị (giữ String cho Order/Payment/Refund):** chỉ cần hoàn trả phần enum trong
  `PreorderRefundService` + `Refund` + `RefundResponse` về String. Khi đó `TcRefund01Test` (đang
  dùng `OrderStatus`/`PaymentStatus`/`RefundStatus`) phải đổi về String để khớp.
- **Hướng ngược (migrasi hết sang enum):** ngoài main, phải đổi nhiều test đang dùng String sang
  enum: `TcOrd01Test`, `TcOrd02Test`, `TcOrd03Test`, `TcPay01Test`, `TcNoti01Test`, `TcFin01Test`,
  `TcDispute01Test` (test 3), `TcComplain01Test`. Khuyến nghị **không** chọn hướng này vì tốn công
  hơn và dễ sai.

Sau khi sửa main + (nếu cần) cập nhật test, chạy:
```
cd backend
.\mvnw.cmd test
```
