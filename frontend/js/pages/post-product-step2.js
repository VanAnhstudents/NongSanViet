/**
 * post-product-step2.js - Bước 2/3: đặt trước theo mùa vụ.
 */
(function () {
  const PREV_URL = "post-product-step1.html";
  const NEXT_URL = "post-product-review.html";

  const $ = (id) => document.getElementById(id);
  const form = $("stepForm");

  /* ---------- Phải có dữ liệu bước 1 mới vào được bước 2 ---------- */
  const draft = postDraft.load();
  if (!draft.name) {
    location.replace(PREV_URL);
    return;
  }

  $("unitLabel").textContent = draft.unit || "kg";
  $("productSummary").textContent =
    draft.name + " · " + Number(draft.price).toLocaleString("vi-VN") + "đ / " + (draft.unit || "kg");

  /* ---------- Giới hạn ngày chọn ---------- */
  const pad = (n) => String(n).padStart(2, "0");
  const toDateStr = (d) => d.getFullYear() + "-" + pad(d.getMonth() + 1) + "-" + pad(d.getDate());
  const toLocalDateTimeStr = (d) =>
    toDateStr(d) + "T" + pad(d.getHours()) + ":" + pad(d.getMinutes());

  const now = new Date();
  const tomorrow = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1);
  $("harvestDate").min = toDateStr(tomorrow);
  $("cutoffAt").min = toLocalDateTimeStr(now);

  // khi chọn ngày thu hoạch, giới hạn giờ chốt đơn phải trước ngày đó
  $("harvestDate").addEventListener("change", () => {
    const h = $("harvestDate").value;
    $("cutoffAt").max = h ? h + "T00:00" : "";
  });

  /* ---------- Điền lại dữ liệu nếu quay lại bước này ---------- */
  ["quantity", "depositPercent", "harvestDate", "cutoffAt"].forEach((f) => {
    if (draft[f] !== undefined && draft[f] !== "") $(f).value = draft[f];
  });
  $("harvestDate").dispatchEvent(new Event("change"));

  /* ---------- Kiểm tra ---------- */
  function validate(v) {
    let ok = true;
    const bad = (id, msg) => {
      formUi.setError(id, msg);
      ok = false;
    };

    if (!(v.quantity > 0)) bad("quantity", "Nhập sản lượng lớn hơn 0.");

    if (!Number.isInteger(v.depositPercent) || v.depositPercent < 10 || v.depositPercent > 100) {
      bad("depositPercent", "Tiền cọc là số nguyên từ 10 đến 100.");
    }

    if (!v.harvestDate) bad("harvestDate", "Chọn ngày thu hoạch dự kiến.");
    else if (v.harvestDate < toDateStr(tomorrow)) bad("harvestDate", "Ngày thu hoạch phải từ ngày mai trở đi.");

    if (!v.cutoffAt) bad("cutoffAt", "Chọn giờ chốt đơn.");
    else if (new Date(v.cutoffAt) <= new Date()) bad("cutoffAt", "Giờ chốt đơn phải ở tương lai.");
    else if (v.harvestDate && v.cutoffAt.slice(0, 10) >= v.harvestDate) {
      bad("cutoffAt", "Giờ chốt đơn phải trước ngày thu hoạch.");
    }
    return ok;
  }

  /* ---------- Gửi form: lưu nháp rồi sang bước 3 ---------- */
  form.addEventListener("submit", (e) => {
    e.preventDefault();
    formUi.clearErrors(form);

    const v = {
      quantity: Number($("quantity").value),
      depositPercent: Number($("depositPercent").value),
      harvestDate: $("harvestDate").value,
      cutoffAt: $("cutoffAt").value
    };

    if (!validate(v)) {
      formUi.focusFirstError(form);
      return;
    }
    postDraft.save(v);
    location.assign(NEXT_URL);
  });
})();