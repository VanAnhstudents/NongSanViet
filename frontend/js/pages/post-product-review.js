/**
 * post-product-review.js - Bước 3/3: kiểm tra trước khi đăng.
 *
 * Dữ liệu nháp do bước 1 và 2 lưu vào sessionStorage (khóa DRAFT_KEY) dạng JSON:
 * {
 *   name, category, price, unit,            // bước 1
 *   farmName, province, district,
 *   certification, batchCode,
 *   quantity, depositPercent,               // bước 2
 *   harvestDate: "2026-11-18",
 *   cutoffAt: "2026-11-17T08:00"
 * }
 * Muốn xem thử khi chưa có bước 1, 2: mở trang với ?demo=1
 */
(function () {
  const DRAFT_KEY = "nsv_post_product_draft";
  const STEP1_URL = "post-product-step1.html";
  const SUCCESS_URL = "../../index.html";

  const DEMO_DRAFT = {
    name: "Cà chua bi Đà Lạt",
    category: "Rau củ quả",
    price: 40000,
    unit: "kg",
    farmName: "Vườn An",
    district: "Đơn Dương",
    province: "Lâm Đồng",
    certification: "VietGAP",
    batchCode: "AN-181126",
    quantity: 200,
    depositPercent: 30,
    harvestDate: "2026-11-18",
    cutoffAt: "2026-11-17T08:00"
  };

  const $ = (sel) => document.querySelector(sel);

  /* ---------- Lấy dữ liệu nháp ---------- */
  function loadDraft() {
    if (new URLSearchParams(location.search).get("demo") === "1") return DEMO_DRAFT;
    try {
      const raw = sessionStorage.getItem(DRAFT_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch (e) {
      return null;
    }
  }

  /* ---------- Định dạng ---------- */
  const formatMoney = (n) => Number(n).toLocaleString("vi-VN") + "đ";

  function formatDate(iso) {
    const d = new Date(iso);
    if (isNaN(d)) return "";
    return d.toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" });
  }

  function formatDateTime(iso) {
    const d = new Date(iso);
    if (isNaN(d)) return "";
    const time = d.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit", hour12: false });
    return time + ", " + formatDate(iso);
  }

  const join = (parts, sep) => parts.filter(Boolean).join(sep);

  /* ---------- Hiển thị ---------- */
  function render(d) {
    const text = {
      name: d.name,
      categoryPrice: join([d.category, formatMoney(d.price) + " / " + d.unit], " · "),
      location: join([d.farmName, join([d.district, d.province], ", ")], " · "),
      quality: join([d.certification, d.batchCode ? "Gắn với lô " + d.batchCode : ""], " · "),
      preorder1: "Sản lượng: " + d.quantity + " " + d.unit + " · Cọc: " + d.depositPercent + "%",
      harvest: "Thu hoạch dự kiến: " + formatDate(d.harvestDate),
      cutoff: "Chốt đơn: " + formatDateTime(d.cutoffAt)
    };
    // dùng textContent để tránh chèn HTML từ dữ liệu người dùng nhập
    document.querySelectorAll("[data-bind]").forEach((el) => {
      el.textContent = text[el.dataset.bind] || "";
    });
    if (d.farmName) {
      $("#sidebarFarm").textContent = d.farmName;
      $("#farmStatus").textContent = d.farmName + " · Đã xác minh";
    }
  }

  /* ---------- Kiểm tra lần cuối trước khi gửi ---------- */
  function validate(d) {
    if (!d.name || !d.name.trim()) return "Thiếu tên sản phẩm. Quay lại bước 1 để bổ sung.";
    if (!(Number(d.price) > 0)) return "Giá phải lớn hơn 0. Quay lại bước 1 để sửa.";
    if (!(Number(d.quantity) > 0)) return "Sản lượng phải lớn hơn 0. Quay lại bước 2 để sửa.";
    if (new Date(d.cutoffAt) >= new Date(d.harvestDate)) {
      return "Giờ chốt đơn phải trước ngày thu hoạch. Quay lại bước 2 để sửa.";
    }
    return "";
  }

  /* ---------- UI helpers ---------- */
  function showError(msg) {
    const el = $("#formError");
    el.textContent = msg;
    el.hidden = !msg;
  }

  function showToast(msg) {
    const el = $("#toast");
    el.textContent = msg;
    el.hidden = false;
  }

  function setBusy(busy) {
    const btn = $("#publishBtn");
    btn.disabled = busy || !$("#confirmCheck").checked;
    btn.textContent = busy ? "Đang đăng..." : "Đăng bán sản phẩm";
    const back = $("#backBtn");
    back.setAttribute("aria-disabled", String(busy));
  }

  /* ---------- Khởi tạo ---------- */
  function init() {
    const draft = loadDraft();
    if (!draft) {
      // chưa có dữ liệu từ bước 1, 2 -> quay về đầu luồng
      location.replace(STEP1_URL);
      return;
    }
    render(draft);

    const check = $("#confirmCheck");
    const publishBtn = $("#publishBtn");

    check.addEventListener("change", () => {
      publishBtn.disabled = !check.checked;
      showError("");
    });

    publishBtn.addEventListener("click", async () => {
      showError("");
      const problem = validate(draft);
      if (problem) {
        showError(problem);
        return;
      }

      setBusy(true);
      try {
        await window.productApi.createProduct(draft);
        sessionStorage.removeItem(DRAFT_KEY);
        showToast("Đã đăng bán sản phẩm.");
        setTimeout(() => location.assign(SUCCESS_URL), 1200);
      } catch (err) {
        showError(err.message);
        setBusy(false);
      }
    });
  }

  document.addEventListener("DOMContentLoaded", init);
})();