# QA Tóm tắt — Module Product & Inventory (NongSanViet)

> Phạm vi: đọc trực tiếp 22 file Java trong `product` + `inventory`, cộng 3 file phụ trợ (`SecurityConfig`, `AuthenticatedUser`, `AdminModerationController`) để xác minh phân quyền. Không sửa file nào.

## 1. Endpoint REST

### ComboController — `/api/v1/combos`
- `POST /api/v1/combos` — `@PreAuthorize("hasRole('FARMER')")` (ComboController.java:25). farmerId lấy từ `authentication.getName()` (parse Long, ComboController.java:49-57).
- `GET /api/v1/combos/{id}` — KHÔNG @PreAuthorize (ComboController.java:35); công khai qua SecurityConfig.java:54 (`GET /api/v1/combos/**` = permitAll).
- `GET /api/v1/combos` — công khai (ComboController.java:40 + SecurityConfig.java:54).

### FarmerBatchController — `/api/v1/farmer/batches`
Controller KHÔNG dùng @PreAuthorize ở mức phương thức; phân quyền hoàn toàn dựa vào SecurityConfig.java:57 (`.requestMatchers("/api/v1/farms/**", "/api/v1/farmer/**").hasRole("FARMER")`). Comment dòng 13 nói rõ.
- `POST /api/v1/farmer/batches` — FARMER (FarmerBatchController.java:32).
- `GET /api/v1/farmer/batches` — FARMER (FarmerBatchController.java:40), param `upcomingOnly`.
- `GET /api/v1/farmer/batches/{id}` — FARMER (FarmerBatchController.java:46).
- `PUT /api/v1/farmer/batches/{id}` — FARMER (FarmerBatchController.java:51).

### Ngoài danh sách (minh bạch)
- `PUT /api/v1/admin/products/{id}/remove` — ADMIN (AdminModerationController.java:27) → gọi `ProductModerationService.removeFromSale`.
- BUYER không tạo được batch (route `/api/v1/farmer/**` chặn vai trò khác). Không phát hiện endpoint nhạy cảm thiếu phân quyền trong phạm vi review.

## 2. Entity & quan hệ

- **Product** (Product.java): `id`, `farmerId` (dòng 23), `categoryId`, `name`, `description`, `unit`, `createdAt`. **Không có trường giá/số lượng/trạng thái** — giá & số lượng nằm trên Batch. Quan hệ Farm/User chỉ qua cột `farmer_id` (không @ManyToOne, không @JsonIgnore). **Không tồn tại code nào tạo/sửa/xóa Product trong toàn codebase** (grep `productRepository.save`/`new Product()`/`Product.builder()` = rỗng) → đăng/sửa/xóa sản phẩm chưa được implement.
- **Combo** (Combo.java): `id`, `farmerId` (dòng 28), `name`, `description`, `comboPrice` (dòng 38), `status` mặc định "ACTIVE" (dòng 41), `createdAt`. `@OneToMany` → ComboItem (dòng 47, cascade ALL + orphanRemoval). Quan hệ Farmer chỉ qua `farmer_id`.
- **ComboItem** (ComboItem.java): `@ManyToOne` → Combo (dòng 17); `productId` là cột Long thuần (dòng 22) — KHÔNG @ManyToOne tới Product, không có FK DB → ràng buộc toàn vẹn chỉ ở tầng app.
- **Batch** (Batch.java, inventory): `productId` (dòng 28), `@ManyToOne` → Product **chỉ đọc** (dòng 32, insertable=false/updatable=false), `traceabilityCode` UUID unique (dòng 34-35), `harvestDate`, `unitPrice` (dòng 40), `totalQuantity` (dòng 43), `remainingQuantity` (dòng 46), `status` (dòng 49-50: PRE_ORDER | AVAILABLE | SOLD_OUT | CLOSED). Không @JsonIgnore.

## 3. Logic nghiệp vụ then chốt

- **Đăng/sửa/xóa sản phẩm**: KHÔNG có controller/service nào thực hiện (toàn codebase). Chỉ có đọc (`ProductRepository`: `findAllByIdInAndFarmerId`, `findByIdAndFarmerId`) và moderation "gỡ khỏi sàn" qua admin.
- **Tạo/sửa combo**: chỉ có CREATE (`ComboServiceImpl.createCombo`). Kiểm tra ≥2 sản phẩm, không trùng `productId`, và **kiểm tra sở hữu** mọi sản phẩm thành phần thuộc farmer qua `findAllByIdInAndFarmerId` rồi so count (ComboServiceImpl.java:54-61). **Không có endpoint sửa/xóa combo** → farmer không thể sửa giá/thành phần hoặc xóa combo đã tạo.
- **Tạo/sửa lô (batch)**: `BatchService.create` gắn batch cho product của chính farmer (findByIdAndFarmerId, dòng 37), `remainingQuantity = totalQuantity` (dòng 47), status PRE_ORDER. `update` chỉ cho PRE_ORDER (dòng 64-67), validate ngày thu hoạch (không quá khứ, ≤365 ngày). `findOwned` trả 404 cho lô người khác (dòng 82, không lộ tồn tại).
- **Moderation**: `ProductModerationService.removeFromSale` chỉ "đóng mọi lô" (status=CLOSED), không xóa sản phẩm (vì `products` không có cột status). Chỉ ADMIN gọi.
- **Tồn kho/hết hạn**: `remainingQuantity` là cột tồn; `BatchRepository` có khóa bi quan PESSIMISTIC_WRITE (dòng 18) phục vụ đặt hàng; logic trừ/cộng tồn nằm ngoài phạm vi (order module).

## 4. Lỗ hổng & điểm cần chú ý (file:dòng — mô tả — mức độ)

**IDOR / sở hữu**
- `BatchService` — Không phát hiện. create/list/get/update đều scope theo farmerId từ token; `findByIdAndFarmer` trả 404 cho lô người khác (BatchService.java:82).
- `ComboServiceImpl.createCombo` — Không phát hiện. Kiểm tra sở hữu thành phần đầy đủ (ComboServiceImpl.java:54-61).
- `ProductModerationService.removeFromSale` — Không phát hiện. Không kiểm tra sở hữu nhưng chỉ ADMIN gọi (route `/api/v1/admin/**`).

**Phân quyền**
- `FarmerBatchController` (toàn bộ, dòng 32/40/46/51) — không có @PreAuthorize mức phương thức, phụ thuộc hoàn toàn SecurityConfig.java:57. Nếu ai đổi SecurityConfig hoặc thêm prefix khác, toàn bộ endpoint batch mất kiểm soát. — **Trung bình** (nên thêm @PreAuthorize dự phòng — Defense-in-depth).

**Mass assignment**
- DTO là record + trường tường minh + @Valid; trạng thái (PRE_ORDER/ACTIVE) hardcode trong service, không bind từ request. — Không phát hiện (tốt).

**SQL/JPQL injection**
- `JpaProductModerationRepository` native query dùng `:id` + setParameter (dòng 19, 27); `BatchRepository` JPQL @Param. Không nối chuỗi input. — Không phát hiện (tốt).

**Business logic**
- `BatchService.java:74-76` — `update()` set totalQuantity/unitPrice/harvestDate nhưng KHÔNG cập nhật `remainingQuantity`. Nếu lô PRE_ORDER đã có đơn đặt trước (remaining < total) mà farmer giảm total xuống dưới remaining → tồn kho mâu thuẫn; nếu tăng total thì remaining không tăng → không mở sức chứa đặt trước. — **Cao**.
- `BatchService.java:82` — `update` dùng `findByIdAndFarmer` (không lock) trong khi đặt hàng dùng PESSIMISTIC_WRITE → có thể race giữa sửa lô và trừ tồn. — **Trung bình**.
- `CreateComboRequest.java:20` + `ComboServiceImpl.java:67` — `@DecimalMin("0.0")` cho phép comboPrice = 0; set thẳng, KHÔNG kiểm tra comboPrice so với tổng thành phần. (Product không có cột giá nên "tổng thành phần" không đơn nghĩa — vẫn thiếu kiểm soát giá hợp lý.) — **Trung bình**.
- `ComboController` — KHÔNG có endpoint sửa/xóa combo → farmer không thể chỉnh/xóa combo sai giá/thành phần. — **Trung bình** (khoảng trống chức năng).
- `ComboItem.java:22` — `productId` Long thuần, không FK → getComboById xử lý product null (ComboServiceImpl.java:101-109) nên không crash nhưng có thể trả combo với thành phần "ma" nếu product bị ảnh hưởng. — **Thấp**.

**Upload file**
- Không có trường ảnh/chứng nhận trên Product/Combo/ComboItem/Batch, không có endpoint upload trong phạm vi review. — Không phát hiện.

**Race condition tồn kho**
- Đã có khóa bi quan cho đặt hàng (BatchRepository.java:18). Logic trừ/cộng remainingQuantity ngoài phạm vi (order module). Trong phạm vi review, điểm race liên quan nhất = update lô không lock (đã nêu trên).

**Quan sát phụ (possible)**
- Hai cơ chế lấy farmerId không đồng nhất: `ComboController` dùng `extractUserId(authentication)` parse `authentication.getName()` (ComboController.java:49-57) còn `FarmerBatchController` dùng `@AuthenticationPrincipal AuthenticatedUser.id()` (FarmerBatchController.java:33). — **Thấp / possible** (cần xác nhận `JwtAuthenticationFilter` rằng hai nguồn luôn đồng nhất).
