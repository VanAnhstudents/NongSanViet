# Test case thủ công / frontend / chưa hiện thực — Nông Sản Việt

Tài liệu này gom các test case (theo `docs/TestCaseTrello.md`) thuộc về giao diện frontend
hoặc tính năng chưa được hiện thực ở backend, nên không thể kiểm thử tự động bằng JUnit
(bộ test backend dùng JUnit + Mockito). Mỗi test case ghi rõ bước thao tác, kết quả mong đợi,
trạng thái hiện tại và vị trí cần sửa.

Trạng thái tóm tắt: **Pass** (đạt khi thử thủ công) / **NotPass** (chưa đạt) / **Manual** (cần
kiểm tra thủ công trên trình duyệt).

---

## TC-NET-01 — PBI-05: Nén ảnh phía client và xử lý mạng yếu
- **Trạng thái hiện tại:** NotPass (một phần)
- **Đã có:** frontend nén ảnh bằng canvas → `toDataURL("image/jpeg", 0.8)` (xuất JPEG quality 0.8)
  tại `frontend/js/pages/post-product-step1.js` (và tương tự step2).
- **Còn thiếu:**
  1. Không giới hạn kích thước đầu ra ≤ 500KB — chỉ nén theo quality cố định 0.8, không đo
     dung lượng và nén lại khi vượt ngưỡng.
  2. Dùng `toDataURL` (base64) thay vì `toBlob` + `FormData`, gây tải nặng hơn.
  3. Chưa có cơ chế retry khi mạng yếu (3G/throttle).
- **Bước test thủ công:** mở trang đăng sản phẩm, chọn ảnh ~3MB trên mạng throttle 3G, bấm gửi.
  Đo kích thước base64/blob gửi lên; kiểm tra có retry khi lỗi mạng.
- **Kết quả mong đợi:** ảnh gửi lên ≤ 500KB; nếu lỗi mạng có tự retry; đăng thành công.
- **Vị trí cần sửa:** `frontend/js/pages/post-product-step1.js`, `post-product-step2.js` — thay
  `toDataURL` bằng `toBlob("image/jpeg", quality)` kèm vòng lặp giảm quality đến khi ≤ 500KB;
  thêm retry (đếm lại + backoff) quanh `fetch` tải ảnh.

---

## TC-UI-01 — PBI-06: Web responsive (mobile-first)
- **Trạng thái hiện tại:** Manual (có media queries, cần kiểm tra thực tế tại 320px)
- **Đã có:** `@media (max-width: 900px)`, `@media (max-width: 480px)`, `@media (min-width: 768px)`,
  `@media (min-width: 1024px)` trong `frontend/css/main.css` và `style.css`.
- **Còn thiếu:** chưa có breakpoint/explicit cho 320px; cần kiểm thủ công không vỡ layout và
  không xuất hiện thanh cuộn ngang tại 320px.
- **Bước test thủ công:** mở các trang chính (`index.html`, `pages/farmer/post-product-step1.html`,
  `step2`, `review`, `farm-profile.html`) tại kích thước 320px trên Chrome DevTools.
- **Kết quả mong đợi:** bố cục không vỡ, không có thanh cuộn ngang.
- **Vị trí cần kiểm/sửa:** `frontend/css/main.css`, `frontend/style.css` (bổ sung breakpoint
  `@media (max-width: 360px)` nếu vỡ, kiểm `overflow-x`).

---

## TC-UI-02 — PBI-07: Giao diện tối giản (≤ 3 bước, chữ ≥ 16px)
- **Trạng thái hiện tại:** NotPass (phần cỡ chữ)
- **Đã đạt:** luồng đăng sản phẩm đúng 3 bước (`post-product-step1` → `step2` → `review`), thỏa
  tiêu chí "≤ 3 bước".
- **Không đạt (cỡ chữ):** trong CSS có nhiều `font-size` nhỏ hơn 16px:
  - `frontend/css/main.css`, `frontend/style.css`, `farm-profile.css`, `post-product.css`:
    `font-size: 15px`, `14px`, `13px`, `12px` (ví dụ `.field .opt { font-size: 12px; }`,
    văn bản `.muted`/`muted-p` 13–15px).
- **Kết quả mong đợi:** cỡ chữ mặc định toàn hệ thống ≥ 16px.
- **Vị trí cần sửa:** các file `frontend/css/*.css` — đặt cỡ chữ mặc định `:root`/`body` ≥ 16px
  và nâng các `font-size` < 16px (trừ tiêu đề đang lớn hơn) lên tối thiểu 16px.

---

## TC-I18N-01 — PBI-10: Ngôn ngữ tiếng Việt và đơn vị VNĐ
- **Trạng thái hiện tại:** Gần đạt (Manual) — lưu ý hậu tố tiền tệ
- **Đã đạt:** nhãn/thông báo giao diện bằng tiếng Việt; có hàm định dạng tiền:
  `const formatVND = (amount) => \`${Number(amount).toLocaleString('vi-VN')}đ\``
  (ví dụ `40.000đ`) tại các trang giỏ/checkout/preorder.
- **Lệch nhẹ so với PBI:** PBI ví dụ `50.000₫` (dùng ký hiệu ₫), nhưng code dùng hậu tố `đ`.
- **Bước test thủ công:** duyệt các trang, kiểm 100% nhãn không còn text tiếng Anh mặc định;
  giá hiển thị định dạng VNĐ.
- **Vị trí cần chỉnh (nếu thống nhất theo PBI):** `frontend/js/pages/post-product-review.js`
  và các trang có `formatVND`/`formatMoney` — đổi hậu tố `đ` thành `₫` nếu cần khớp PBI.

---

## TC-ONB-01 — PBI-13: Hướng dẫn thao tác cho người bán (onboarding)
- **Trạng thái hiện tại:** NotPass (chưa hiện thực)
- **Đã có:** chỉ có các câu thông báo/text "gửi hướng dẫn cho người bán", "hướng dẫn bổ sung hồ sơ
  trước khi đăng lại" (thông báo tĩnh, không phải wizard/tooltip từng bước).
- **Còn thiếu:** không có component onboarding wizard/tooltip cho Farmer đăng nhập lần đầu; không
  có nút "bỏ qua"/"xem lại" hướng dẫn.
- **Bước test thủ công:** đăng nhập Farmer mới → kiểm có wizard/tooltip từng bước; có thể bỏ qua
  và xem lại sau.
- **Vị trí cần hiện thực:** thêm `frontend/js/components/onboarding.js` + đánh dấu bước
  `sessionStorage`/`localStorage` để hiển thị đúng "lần đầu" và cho phép xem lại.

---

## TC-CHECK-02 — PBI-26: Kết quả đồng kiểm và báo "Đã giao" (chưa hiện thực backend)
- **Trạng thái hiện tại:** NotPass — chưa có code backend
- **Còn thiếu ở backend:**
  1. `OrderCoInspection` thiếu trường kết quả: `result` (PASS/FAIL), `resultNote`,
     `completedBy`, `completedAt`. Entity hiện chỉ có `status` (REQUESTED|ACKNOWLEDGED|COMPLETED),
     `note`, `acknowledgedBy`, `acknowledgedAt`.
  2. `CoInspectionServiceImpl` chưa có phương thức `recordCoInspectionResult(buyerId, orderId,
     result, note)` để Buyer ghi kết quả đồng kiểm.
  3. `OrderServiceImpl` chưa có phương thức "Buyer xác nhận đã nhận hàng" chuyển đơn
     `DELIVERED -> COMPLETED` (hiện chỉ Farmer đẩy PENDING→CONFIRMED→SHIPPING→DELIVERED).
  4. Chưa khoá kết quả đồng kiểm sau khi đơn "Đã giao"/hoàn tất (không cho sửa).
- **Bước test (sau khi hiện thực):** Buyer gửi yêu cầu đồng kiểm → nhận hàng → ghi kết quả
  (đạt/không đạt + ghi chú) → xác nhận "Đã giao" → đơn sang COMPLETED, kết quả không sửa được.
- **Vị trí cần hiện thực:** `order/entity/OrderCoInspection.java` (thêm cột), `order/service/
  CoInspectionServiceImpl.java` (thêm `recordCoInspectionResult`), `order/service/
  OrderServiceImpl.java` (thêm `confirmDelivery` Buyer, chuyển DELIVERED→COMPLETED), kèm migration
  Flyway trong `src/main/resources/db/migration`.

---

## TC-PERF-01 — PBI-40: Hiệu năng trang chính < 3s
- **Trạng thái hiện tại:** Manual / ops (chưa đo)
- **Bước test thủ công:** dùng Lighthouse / DevTools Network (throttle "Slow 3G" hoặc mạng trung
  bình) đo `index.html` (request → hiển thị nội dung chính). Lặp ≥ 20 lần.
- **Kết quả mong đợi:** ≥ 95% số lần đo < 3 giây.
- **Ghi chú:** cần backend `/actuator/health` nhanh, gzip tĩnh, cache. Nếu chưa đạt, tối ưu
  truy vấn trang chủ và nén tài nguyên tĩnh.

---

## TC-AVAIL-01 — PBI-41: Khả dụng (uptime 99,5%)
- **Trạng thái hiện tại:** Manual / ops (chưa có giám sát)
- **Bước test thủ công:** bật monitoring (vd. UptimeRobot) trỏ tới `/actuator/health` trong giai
  đoạn vận hành thử; kiểm uptime ≥ 99,5%.
- **Kết quả mong đợi:** uptime ≥ 99,5%; có alert tự động khi downtime vượt ngưỡng.
- **Vị trí cần hiện thực:** cấu hình alert (actuator + monitoring bên ngoài), chưa thuộc mã
  nguồn chính.
