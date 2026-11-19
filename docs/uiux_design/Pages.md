# PAGES.md — DANH SÁCH TRANG & NAVIGATION BAR
## Nền tảng kết nối nông sản trực tiếp từ nông dân đến người tiêu dùng

> Tài liệu này tổng hợp từ **UIUXpromax.md** (Design System) và **BTL-NhapMonCongNghePhanMem.md** (báo cáo đề tài — danh sách PBI/FR mục 3.3, phân vai actor mục 1.2, layout chuẩn mục 9). Mỗi trang bên dưới được suy ra trực tiếp từ một hoặc nhiều PBI/FR cụ thể để đảm bảo không thiếu, không thừa so với phạm vi MVP (mục 1.5.3).

---

## 0. QUY ƯỚC CHUNG

- Hệ thống có **3 vai trò (actor)**: Farmer (Nông dân), Buyer (Người mua), Admin (Quản trị viên) — mỗi vai trò có khu vực trang riêng nhưng dùng chung 1 bộ Design Token, chỉ khác **màu accent vai trò** (`--color-role-farmer` / `--color-role-buyer` / `--color-role-admin`).
- Trang dùng chung cho người chưa đăng nhập (Public/Guest) không gắn màu accent vai trò nào.
- Farmer & Admin dùng layout **Dashboard + Sidebar trái** (desktop), Buyer dùng layout **Header + Danh mục ngang** (desktop) / **Bottom Navigation Bar** (mobile).

---

## 1. NHÓM TRANG PUBLIC / CHUNG (chưa đăng nhập hoặc dùng chung 3 vai trò)

### 1.1. Trang Đăng nhập (Login)
- **Căn cứ:** PBI-01, PBI-08, PBI-15.
- **Cấu trúc:**
  - Logo + tên nền tảng
  - Form: ô email/số điện thoại, ô mật khẩu, nút hiện/ẩn mật khẩu
  - Link "Quên mật khẩu?"
  - Nút chính "Đăng nhập" (`btn--primary`, full-width mobile)
  - Link chuyển sang "Đăng ký" + lựa chọn vai trò (Farmer/Buyer) khi đăng ký
  - Thông báo lỗi đăng nhập dùng `form-field--error`

### 1.2. Trang Đăng ký (Register)
- **Căn cứ:** PBI-01 (đăng ký, phân quyền), PBI-15 (xác thực số điện thoại).
- **Cấu trúc:**
  - Chọn vai trò: Nông dân / Người mua (Admin không tự đăng ký, do hệ thống tạo sẵn)
  - Form: họ tên, số điện thoại, email, mật khẩu, xác nhận mật khẩu
  - Checkbox đồng ý điều khoản
  - Nút "Đăng ký"
  - Bước xác thực OTP số điện thoại (PBI-15)

### 1.3. Trang Quên mật khẩu / Đặt lại mật khẩu
- **Căn cứ:** PBI-15.
- **Cấu trúc:**
  - Bước 1: nhập số điện thoại/email → gửi OTP
  - Bước 2: nhập OTP xác thực
  - Bước 3: nhập mật khẩu mới + xác nhận

### 1.4. Trang 404 / Lỗi hệ thống
- **Căn cứ:** quy chuẩn A11y & trạng thái Error state (mục 11 UIUXpromax).
- **Cấu trúc:** illustration SVG empty/error state, thông báo ngắn, nút "Về trang chủ".

---

## 2. NHÓM TRANG BUYER (NGƯỜI MUA)

Layout: Header sticky viền accent `--color-role-buyer` + thanh danh mục ngang (desktop) / Bottom Nav 5 mục (mobile) — theo mục 9.1 UIUXpromax.

### 2.1. Trang chủ (Home)
- **Căn cứ:** PBI-06, PBI-07, PBI-10, PBI-40 (hiệu năng < 3s).
- **Cấu trúc:**
  - Hero banner giới thiệu nền tảng
  - Thanh tìm kiếm nổi bật
  - Danh mục nổi bật (Rau củ, Trái cây, Pre-order theo mùa…)
  - Section "Sản phẩm theo mùa / Pre-order" (badge `badge--seasonal`)
  - Grid sản phẩm nổi bật/gợi ý theo khu vực
  - Footer (liên hệ, chính sách)

### 2.2. Trang Danh sách/Tìm kiếm sản phẩm (Product Listing & Search)
- **Căn cứ:** PBI-14 (tìm kiếm và lọc).
- **Cấu trúc:**
  - Thanh tìm kiếm + bộ lọc (danh mục, giá, khu vực/trang trại, loại: thường/pre-order)
  - Grid sản phẩm responsive (2 cột mobile → 5 cột desktop lớn, mục 5.3)
  - Phân trang / load more
  - Trạng thái rỗng khi không có kết quả (empty state)

### 2.3. Trang Chi tiết sản phẩm (Product Detail)
- **Căn cứ:** FR-08/09 (truy xuất nguồn gốc), PBI-17 (pre-order), PBI-36 (đánh giá), PBI-38 (uy tín người bán).
- **Cấu trúc:**
  - Gallery ảnh sản phẩm
  - Tên sản phẩm, giá (`price`), badge trạng thái (Pre-order/Theo mùa)
  - Thông tin trang trại (tên, chỉ số uy tín người bán — PBI-38)
  - Khối truy xuất nguồn gốc QR (`trace-block`): mã QR, tên trang trại, ngày thu hoạch, mã lô hàng (PBI-27, PBI-28)
  - Mô tả sản phẩm
  - Chọn số lượng + nút "Thêm vào giỏ" / "Đặt trước" (nếu là pre-order)
  - Section đánh giá & bình luận (PBI-36)
  - Sản phẩm liên quan

### 2.4. Trang Giỏ hàng (Cart)
- **Căn cứ:** PBI-16.
- **Cấu trúc:**
  - Danh sách sản phẩm trong giỏ (card dạng dòng: ảnh, tên, đơn giá, số lượng điều chỉnh được, thành tiền)
  - Tổng tạm tính
  - Nút "Tiến hành đặt hàng"
  - Empty state khi giỏ trống

### 2.5. Trang Đặt hàng / Thanh toán (Checkout)
- **Căn cứ:** PBI-17 (pre-order & đặt cọc), PBI-18 (thanh toán COD/mô phỏng), PBI-21 (địa chỉ & phí vận chuyển).
- **Cấu trúc (stepper tối đa vài bước, theo tinh thần tối giản PBI-07):**
  - Bước 1: Địa chỉ giao hàng (chọn/thêm địa chỉ) + tính phí vận chuyển hiển thị realtime
  - Bước 2: Phương thức thanh toán (COD / thanh toán trực tuyến mô phỏng), nếu là pre-order hiển thị rõ % đặt cọc
  - Bước 3: Xem lại đơn hàng & xác nhận đặt hàng
  - Thông báo thành công (toast) kèm mã đơn

### 2.6. Trang Đơn hàng của tôi (Order List & Order Detail)
- **Căn cứ:** PBI-19 (quản lý đơn hàng & trạng thái), PBI-25/26 (đồng kiểm, xác nhận "Đã giao"), PBI-24 (chat).
- **Cấu trúc:**
  - Danh sách đơn hàng dạng tab/filter theo trạng thái (Chờ xác nhận, Đang giao, Đã giao, Đã hủy) — badge trạng thái
  - Trang chi tiết đơn: thông tin sản phẩm, trạng thái dạng timeline, link xác nhận đồng kiểm/giao hàng, nút "Khiếu nại", nút "Chat với người bán" (PBI-24), nút "Đánh giá" khi đã giao

### 2.7. Trang Khiếu nại (Complaint)
- **Căn cứ:** PBI-29 (gửi khiếu nại kèm ảnh/video trong 24h), PBI-30 (SLA phản hồi 24h).
- **Cấu trúc:**
  - Form chọn đơn hàng liên quan, lý do khiếu nại, upload ảnh/video minh chứng
  - Trạng thái xử lý khiếu nại (đang chờ/đã phản hồi/đã hoàn tiền)
  - Lịch sử trao đổi với Admin/người bán

### 2.8. Trang Chat (Buyer ↔ Farmer)
- **Căn cứ:** PBI-24.
- **Cấu trúc:** danh sách hội thoại theo người bán, khung chat realtime, gửi ảnh kèm tin nhắn.

### 2.9. Trang Hồ sơ cá nhân (Profile) & Sổ địa chỉ
- **Căn cứ:** PBI-21 (địa chỉ giao hàng), thông tin tài khoản chung.
- **Cấu trúc:**
  - Thông tin cá nhân (tên, số điện thoại, email, đổi mật khẩu)
  - Quản lý sổ địa chỉ giao hàng (thêm/sửa/xóa, đặt mặc định)
  - Lịch sử đánh giá đã viết
  - Đăng xuất

### 2.10. Trang Thông báo (Notifications)
- **Căn cứ:** PBI-20 (thông báo trong ứng dụng).
- **Cấu trúc:** danh sách thông báo (đơn hàng cập nhật trạng thái, khiếu nại được phản hồi, pre-order sắp đến mùa thu hoạch…), đánh dấu đã đọc.

---

## 3. NHÓM TRANG FARMER (NÔNG DÂN)

Layout: Dashboard với Sidebar trái cố định (desktop ≥1024px) / Sidebar slide-in overlay (mobile) — viền accent `--color-role-farmer`, theo mục 9.2 UIUXpromax.

### 3.1. Trang Tổng quan (Dashboard)
- **Căn cứ:** mục tiêu dễ dùng với nông dân ít rành công nghệ (mục 1.3), PBI-07.
- **Cấu trúc:**
  - Thẻ tóm tắt nhanh: số đơn chờ xử lý, doanh thu tạm tính, sản phẩm sắp hết hàng
  - Danh sách đơn hàng mới cần xác nhận
  - Lối tắt "Đăng sản phẩm mới" (nút nổi bật, primary action duy nhất của màn hình theo nguyên tắc 1 trong mục 1 UIUXpromax)

### 3.2. Trang Hồ sơ trang trại (Farm Profile)
- **Căn cứ:** PBI-02 (thông tin tối thiểu), PBI-11 (thông tin mở rộng), PBI-37 (xác minh chứng nhận — do Admin duyệt nhưng Farmer nộp hồ sơ tại đây).
- **Cấu trúc:**
  - Thông tin tối thiểu: tên trang trại, địa chỉ, số điện thoại, ảnh đại diện
  - Thông tin mở rộng: giới thiệu, hình ảnh trang trại, chứng nhận (VietGAP, hữu cơ…) để Admin xác minh
  - Chỉ số uy tín hiển thị cho Farmer tự theo dõi (liên kết PBI-38)

### 3.3. Trang Đăng sản phẩm (Create/Edit Product — Stepper ≤ 3 bước)
- **Căn cứ:** PBI-03 (bắt buộc ≤ 3 bước), PBI-04 (sản lượng & lịch thu hoạch), PBI-05 (nén ảnh phía client), PBI-12 (combo sản phẩm), PBI-13 (hướng dẫn thao tác).
- **Cấu trúc (đúng component `stepper` mục 10.5 UIUXpromax):**
  - Bước 1 — Thông tin cơ bản: tên, danh mục, ảnh (tự nén phía client), mô tả
  - Bước 2 — Giá & sản lượng: giá bán, đơn vị, số lượng tồn, lịch thu hoạch dự kiến, bật/tắt chế độ Pre-order theo mùa, gán vào combo (nếu có)
  - Bước 3 — Xem lại & đăng: preview như Buyer sẽ thấy, nút "Đăng sản phẩm"
  - Tooltip/hướng dẫn ngắn ở mỗi bước (PBI-13)

### 3.4. Trang Quản lý sản phẩm & Combo (Product & Combo Management)
- **Căn cứ:** PBI-04, PBI-12.
- **Cấu trúc:**
  - Danh sách sản phẩm đang bán dạng bảng (desktop) / card dọc (mobile, theo mục 10.9)
  - Bộ lọc theo trạng thái (còn hàng/hết hàng/đang pre-order)
  - Quản lý combo: gộp nhiều sản phẩm thành 1 gói bán
  - Cập nhật nhanh tồn kho, giá, ngày thu hoạch

### 3.5. Trang Nhật ký trồng trọt & QR truy xuất (Traceability Log)
- **Căn cứ:** PBI-27 (cập nhật nhật ký trồng trọt), PBI-28 (sinh mã QR truy xuất nguồn gốc), PBI-35 (toàn vẹn dữ liệu).
- **Cấu trúc:**
  - Timeline nhật ký canh tác theo từng lô hàng (ngày gieo trồng, bón phân, thu hoạch…)
  - Nút sinh mã QR cho lô hàng mới
  - Xem trước khối truy xuất (`trace-block`) như Buyer sẽ thấy khi quét mã

### 3.6. Trang Quản lý đơn hàng (Order Management)
- **Căn cứ:** PBI-19, PBI-25/26 (đồng kiểm), PBI-32 (tự động hoàn tiền khi thiếu hàng pre-order).
- **Cấu trúc:**
  - Danh sách đơn hàng cần xử lý theo trạng thái (Chờ xác nhận, Đang chuẩn bị, Đang giao, Đã giao, Khiếu nại)
  - Xác nhận đơn / cập nhật trạng thái giao hàng thủ công
  - Cảnh báo khi không đủ hàng cho đơn pre-order → kích hoạt luồng hoàn tiền tự động (PBI-32)

### 3.7. Trang Chat (Farmer ↔ Buyer)
- **Căn cứ:** PBI-24. Cấu trúc tương tự mục 2.8 nhưng từ góc nhìn Farmer (danh sách hội thoại theo khách hàng).

### 3.8. Trang Khiếu nại nhận được (Complaint Inbox)
- **Căn cứ:** PBI-25 (phản hồi khiếu nại, SLA 24h).
- **Cấu trúc:** danh sách khiếu nại từ Buyer liên quan đến đơn của mình, form phản hồi, đính kèm minh chứng, trạng thái SLA đếm ngược 24h.

### 3.9. Trang Báo cáo doanh thu (Revenue Report)
- **Căn cứ:** liên quan mục tiêu 6 (công cụ quản trị) áp dụng ở quy mô Farmer; biểu đồ bar-chart đơn giản theo icon `bar-chart-2`.
- **Cấu trúc:** biểu đồ doanh thu theo thời gian, danh sách sản phẩm bán chạy.

---

## 4. NHÓM TRANG ADMIN (QUẢN TRỊ VIÊN)

Layout: Dashboard Sidebar trái, nền sidebar đậm hơn (`--color-neutral-800`), viền accent `--color-role-admin` — theo mục 9.2 & 3.4 UIUXpromax.

### 4.1. Trang Tổng quan quản trị (Admin Dashboard)
- **Căn cứ:** mục tiêu sản phẩm (6) — công cụ quản trị tổng quan.
- **Cấu trúc:** thẻ số liệu tổng (số Farmer chờ duyệt, số khiếu nại mở, tổng đơn hàng, doanh thu toàn hệ thống), biểu đồ nhanh.

### 4.2. Trang Duyệt tài khoản Farmer (Farmer Approval)
- **Căn cứ:** PBI-33 (khóa tài khoản), PBI-37 (xác minh chứng nhận người bán).
- **Cấu trúc:**
  - Danh sách Farmer chờ duyệt / đã duyệt / bị từ chối
  - Xem chi tiết hồ sơ trang trại + chứng nhận đã nộp
  - Nút Duyệt / Từ chối / Khóa tài khoản (kèm modal xác nhận hành động nguy hiểm — mục 10.8)

### 4.3. Trang Quản lý người dùng (User Management)
- **Căn cứ:** PBI-01 (phân quyền), PBI-33 (khóa tài khoản & gỡ sản phẩm).
- **Cấu trúc:** bảng danh sách Farmer/Buyer, tìm kiếm/lọc, khóa/mở khóa tài khoản (khóa Farmer → tự động gỡ sản phẩm khỏi hệ thống).

### 4.4. Trang Quản lý danh mục sản phẩm (Category Management)
- **Căn cứ:** mục tiêu sản phẩm (6) — quản lý danh mục.
- **Cấu trúc:** CRUD danh mục/nhóm sản phẩm (Rau củ, Trái cây, Pre-order theo mùa…).

### 4.5. Trang Xử lý khiếu nại & Tranh chấp (Dispute Resolution)
- **Căn cứ:** PBI-30 (phản hồi & SLA 24h), PBI-31 (phân xử tranh chấp & hoàn tiền), PBI-34 (lưu trữ minh chứng).
- **Cấu trúc:**
  - Danh sách khiếu nại đang mở, lọc theo SLA sắp hết hạn
  - Chi tiết khiếu nại: nội dung từ Buyer, phản hồi từ Farmer, minh chứng ảnh/video
  - Hành động: phân xử, ra quyết định hoàn tiền/không hoàn tiền (nút `btn--danger` cho hành động quyết liệt)

### 4.6. Trang Báo cáo tổng quan hệ thống (System Reports)
- **Căn cứ:** mục tiêu sản phẩm (6) — công cụ báo cáo; đối chiếu độ đo chất lượng (Bảng 2.3).
- **Cấu trúc:** biểu đồ doanh thu toàn hệ thống, số đơn theo trạng thái, top sản phẩm/Farmer, xuất báo cáo.

### 4.7. Trang Nhật ký hệ thống (Audit Log)
- **Căn cứ:** PBI-23 (ghi log thao tác quản trị và giao dịch), NFR-13.
- **Cấu trúc:** bảng log có thể lọc theo loại thao tác, người thực hiện, thời gian (desktop: table sticky header; mobile: card dọc theo mục 10.9).

---

## 5. NAVIGATION BAR — THIẾT KẾ CHI TIẾT

Tất cả navigation đều tuân icon system không-emoji (mục 7 UIUXpromax) và vùng chạm tối thiểu 44×44px trên mobile (mục 8.2).

### 5.1. Buyer — Desktop (≥1024px)

**Header trên cùng (sticky, cao 64px, viền trên 4px màu `--color-role-buyer`):**
| Vị trí | Thành phần | Icon (Lucide) |
|---|---|---|
| Trái | Logo nền tảng (link về Trang chủ) | — |
| Giữa | Thanh tìm kiếm sản phẩm (rộng, luôn hiện) | `search` |
| Phải | Giỏ hàng (badge số lượng) | `shopping-cart` |
| Phải | Thông báo (badge số chưa đọc) | `bell` |
| Phải | Avatar → dropdown: Hồ sơ, Đơn hàng của tôi, Đăng xuất | `user-circle` |

**Thanh danh mục ngang (ngay dưới header):**
`Rau củ | Trái cây | Pre-order theo mùa | Combo | Theo khu vực...` — mỗi mục là link đến trang Danh sách sản phẩm (2.2) đã áp bộ lọc tương ứng.

### 5.2. Buyer — Mobile (<768px)

**Header (56–64px):** `[Icon menu ☰]` `[Logo]` `[Giỏ hàng][Avatar]`
**Thanh tìm kiếm:** full-width, ngay dưới header.
**Danh mục:** dạng chip cuộn ngang (scroll-x).

**Bottom Navigation Bar (cố định đáy, 5 mục chính — thay cho menu ngang để thao tác 1 tay):**
| Thứ tự | Nhãn | Icon | Trang liên kết |
|---|---|---|---|
| 1 | Trang chủ | `home`/`leaf` | 2.1 Trang chủ |
| 2 | Tìm kiếm | `search` | 2.2 Danh sách/Tìm kiếm |
| 3 | Giỏ hàng | `shopping-cart` (badge số lượng) | 2.4 Giỏ hàng |
| 4 | Đơn hàng | `package` | 2.6 Đơn hàng của tôi |
| 5 | Tôi | `user` | 2.9 Hồ sơ cá nhân |

### 5.3. Farmer — Sidebar (Desktop ≥1024px cố định bên trái 260px / Mobile <1024px slide-in overlay)

Header phía trên sidebar có viền accent `--color-role-farmer`. Sidebar dạng menu dọc, mỗi mục gồm icon + nhãn:

| Thứ tự | Nhãn | Icon | Trang liên kết |
|---|---|---|---|
| 1 | Tổng quan | `bar-chart-2` | 3.1 Dashboard |
| 2 | Hồ sơ trang trại | `tractor` | 3.2 Farm Profile |
| 3 | Đăng sản phẩm | `plus-circle` | 3.3 Đăng sản phẩm |
| 4 | Sản phẩm & Combo | `leaf` | 3.4 Quản lý sản phẩm |
| 5 | Nhật ký & QR truy xuất | `qr-code` | 3.5 Traceability Log |
| 6 | Đơn hàng | `package` | 3.6 Quản lý đơn hàng |
| 7 | Tin nhắn | `message-circle` | 3.7 Chat |
| 8 | Khiếu nại | `alert-triangle` | 3.8 Complaint Inbox |
| 9 | Báo cáo doanh thu | `bar-chart-2` | 3.9 Revenue Report |

Mobile: icon `menu` ở header mở sidebar trượt từ trái kèm overlay tối; icon `x` hoặc tap ra ngoài để đóng.

### 5.4. Admin — Sidebar (nền đậm `--color-neutral-800`, viền accent `--color-role-admin`)

| Thứ tự | Nhãn | Icon | Trang liên kết |
|---|---|---|---|
| 1 | Tổng quan | `bar-chart-2` | 4.1 Admin Dashboard |
| 2 | Duyệt Farmer | `shield` | 4.2 Farmer Approval |
| 3 | Quản lý người dùng | `user` | 4.3 User Management |
| 4 | Danh mục sản phẩm | `leaf` | 4.4 Category Management |
| 5 | Khiếu nại & Tranh chấp | `alert-triangle` | 4.5 Dispute Resolution |
| 6 | Báo cáo hệ thống | `bar-chart-2` | 4.6 System Reports |
| 7 | Nhật ký hệ thống | `database` | 4.7 Audit Log |

Header phía trên sidebar: avatar Admin → dropdown "Đăng xuất"; icon `bell` cho thông báo khiếu nại mới/SLA sắp hết hạn.

### 5.5. Quy tắc chung áp dụng cho mọi Navigation Bar
- Mục đang active: đổi màu icon + chữ sang màu accent vai trò tương ứng (`--color-role-farmer/buyer/admin`), nền nhẹ `--color-primary-50` (hoặc tương đương theo vai trò).
- Badge số lượng (giỏ hàng, thông báo chưa đọc) dùng `badge--danger` hoặc màu nổi bật, không dùng emoji.
- Icon đơn độc không kèm chữ (ví dụ nút giỏ hàng chỉ icon trên mobile) **bắt buộc có `aria-label`** theo chuẩn A11y mục 13.
- Toàn bộ mục nav phải tab tới được bằng bàn phím, có `:focus-visible` rõ ràng.

---

## 6. BẢNG ĐỐI CHIẾU NHANH (Trang ↔ Vai trò ↔ PBI chính)

| Nhóm | Số trang | PBI/FR tham chiếu chính |
|---|---|---|
| Public/Chung | 4 | PBI-01, 08, 15 |
| Buyer | 10 | PBI-06, 07, 10, 14, 16, 17, 18, 19, 20, 21, 24, 25, 26, 29, 30, 36, 38 |
| Farmer | 9 | PBI-02, 03, 04, 05, 11, 12, 13, 24, 25, 27, 28, 32, 35, 37 |
| Admin | 7 | PBI-01, 23, 30, 31, 33, 34, 37 |

**Tổng cộng: 30 trang**, bao phủ đầy đủ 3 actor và các PBI ưu tiên High/Medium trong phạm vi MVP (mục 1.5.3 — không bao gồm tính năng ngoài phạm vi như thanh toán thật, AI recommendation, logistics thật).
