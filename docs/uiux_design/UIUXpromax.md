# UI/UX PRO MAX — DESIGN SYSTEM
## Nền tảng kết nối nông sản trực tiếp từ nông dân đến người tiêu dùng

> Tài liệu này là **nguồn chân lý duy nhất (single source of truth)** về giao diện. Mọi màn hình, mọi component mới khi phát triển phải tuân thủ các token, quy tắc, và cấu trúc được định nghĩa ở đây. Không tự ý bịa thêm màu, font, spacing ngoài hệ thống token bên dưới.

---

## MỤC LỤC

1. Triết lý & nguyên tắc thiết kế
2. Design Tokens (biến CSS gốc)
3. Màu sắc (Color System)
4. Typography (Font chữ & phân cấp)
5. Spacing, Grid & Layout
6. Bo góc, đổ bóng, hiệu ứng
7. Icon System (KHÔNG dùng emoji)
8. Breakpoints & chiến lược Responsive
9. Cấu trúc trang & Layout chuẩn theo Desktop/Tablet/Mobile
10. Component Library chi tiết
11. Trạng thái (States) & Tương tác
12. Quy ước đặt tên class/id (Naming Convention)
13. Cấu trúc thư mục dự án Frontend
14. Accessibility (A11y) & NFR giao diện
15. Checklist bàn giao cho AI/Dev khi tạo giao diện mới

---

## 1. TRIẾT LÝ & NGUYÊN TẮC THIẾT KẾ

Thiết kế bám sát 3 mục tiêu phi chức năng cốt lõi của đề tài:

| # | Nguyên tắc | Diễn giải áp dụng vào UI |
|---|---|---|
| 1 | **Tối giản cho nông dân** | Luồng đăng sản phẩm tối đa **3 bước**. Mỗi màn hình chỉ có 1 hành động chính (primary action) nổi bật, hạn chế lựa chọn gây phân tâm. |
| 2 | **Chữ đủ lớn, dễ đọc (NFR-02)** | Cỡ chữ cơ bản (body) **tối thiểu 16px**, không dùng chữ nhỏ hơn 12px ở bất kỳ đâu. |
| 3 | **Mobile-first, Responsive chuẩn (NFR-01)** | Viết CSS từ màn hình nhỏ nhất mở rộng dần lên desktop (min-width media query), không viết ngược lại. |
| 4 | **Tiếng Việt, đơn vị VNĐ (NFR-10)** | Toàn bộ nhãn, placeholder, thông báo bằng tiếng Việt có dấu; định dạng tiền: `1.250.000₫` (dấu chấm ngăn cách hàng nghìn, ký hiệu ₫ sau số). |
| 5 | **Minh bạch & tin cậy** | Mọi thông tin nguồn gốc (trang trại, ngày thu hoạch, QR) phải hiển thị nổi bật, có khung/nhãn riêng, không lẫn vào mô tả chung. |
| 6 | **Nhất quán 3 vai trò** | Farmer, Buyer, Admin dùng chung 1 bộ token (màu, font, spacing, component) — chỉ khác **màu nhấn phụ theo vai trò** (xem mục 3.4) để người dùng tự nhận biết mình đang ở khu vực nào, tránh thiết kế 3 hệ giao diện rời rạc.

---

## 2. DESIGN TOKENS (BIẾN CSS GỐC)

Tạo file riêng **`tokens.css`**, nạp **đầu tiên** trước mọi file CSS khác. Toàn bộ giá trị màu, cỡ chữ, khoảng cách trong các file CSS khác **bắt buộc dùng biến `var(--...)`** từ file này, không hard-code lại giá trị số/hex.

```css
/* ============================================
   tokens.css — DESIGN TOKENS GỐC - KHÔNG SỬA TRỰC TIẾP
   Mọi file khác chỉ được đọc (var(--...)), không ghi đè giá trị số.
   ============================================ */
:root {
  /* ---------- 1. BRAND COLOR — Xanh nông sản ---------- */
  --color-primary-50:  #eef7ee;
  --color-primary-100: #d4ecd6;
  --color-primary-200: #a9d9ad;
  --color-primary-300: #7ec67f; /* đồng bộ dải màu, không chẵn tỉ lệ tuyệt đối */
  --color-primary-400: #52a854;
  --color-primary-500: #2e8b3d; /* MÀU THƯƠNG HIỆU CHÍNH */
  --color-primary-600: #246f31;
  --color-primary-700: #1c5726;
  --color-primary-800: #153f1c;
  --color-primary-900: #0d2611;

  /* ---------- 2. SECONDARY COLOR — Cam đất (đất nông nghiệp, mùa vụ) ---------- */
  --color-secondary-50:  #fdf2e7;
  --color-secondary-100: #fadfc2;
  --color-secondary-300: #f0a85c;
  --color-secondary-500: #e2822c; /* dùng cho Pre-order, mùa vụ, badge nổi bật */
  --color-secondary-600: #c06a1c;
  --color-secondary-700: #955115;

  /* ---------- 3. NEUTRAL / GRAYSCALE ---------- */
  --color-neutral-0:   #ffffff;
  --color-neutral-50:  #f7f8f6;
  --color-neutral-100: #eef0ec;
  --color-neutral-200: #dde1d8;
  --color-neutral-300: #c3c9bd;
  --color-neutral-400: #9aa295;
  --color-neutral-500: #717a6c;
  --color-neutral-600: #545c50;
  --color-neutral-700: #3c423a;
  --color-neutral-800: #282d26;
  --color-neutral-900: #171a15;

  /* ---------- 4. SEMANTIC / TRẠNG THÁI ---------- */
  --color-success-bg:    #e5f6e8;
  --color-success-text:  #1c7a31;
  --color-success-border:#8fd6a0;

  --color-warning-bg:    #fff6e0;
  --color-warning-text:  #8a6200;
  --color-warning-border:#f0d479;

  --color-danger-bg:     #fdecec;
  --color-danger-text:   #c1282c;
  --color-danger-border: #f1a6a6;

  --color-info-bg:       #e8f1fb;
  --color-info-text:     #1d5aa6;
  --color-info-border:   #a7c6ec;

  /* ---------- 5. VAI TRÒ (ROLE ACCENT) — dùng cho header/sidebar riêng từng vai trò ---------- */
  --color-role-farmer:  var(--color-primary-500);   /* Nông dân: xanh thương hiệu */
  --color-role-buyer:   #2f6fa8;                     /* Người mua: xanh dương tin cậy */
  --color-role-admin:   #4a3f7a;                     /* Admin: tím than, nghiêm túc */

  /* ---------- 6. NỀN & CHỮ MẶC ĐỊNH ---------- */
  --color-bg-page:       var(--color-neutral-50);
  --color-bg-surface:    var(--color-neutral-0);
  --color-bg-surface-alt:var(--color-neutral-100);
  --color-text-heading:  var(--color-neutral-900);
  --color-text-body:     var(--color-neutral-700);
  --color-text-muted:    var(--color-neutral-500);
  --color-text-on-primary: var(--color-neutral-0);
  --color-border-default:  var(--color-neutral-200);
  --color-border-strong:   var(--color-neutral-300);
  --color-focus-ring:       #1b73e8;

  /* ---------- 7. TYPOGRAPHY ---------- */
  --font-family-base: 'Inter', 'Be Vietnam Pro', -apple-system, 'Segoe UI', Roboto, Arial, sans-serif;
  --font-family-heading: 'Be Vietnam Pro', 'Inter', -apple-system, 'Segoe UI', Roboto, Arial, sans-serif;
  --font-family-mono: 'JetBrains Mono', 'Courier New', monospace; /* dùng cho mã đơn, mã QR, mã truy xuất */

  --fs-xs:   0.8125rem; /* 13px — chỉ dùng cho timestamp, caption phụ, KHÔNG dùng cho nội dung thao tác */
  --fs-sm:   0.875rem;  /* 14px — label phụ, helper text */
  --fs-base: 1rem;      /* 16px — BODY MẶC ĐỊNH, bắt buộc tối thiểu theo NFR-02 */
  --fs-md:   1.125rem;  /* 18px — nội dung nhấn nhẹ, input quan trọng */
  --fs-lg:   1.25rem;   /* 20px — H5 / tiêu đề card */
  --fs-xl:   1.5rem;    /* 24px — H4 */
  --fs-2xl:  1.875rem;  /* 30px — H3 */
  --fs-3xl:  2.25rem;   /* 36px — H2 */
  --fs-4xl:  2.75rem;   /* 44px — H1 / Hero */

  --fw-regular:  400;
  --fw-medium:   500;
  --fw-semibold: 600;
  --fw-bold:     700;

  --lh-tight:  1.2;
  --lh-normal: 1.5;
  --lh-loose:  1.75;

  /* ---------- 8. SPACING SCALE (bội số 4px) ---------- */
  --space-1:  0.25rem; /* 4px */
  --space-2:  0.5rem;  /* 8px */
  --space-3:  0.75rem; /* 12px */
  --space-4:  1rem;    /* 16px */
  --space-5:  1.25rem; /* 20px */
  --space-6:  1.5rem;  /* 24px */
  --space-8:  2rem;    /* 32px */
  --space-10: 2.5rem;  /* 40px */
  --space-12: 3rem;    /* 48px */
  --space-16: 4rem;    /* 64px */
  --space-20: 5rem;    /* 80px */

  /* ---------- 9. BO GÓC ---------- */
  --radius-sm:   6px;
  --radius-md:   10px;
  --radius-lg:   16px;
  --radius-xl:   24px;
  --radius-pill: 999px;
  --radius-circle: 50%;

  /* ---------- 10. ĐỔ BÓNG ---------- */
  --shadow-xs: 0 1px 2px rgba(23, 26, 21, 0.06);
  --shadow-sm: 0 2px 6px rgba(23, 26, 21, 0.08);
  --shadow-md: 0 6px 16px rgba(23, 26, 21, 0.10);
  --shadow-lg: 0 14px 32px rgba(23, 26, 21, 0.14);
  --shadow-focus: 0 0 0 3px rgba(27, 115, 232, 0.35);

  /* ---------- 11. Z-INDEX LAYERS ---------- */
  --z-dropdown: 100;
  --z-sticky-header: 200;
  --z-overlay: 300;
  --z-modal: 400;
  --z-toast: 500;
  --z-tooltip: 600;

  /* ---------- 12. ANIMATION ---------- */
  --ease-standard: cubic-bezier(0.4, 0, 0.2, 1);
  --duration-fast: 120ms;
  --duration-base: 200ms;
  --duration-slow: 320ms;

  /* ---------- 13. LAYOUT CONTAINER ---------- */
  --container-max-width: 1200px;
  --container-padding-mobile: 1rem;
  --container-padding-desktop: 2rem;
  --header-height: 64px;
  --sidebar-width: 260px;
  --sidebar-width-collapsed: 72px;
}
```

---

## 3. MÀU SẮC (COLOR SYSTEM)

### 3.1. Logic chọn màu
- **Primary — Xanh lá `#2E8B3D`**: đại diện nông nghiệp, tươi mới, tin cậy. Dùng cho CTA chính, link, icon active, trạng thái "đã xác nhận".
- **Secondary — Cam đất `#E2822C`**: đại diện mùa vụ, đất đai, dùng cho **Pre-order**, badge "Theo mùa", cảnh báo nhẹ mang tính gợi chú ý (không phải lỗi).
- **Neutral**: nền, viền, chữ phụ — chiếm 70% diện tích giao diện, giúp sản phẩm/ảnh nông sản nổi bật.

### 3.2. Bảng áp dụng nhanh

| Mục đích sử dụng | Token |
|---|---|
| Nút hành động chính (Thêm vào giỏ, Đăng sản phẩm, Xác nhận đơn) | `--color-primary-500` nền, `--color-text-on-primary` chữ |
| Nút hover | `--color-primary-600` |
| Nút active/pressed | `--color-primary-700` |
| Badge "Pre-order / Theo mùa vụ" | nền `--color-secondary-50`, chữ `--color-secondary-700`, viền `--color-secondary-300` |
| Trạng thái đơn "Đã giao" / thành công | `--color-success-bg` / `--color-success-text` |
| Trạng thái "Chờ xử lý" / cảnh báo | `--color-warning-bg` / `--color-warning-text` |
| Trạng thái "Đã hủy" / lỗi / khiếu nại khẩn | `--color-danger-bg` / `--color-danger-text` |
| Thông tin trung tính (gợi ý, tooltip) | `--color-info-bg` / `--color-info-text` |
| Nền trang | `--color-bg-page` |
| Nền thẻ/card/modal | `--color-bg-surface` |
| Chữ tiêu đề | `--color-text-heading` |
| Chữ nội dung | `--color-text-body` |
| Chữ phụ/mô tả nhỏ | `--color-text-muted` |

### 3.3. Tỷ lệ tương phản (Contrast)
- Mọi cặp chữ/nền dùng cho nội dung đọc chính phải đạt **WCAG AA ≥ 4.5:1** (body text) và **≥ 3:1** cho chữ lớn ≥ 24px hoặc in đậm ≥ 18.66px.
- Không đặt chữ `--color-text-muted` trên nền `--color-neutral-100` trở lên tối — luôn kiểm tra tương phản trước khi style text phụ trên nền màu.

### 3.4. Màu nhận diện theo vai trò (Role Accent)
Mỗi vai trò có 1 dải màu nhấn viền trên cùng của header/sidebar để phân biệt khu vực đang thao tác — **không đổi toàn bộ bảng màu, chỉ đổi accent**:

| Vai trò | Biến | Vị trí áp dụng |
|---|---|---|
| Farmer (Nông dân) | `--color-role-farmer` | Thanh viền trên cùng header 4px + icon active trong sidebar Farmer |
| Buyer (Người mua) | `--color-role-buyer` | Thanh viền trên cùng header 4px + icon active trong menu Buyer |
| Admin (Quản trị) | `--color-role-admin` | Thanh viền trên cùng header 4px + sidebar nền đậm hơn `--color-neutral-800` |

---

## 4. TYPOGRAPHY (FONT CHỮ & PHÂN CẤP)

### 4.1. Font chữ
- **Font chính (`--font-family-base`)**: **Inter** — rõ ràng, hỗ trợ tiếng Việt có dấu đầy đủ, tối ưu đọc trên màn hình nhỏ.
- **Font tiêu đề (`--font-family-heading`)**: **Be Vietnam Pro** — font Việt hóa, nét dày hơn, tạo bản sắc "nông sản Việt" cho H1–H3, logo, banner.
- **Font số liệu/mã (`--font-family-mono`)**: **JetBrains Mono** — dùng riêng cho: mã đơn hàng, mã truy xuất QR, mã OTP, số lô hàng → giúp số liệu dễ đọc, không nhầm lẫn 0/O, 1/l.
- Nạp font qua Google Fonts (`<link>` trong `<head>`), có **fallback hệ thống** đầy đủ như khai báo trong token để không vỡ layout khi mất mạng.

```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Be+Vietnam+Pro:wght@500;600;700&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
```

### 4.2. Bảng phân cấp cỡ chữ (Type Scale)

| Cấp | Token | Kích thước | Weight | Line-height | Dùng cho |
|---|---|---|---|---|---|
| Display | `--fs-4xl` | 44px | `--fw-bold` | 1.2 | Hero banner trang chủ |
| H1 | `--fs-3xl` | 36px | `--fw-bold` | 1.2 | Tiêu đề trang chính (vd "Sản phẩm của tôi") |
| H2 | `--fs-2xl` | 30px | `--fw-semibold` | 1.25 | Tiêu đề section lớn |
| H3 | `--fs-xl` | 24px | `--fw-semibold` | 1.3 | Tiêu đề khối nội dung/modal |
| H4 | `--fs-lg` | 20px | `--fw-semibold` | 1.35 | Tiêu đề card, tên sản phẩm trong trang chi tiết |
| H5 | `--fs-md` | 18px | `--fw-medium` | 1.4 | Tiêu đề phụ, tên sản phẩm trong danh sách |
| Body (mặc định) | `--fs-base` | **16px** | `--fw-regular` | 1.5 | **Toàn bộ nội dung đọc, form, nút** — KHÔNG được nhỏ hơn mức này |
| Body nhấn | `--fs-base` | 16px | `--fw-medium` | 1.5 | Label quan trọng, giá tiền trong danh sách |
| Small / Helper | `--fs-sm` | 14px | `--fw-regular` | 1.45 | Helper text dưới input, phụ chú ngắn |
| Caption | `--fs-xs` | 13px | `--fw-regular` | 1.4 | Timestamp, số lượt đánh giá — **giới hạn phạm vi sử dụng, không dùng cho nội dung thao tác** |

> **Ràng buộc cứng (NFR-02):** `body { font-size: var(--fs-base); }` là mốc sàn. Mọi phần tử tương tác được (nút, input, link thao tác, label chính) **không được nhỏ hơn 16px**. Chỉ caption/timestamp được phép 13px.

### 4.3. Giá tiền (số liệu quan trọng)
```css
.price {
  font-family: var(--font-family-heading);
  font-weight: var(--fw-bold);
  font-size: var(--fs-lg);
  color: var(--color-primary-700);
}
.price--strike {
  font-weight: var(--fw-regular);
  color: var(--color-text-muted);
  text-decoration: line-through;
  font-size: var(--fs-sm);
}
```

---

## 5. SPACING, GRID & LAYOUT

### 5.1. Nguyên tắc spacing
- Dùng **thang bội số 4px** (`--space-1` → `--space-20`), **không dùng số lẻ** như 15px, 18px, 23px...
- Khoảng cách giữa các section lớn: `--space-12` (desktop) / `--space-8` (mobile).
- Khoảng cách trong 1 card (padding): `--space-6` (desktop) / `--space-4` (mobile).
- Khoảng cách giữa các field form: `--space-5`.

### 5.2. Grid hệ thống (CSS Grid thuần — không framework)
```css
.container {
  width: 100%;
  max-width: var(--container-max-width);
  margin-inline: auto;
  padding-inline: var(--container-padding-mobile);
}
@media (min-width: 1024px) {
  .container { padding-inline: var(--container-padding-desktop); }
}

/* Grid 12 cột dùng chung toàn hệ thống */
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr); /* mobile: 4 cột */
  gap: var(--space-4);
}
@media (min-width: 768px) {
  .grid { grid-template-columns: repeat(8, 1fr); gap: var(--space-5); }
}
@media (min-width: 1024px) {
  .grid { grid-template-columns: repeat(12, 1fr); gap: var(--space-6); }
}

/* Helper chiếm cột - ví dụ dùng cho card sản phẩm responsive */
.col-span-4  { grid-column: span 4; }
.col-span-6  { grid-column: span 6; }
.col-span-12 { grid-column: span 12; }
```

### 5.3. Lưới danh sách sản phẩm (Product Grid) — ví dụ áp dụng thực tế
| Breakpoint | Số cột sản phẩm/hàng |
|---|---|
| Mobile (< 600px) | 2 cột |
| Tablet (600–1023px) | 3 cột |
| Desktop (≥ 1024px) | 4 cột |
| Desktop lớn (≥ 1440px) | 5 cột |

```css
.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
}
@media (min-width: 600px)  { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (min-width: 1024px) { .product-grid { grid-template-columns: repeat(4, 1fr); gap: var(--space-6); } }
@media (min-width: 1440px) { .product-grid { grid-template-columns: repeat(5, 1fr); } }
```

---

## 6. BO GÓC, ĐỔ BÓNG, HIỆU ỨNG

| Thành phần | Bo góc | Đổ bóng |
|---|---|---|
| Button | `--radius-md` (10px) | không shadow mặc định, shadow-sm khi hover |
| Card sản phẩm | `--radius-lg` (16px) | `--shadow-sm`, hover → `--shadow-md` |
| Input / Select / Textarea | `--radius-sm` (6px) | none, focus → `--shadow-focus` |
| Modal / Dialog | `--radius-xl` (24px) | `--shadow-lg` |
| Badge / Tag | `--radius-pill` | none |
| Avatar | `--radius-circle` | none |
| Toast notification | `--radius-md` | `--shadow-md` |

**Transition chuẩn cho mọi hover/focus/active:**
```css
.transition-base {
  transition: background-color var(--duration-base) var(--ease-standard),
              border-color var(--duration-base) var(--ease-standard),
              box-shadow var(--duration-base) var(--ease-standard),
              transform var(--duration-fast) var(--ease-standard);
}
```

---

## 7. ICON SYSTEM — **TUYỆT ĐỐI KHÔNG DÙNG EMOJI**

### 7.1. Quy định bắt buộc
> ⚠️ **Mọi icon trong giao diện (nút, menu, trạng thái, thông báo, breadcrumb...) PHẢI dùng icon dạng SVG/vector từ thư viện mã nguồn mở, KHÔNG được chèn emoji Unicode (🌾, ✅, 🛒, 📦...) dưới bất kỳ hình thức nào** — kể cả trong text, trong title, trong placeholder.

### 7.2. Thư viện icon được duyệt dùng (chọn 1, dùng xuyên suốt dự án)

| Thư viện | Cách tích hợp (không framework) | Ghi chú |
|---|---|---|
| **Lucide Icons** (khuyến nghị chính) | Tải file SVG tĩnh về `/assets/icons/*.svg` rồi nhúng bằng `<img>` hoặc inline `<svg>`, **không dùng CDN JS runtime** (tránh phụ thuộc framework ẩn) | Bộ icon outline, nhất quán, nét 2px, phù hợp phong cách tối giản |
| **Phosphor Icons** | Tương tự — tải SVG tĩnh | Có 2 biến thể outline/fill để thể hiện trạng thái active |
| **Material Symbols (Google) dạng SVG** | Tải file `.svg` tĩnh, tự host | Dùng nếu cần bộ icon rộng hơn (nông nghiệp, vận chuyển) |

### 7.3. Cách nhúng icon chuẩn (ví dụ)
```html
<!-- Cách 1: inline SVG (khuyến nghị — có thể đổi màu bằng CSS currentColor) -->
<button class="btn btn--primary">
  <svg class="icon" width="20" height="20" viewBox="0 0 24 24" fill="none"
       stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z"/>
    <path d="M3 6h18"/><path d="M16 10a4 4 0 0 1-8 0"/>
  </svg>
  <span>Thêm vào giỏ</span>
</button>

<!-- Cách 2: dùng sprite SVG symbol để tối ưu tải trang -->
<svg class="icon"><use href="/assets/icons/sprite.svg#icon-cart"></use></svg>
```

```css
.icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  color: currentColor; /* icon thừa hưởng màu chữ của phần tử cha */
}
.icon--sm { width: 16px; height: 16px; }
.icon--lg { width: 28px; height: 28px; }
```

### 7.4. Bảng ánh xạ icon theo chức năng (để Dev/AI tra cứu nhanh, tên icon theo chuẩn Lucide)

| Chức năng | Tên icon (Lucide) |
|---|---|
| Giỏ hàng | `shopping-cart` |
| Tìm kiếm | `search` |
| Thông báo | `bell` |
| Hồ sơ/tài khoản | `user` hoặc `user-circle` |
| Đăng sản phẩm/thêm mới | `plus-circle` |
| Sản phẩm/nông sản | `leaf` hoặc `wheat` |
| Trang trại | `tractor` hoặc `home` |
| Mã QR truy xuất | `qr-code` |
| Đơn hàng | `package` |
| Lịch mùa vụ | `calendar` |
| Đặt trước (pre-order) | `clock` |
| Vận chuyển | `truck` |
| Đánh giá/sao | `star` |
| Chat | `message-circle` |
| Khiếu nại | `alert-triangle` |
| Thành công | `check-circle` |
| Cảnh báo | `alert-circle` |
| Lỗi/hủy | `x-circle` |
| Chỉnh sửa | `pencil` / `edit-3` |
| Xóa | `trash-2` |
| Lọc | `sliders-horizontal` |
| Menu (mobile) | `menu` |
| Đóng | `x` |
| Mũi tên xuống (dropdown) | `chevron-down` |
| Vị trí/địa chỉ | `map-pin` |
| Admin/quản trị | `shield` |
| Thống kê/báo cáo | `bar-chart-2` |
| Sao lưu | `database` |
| Khóa tài khoản | `lock` |

### 7.5. Ảnh minh họa / illustration (không phải icon chức năng)
- Trạng thái rỗng (empty state: giỏ hàng trống, không có đơn hàng...) dùng **illustration SVG phẳng (flat illustration)** tông màu xanh/cam của hệ thống, lấy từ thư viện mã nguồn mở có giấy phép tự do sử dụng (ví dụ unDraw, Storyset — chọn bản thuần SVG, không cần JS).
- **Không dùng emoji thay thế illustration** trong bất kỳ trường hợp nào.

---

## 8. BREAKPOINTS & CHIẾN LƯỢC RESPONSIVE

### 8.1. Bảng breakpoint chính thức

| Tên | Khoảng | Thiết bị điển hình | Ghi chú |
|---|---|---|---|
| `xs` (mặc định, không cần media query) | 0 – 599px | Điện thoại | **Đây là baseline — viết CSS mobile trước** |
| `sm` | ≥ 600px | Điện thoại lớn / Phablet ngang | |
| `md` | ≥ 768px | Tablet dọc | Chuyển sidebar sang dạng overlay → vẫn ẩn mặc định |
| `lg` | ≥ 1024px | Tablet ngang / Laptop nhỏ | **Breakpoint chuyển sang layout Desktop**: hiện sidebar cố định, header đầy đủ |
| `xl` | ≥ 1280px | Desktop chuẩn | Container đạt max-width |
| `2xl` | ≥ 1440px | Màn hình lớn | Tăng số cột grid sản phẩm |

```css
/* Mixin tham chiếu bằng comment vì CSS thuần không có mixin thật sự */
/* sm:  @media (min-width: 600px)  */
/* md:  @media (min-width: 768px)  */
/* lg:  @media (min-width: 1024px) */
/* xl:  @media (min-width: 1280px) */
/* 2xl: @media (min-width: 1440px) */
```

### 8.2. Quy tắc bắt buộc (NFR-01 — mobile-first)
1. Viết style mặc định (không media query) = style cho **mobile**.
2. Dùng **`min-width`** để mở rộng dần lên tablet/desktop — **cấm dùng `max-width`** làm chiến lược chính (tránh code chồng chéo khó bảo trì).
3. Test tối thiểu tại 4 mốc: **360px (điện thoại nhỏ), 768px (tablet), 1024px (laptop), 1440px (desktop lớn)** — tương ứng độ đo "4 cặp trình duyệt × kích thước" trong tiêu chuẩn kiểm thử của báo cáo gốc.
4. Vùng chạm (tap target) tối thiểu **44×44px** trên mobile cho mọi nút, icon bấm được.
5. Không có thao tác nào trên desktop (hover menu, tooltip hover) mà **không có cách thay thế bằng tap/click trên mobile**.

---

## 9. CẤU TRÚC TRANG & LAYOUT CHUẨN

### 9.1. Layout tổng thể — Buyer (Người mua)

**Desktop (≥1024px):**
```
┌─────────────────────────────────────────────────────┐
│ HEADER (sticky, height 64px, viền accent --color-role-buyer) │
│  [Logo]   [Thanh tìm kiếm rộng]   [Giỏ hàng][Thông báo][Avatar] │
├─────────────────────────────────────────────────────┤
│  [Thanh danh mục ngang: Rau củ | Trái cây | Pre-order...]  │
├─────────────────────────────────────────────────────┤
│                                                       │
│              NỘI DUNG CHÍNH (container max 1200px)   │
│              Grid sản phẩm 4-5 cột                   │
│                                                       │
├─────────────────────────────────────────────────────┤
│ FOOTER (thông tin liên hệ, chính sách, bản đồ trang)│
└─────────────────────────────────────────────────────┘
```

**Mobile (<768px):**
```
┌──────────────────────────────┐
│ HEADER (56-64px)              │
│ [Menu ☰] [Logo]  [Giỏ][Avatar]│
├──────────────────────────────┤
│ [Thanh tìm kiếm full-width]   │
├──────────────────────────────┤
│ [Danh mục cuộn ngang - scroll]│
├──────────────────────────────┤
│                                │
│   NỘI DUNG (grid 2 cột)       │
│                                │
├──────────────────────────────┤
│ BOTTOM NAV BAR (cố định đáy)  │
│ [Trang chủ][Tìm][Giỏ][Đơn][Tôi]│
└──────────────────────────────┘
```
> Trên mobile, **Buyer dùng Bottom Navigation Bar cố định** (5 mục chính) thay cho menu ngang trên đầu để tối ưu thao tác 1 tay.

### 9.2. Layout tổng thể — Farmer (Nông dân) & Admin

**Desktop (≥1024px):** Layout dạng **Dashboard với Sidebar cố định bên trái**
```
┌───────────┬───────────────────────────────────────┐
│           │ HEADER (viền accent theo vai trò)      │
│  SIDEBAR  ├───────────────────────────────────────┤
│  260px    │                                        │
│           │         NỘI DUNG CHÍNH                 │
│ [Icon+Nhãn]│         (dashboard, bảng, form)        │
│  menu dọc │                                        │
│           │                                        │
└───────────┴───────────────────────────────────────┘
```

**Mobile (<1024px) cho Farmer/Admin:** Sidebar ẩn mặc định, mở bằng icon `menu` ở header, trượt (slide-in) từ trái, có lớp phủ tối (overlay) phía sau, đóng bằng icon `x` hoặc tap ra ngoài overlay.

```css
.sidebar {
  position: fixed;
  top: 0; left: 0; bottom: 0;
  width: var(--sidebar-width);
  background: var(--color-bg-surface);
  transform: translateX(-100%);
  transition: transform var(--duration-base) var(--ease-standard);
  z-index: var(--z-overlay);
}
.sidebar.is-open { transform: translateX(0); }

@media (min-width: 1024px) {
  .sidebar { position: sticky; transform: none; } /* luôn hiện trên desktop */
  .app-layout { display: grid; grid-template-columns: var(--sidebar-width) 1fr; }
}
```

### 9.3. Quy tắc Header (dùng chung 3 vai trò)
- Chiều cao: `--header-height` (64px desktop, 56px mobile).
- `position: sticky; top: 0;`, `z-index: var(--z-sticky-header)`.
- Luôn có viền trên 4px màu accent vai trò (`--color-role-farmer/buyer/admin`) để phân biệt khu vực.
- Nền: `--color-bg-surface`, đổ bóng nhẹ `--shadow-xs` khi cuộn trang (thêm class `.is-scrolled` bằng JS khi `scrollY > 0`).

---

## 10. COMPONENT LIBRARY CHI TIẾT

> Mỗi component dưới đây là **chuẩn bắt buộc**. Khi AI/Dev tạo màn hình mới, phải tái sử dụng đúng cấu trúc HTML + class này, không tự sáng tạo cấu trúc mới cho cùng 1 loại component.

### 10.1. Button

```html
<button class="btn btn--primary btn--md">
  <svg class="icon"><use href="#icon-plus-circle"></use></svg>
  <span>Đăng sản phẩm</span>
</button>
```

| Biến thể class | Mục đích |
|---|---|
| `.btn--primary` | Hành động chính duy nhất trên 1 màn hình (nền primary-500) |
| `.btn--secondary` | Hành động phụ (viền primary-500, nền trong suốt) |
| `.btn--ghost` | Hành động nhẹ, không viền (vd "Hủy") |
| `.btn--danger` | Hành động phá hủy (xóa, hủy đơn, khóa tài khoản) |
| `.btn--sm` / `.btn--md` / `.btn--lg` | Kích thước — mobile mặc định dùng `--lg` cho CTA chính để đủ vùng chạm |

```css
.btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-family: var(--font-family-base);
  font-size: var(--fs-base);
  font-weight: var(--fw-semibold);
  border-radius: var(--radius-md);
  border: 1.5px solid transparent;
  cursor: pointer;
  min-height: 44px; /* vùng chạm chuẩn */
  padding: var(--space-3) var(--space-5);
}
.btn--primary   { background: var(--color-primary-500); color: var(--color-text-on-primary); }
.btn--primary:hover  { background: var(--color-primary-600); }
.btn--primary:active { background: var(--color-primary-700); }
.btn--secondary { background: transparent; border-color: var(--color-primary-500); color: var(--color-primary-700); }
.btn--ghost     { background: transparent; color: var(--color-text-body); }
.btn--danger    { background: var(--color-danger-text); color: var(--color-neutral-0); }
.btn:disabled   { opacity: 0.5; cursor: not-allowed; }
.btn:focus-visible { outline: none; box-shadow: var(--shadow-focus); }
```

### 10.2. Card sản phẩm (Product Card)
```html
<article class="product-card">
  <div class="product-card__media">
    <img src="..." alt="Xoài cát Hòa Lộc" loading="lazy">
    <span class="badge badge--seasonal">
      <svg class="icon icon--sm"><use href="#icon-clock"></use></svg> Pre-order
    </span>
  </div>
  <div class="product-card__body">
    <h3 class="product-card__title">Xoài cát Hòa Lộc</h3>
    <p class="product-card__farm">
      <svg class="icon icon--sm"><use href="#icon-map-pin"></use></svg> Trang trại Ba Tri, Bến Tre
    </p>
    <div class="product-card__footer">
      <span class="price">65.000₫<span class="price__unit">/kg</span></span>
      <button class="btn btn--primary btn--sm" aria-label="Thêm vào giỏ">
        <svg class="icon"><use href="#icon-shopping-cart"></use></svg>
      </button>
    </div>
  </div>
</article>
```
- Tỉ lệ ảnh cố định `aspect-ratio: 4 / 3` để lưới không bị lệch khi ảnh nông dân tải lên không đồng đều kích thước.
- Hover (desktop only): nâng nhẹ `transform: translateY(-4px)` + `--shadow-md`.

### 10.3. Badge / Tag trạng thái
```html
<span class="badge badge--success"><svg class="icon icon--sm"><use href="#icon-check-circle"/></svg> Đã giao</span>
<span class="badge badge--warning">Chờ xác nhận</span>
<span class="badge badge--danger">Đã hủy</span>
<span class="badge badge--seasonal">Theo mùa vụ</span>
```
```css
.badge {
  display: inline-flex; align-items: center; gap: var(--space-1);
  font-size: var(--fs-sm); font-weight: var(--fw-medium);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-pill);
  border: 1px solid transparent;
}
.badge--success  { background: var(--color-success-bg); color: var(--color-success-text); border-color: var(--color-success-border); }
.badge--warning  { background: var(--color-warning-bg); color: var(--color-warning-text); border-color: var(--color-warning-border); }
.badge--danger   { background: var(--color-danger-bg);  color: var(--color-danger-text);  border-color: var(--color-danger-border); }
.badge--seasonal { background: var(--color-secondary-50); color: var(--color-secondary-700); border-color: var(--color-secondary-300); }
```

### 10.4. Form Input
```html
<div class="form-field">
  <label class="form-field__label" for="qty">Số lượng (kg)</label>
  <input class="form-field__input" type="number" id="qty" min="1" placeholder="Ví dụ: 10">
  <p class="form-field__helper">Còn lại 48kg trong lô hàng này</p>
</div>

<div class="form-field form-field--error">
  <label class="form-field__label" for="phone">Số điện thoại</label>
  <input class="form-field__input" type="tel" id="phone" aria-invalid="true">
  <p class="form-field__error">
    <svg class="icon icon--sm"><use href="#icon-alert-circle"/></svg> Số điện thoại không hợp lệ
  </p>
</div>
```
```css
.form-field { display: flex; flex-direction: column; gap: var(--space-2); margin-bottom: var(--space-5); }
.form-field__label { font-size: var(--fs-base); font-weight: var(--fw-medium); color: var(--color-text-heading); }
.form-field__input {
  font-size: var(--fs-base); /* KHÔNG nhỏ hơn 16px để tránh iOS auto-zoom khi focus */
  min-height: 48px;
  padding: var(--space-3) var(--space-4);
  border: 1.5px solid var(--color-border-default);
  border-radius: var(--radius-sm);
  background: var(--color-bg-surface);
  color: var(--color-text-body);
}
.form-field__input:focus { outline: none; border-color: var(--color-focus-ring); box-shadow: var(--shadow-focus); }
.form-field--error .form-field__input { border-color: var(--color-danger-text); }
.form-field__helper { font-size: var(--fs-sm); color: var(--color-text-muted); }
.form-field__error  { font-size: var(--fs-sm); color: var(--color-danger-text); display: flex; align-items: center; gap: var(--space-1); }
```

### 10.5. Stepper luồng đăng sản phẩm (≤ 3 bước — PBI-03/07)
```html
<ol class="stepper">
  <li class="stepper__step is-done"><span class="stepper__circle">1</span><span>Thông tin cơ bản</span></li>
  <li class="stepper__step is-active"><span class="stepper__circle">2</span><span>Giá & sản lượng</span></li>
  <li class="stepper__step"><span class="stepper__circle">3</span><span>Xem lại & đăng</span></li>
</ol>
```
```css
.stepper { display: flex; gap: var(--space-4); list-style: none; }
.stepper__step { display: flex; align-items: center; gap: var(--space-2); color: var(--color-text-muted); font-weight: var(--fw-medium); }
.stepper__circle {
  width: 32px; height: 32px; border-radius: var(--radius-circle);
  display: grid; place-items: center;
  background: var(--color-neutral-200); color: var(--color-neutral-600);
  font-weight: var(--fw-semibold);
}
.stepper__step.is-active { color: var(--color-primary-700); }
.stepper__step.is-active .stepper__circle { background: var(--color-primary-500); color: #fff; }
.stepper__step.is-done .stepper__circle { background: var(--color-success-text); color: #fff; }
```

### 10.6. Khối truy xuất nguồn gốc QR (Traceability Block — FR-09)
```html
<section class="trace-block">
  <div class="trace-block__qr">
    <img src="qr-lohang-001.png" alt="Mã QR truy xuất nguồn gốc lô hàng #LH-001" width="140" height="140">
  </div>
  <dl class="trace-block__info">
    <div><dt>Trang trại</dt><dd>Trang trại Ba Tri, Bến Tre</dd></div>
    <div><dt>Ngày thu hoạch</dt><dd>12/11/2026</dd></div>
    <div><dt>Mã lô hàng</dt><dd class="mono">LH-2026-00142</dd></div>
  </dl>
</section>
```
```css
.trace-block {
  display: flex; gap: var(--space-5); align-items: center;
  padding: var(--space-5);
  background: var(--color-primary-50);
  border: 1px solid var(--color-primary-200);
  border-radius: var(--radius-lg);
}
.trace-block__info dt { font-size: var(--fs-sm); color: var(--color-text-muted); }
.trace-block__info dd { font-size: var(--fs-base); font-weight: var(--fw-medium); color: var(--color-text-heading); margin: 0 0 var(--space-2); }
.mono { font-family: var(--font-family-mono); }
```

### 10.7. Toast / Thông báo trong ứng dụng (PBI-20)
```html
<div class="toast toast--success" role="status">
  <svg class="icon"><use href="#icon-check-circle"/></svg>
  <span>Đặt hàng thành công! Mã đơn #DH-00231</span>
  <button class="toast__close" aria-label="Đóng thông báo"><svg class="icon icon--sm"><use href="#icon-x"/></svg></button>
</div>
```
- Vị trí: `position: fixed; top: var(--space-5); right: var(--space-5); z-index: var(--z-toast);` (mobile: full-width trên cùng, cách mép `var(--space-3)`).
- Tự ẩn sau 4s hoặc đóng thủ công; dùng `role="status"` để hỗ trợ đọc màn hình.

### 10.8. Modal/Dialog
- Overlay nền: `rgba(23,26,21,0.5)`, click ra ngoài → đóng (trừ modal xác nhận hành động nguy hiểm — phải bấm nút rõ ràng).
- Trên mobile (< 600px): modal chiếm full-width, trượt lên từ đáy màn hình (bottom-sheet) thay vì center — tối ưu thao tác 1 tay.

### 10.9. Bảng dữ liệu (Admin — quản lý tài khoản, đơn hàng)
- Desktop: bảng `<table>` chuẩn với header sticky.
- Mobile: chuyển bảng thành **danh sách card xếp dọc** (mỗi dòng dữ liệu → 1 card nhỏ), **không cho bảng co ngang kiểu tràn scroll ngang mất ngữ cảnh** (trừ khi bảng có trên 6 cột thì cho phép scroll ngang trong khung có chỉ báo rõ ràng).

---

## 11. TRẠNG THÁI (STATES) & TƯƠNG TÁC

Mọi component tương tác được phải định nghĩa đủ các trạng thái sau trong CSS, không được bỏ sót:

| Trạng thái | Yêu cầu |
|---|---|
| **Default** | Trạng thái gốc |
| **Hover** (desktop) | Đổi màu nền/viền nhẹ, không đổi layout gây giật |
| **Focus** | Bắt buộc `:focus-visible` với `box-shadow: var(--shadow-focus)` — phục vụ điều hướng bàn phím |
| **Active/Pressed** | Màu đậm hơn hover 1 bậc |
| **Disabled** | `opacity: 0.5`, `cursor: not-allowed`, loại khỏi tab order nếu cần |
| **Loading** | Dùng skeleton screen (khối xám `--color-neutral-100` nhấp nháy `animation: pulse`) cho card/list đang tải — **không dùng spinner emoji hoặc icon xoay kiểu GIF**, dùng SVG xoay bằng CSS `animation: spin` |
| **Empty state** | Illustration SVG + câu hướng dẫn hành động tiếp theo (vd "Chưa có đơn hàng nào — Khám phá sản phẩm ngay") |
| **Error state** | Khối thông báo lỗi `--color-danger-*` kèm icon `alert-circle` + nút thử lại |

```css
@keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.5; } }
.skeleton { background: var(--color-neutral-100); border-radius: var(--radius-sm); animation: pulse 1.4s ease-in-out infinite; }

@keyframes spin { to { transform: rotate(360deg); } }
.spinner { animation: spin 0.8s linear infinite; }
```

---

## 12. QUY ƯỚC ĐẶT TÊN CLASS/ID (NAMING CONVENTION)

Thống nhất với quy ước đã nêu trong báo cáo gốc (tiền tố + PascalCase cho `id`/`data-testid`), còn **class CSS dùng BEM** để tránh xung đột style khi dự án không có framework hỗ trợ scoped-CSS:

### 12.1. Class CSS — chuẩn BEM
```
.block                 → .product-card
.block__element        → .product-card__title
.block--modifier       → .btn--primary
.block__element--mod   → .form-field__input--error
```
- **Block**: 1 component độc lập (card, btn, modal, header...).
- **Element**: bộ phận bên trong block, nối bằng `__`.
- **Modifier**: biến thể trạng thái/kiểu dáng, nối bằng `--`.
- Trạng thái động do JS gán dùng tiền tố `is-`/`has-` (vd `.is-active`, `.is-open`, `.has-error`) — **tách riêng khỏi BEM modifier** để JS và CSS style không giẫm lên nhau khi maintain.

### 12.2. id / data-testid — theo chuẩn báo cáo gốc
Tiền tố theo loại phần tử + PascalCase, ví dụ: `btnSubmitOrder`, `inputPhoneNumber`, `modalConfirmDelete`, `cardProduct001`.

| Tiền tố | Loại phần tử |
|---|---|
| `btn` | Button |
| `input` | Input/Textarea |
| `select` | Dropdown/Select |
| `modal` | Modal/Dialog |
| `card` | Card |
| `form` | Form |
| `nav` | Navigation/Menu |
| `table` | Bảng dữ liệu |
| `toast` | Thông báo toast |
| `badge` | Badge/Tag |

### 12.3. Tên file CSS/JS (kebab-case)
```
tokens.css
base.css
components/button.css
components/product-card.css
layouts/header.css
pages/product-detail.css
scripts/cart.js
scripts/form-validation.js
```

---

## 13. ACCESSIBILITY (A11Y) & NFR GIAO DIỆN

| Yêu cầu | Chi tiết triển khai |
|---|---|
| Ngôn ngữ trang | `<html lang="vi">` |
| Ảnh sản phẩm | Luôn có `alt` mô tả tên sản phẩm, không để rỗng (trừ ảnh trang trí thuần túy thì `alt=""`) |
| Icon chức năng không kèm text | Bắt buộc `aria-label` (vd nút giỏ hàng chỉ có icon → `aria-label="Giỏ hàng"`) |
| Điều hướng bàn phím | Toàn bộ nút, link, input phải tab tới được theo đúng thứ tự logic, có `:focus-visible` rõ ràng |
| Form lỗi | Dùng `aria-invalid="true"` + `aria-describedby` trỏ tới id của dòng báo lỗi |
| Thông báo động (toast) | `role="status"` hoặc `role="alert"` tùy mức độ khẩn |
| Màu không phải kênh duy nhất truyền đạt thông tin | Trạng thái đơn hàng luôn có **icon + chữ**, không chỉ dựa vào màu (hỗ trợ người mù màu) |
| Cỡ chữ tối thiểu | 16px body (đã nêu mục 4) |
| Zoom | Không khóa `user-scalable=no` trong viewport meta — luôn cho phép người dùng pinch-zoom |

```html
<meta name="viewport" content="width=device-width, initial-scale=1">
```
---