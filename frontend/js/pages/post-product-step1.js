/**
 * post-product-step1.js - Bước 1/3: thông tin nông sản, nguồn gốc và chất lượng.
 */
(function () {
  const NEXT_URL = "post-product-step2.html";
  const MAX_IMAGE_BYTES = 5 * 1024 * 1024;
  const FIELDS = ["name", "category", "price", "unit", "farmName", "district", "province", "certification", "batchCode"];

  const $ = (id) => document.getElementById(id);
  const form = $("stepForm");
  let imageData = "";

  /* ---------- Khu vực ảnh: Chọn ảnh / Đổi ảnh / Xóa ảnh ---------- */
  function renderImage() {
    const has = Boolean(imageData);
    if (has) $("imagePreview").src = imageData;
    $("imagePreview").hidden = !has;
    $("imagePlaceholder").hidden = has;
    $("imageRemoveBtn").hidden = !has;
    $("imageBtn").textContent = has ? "Đổi ảnh" : "Chọn ảnh";
  }

  function setImageError(msg) {
    formUi.setError("image", msg);
    $("imageBox").classList.toggle("is-invalid", Boolean(msg));
  }

  // nén ảnh về tối đa maxSide px để lưu vừa trong sessionStorage
  function shrink(file, maxSide) {
    return new Promise((resolve, reject) => {
      const url = URL.createObjectURL(file);
      const img = new Image();
      img.onload = () => {
        const ratio = Math.min(1, maxSide / Math.max(img.width, img.height));
        const canvas = document.createElement("canvas");
        canvas.width = Math.round(img.width * ratio);
        canvas.height = Math.round(img.height * ratio);
        canvas.getContext("2d").drawImage(img, 0, 0, canvas.width, canvas.height);
        URL.revokeObjectURL(url);
        resolve(canvas.toDataURL("image/jpeg", 0.8));
      };
      img.onerror = () => {
        URL.revokeObjectURL(url);
        reject(new Error("read"));
      };
      img.src = url;
    });
  }

  $("imageBtn").addEventListener("click", () => $("image").click());

  $("imageRemoveBtn").addEventListener("click", () => {
    imageData = "";
    $("image").value = "";
    setImageError("");
    renderImage();
    $("imageBtn").focus();
  });

  $("image").addEventListener("change", async (e) => {
    const file = e.target.files[0];
    e.target.value = ""; // cho phép chọn lại đúng file đó lần sau
    if (!file) return; // người dùng bấm Hủy: giữ nguyên ảnh đang có

    if (!/^image\/(jpeg|png|webp)$/.test(file.type)) {
      setImageError("Chỉ nhận ảnh JPG, PNG hoặc WebP.");
      return;
    }
    if (file.size > MAX_IMAGE_BYTES) {
      setImageError("Ảnh lớn hơn 5 MB. Chọn ảnh nhỏ hơn.");
      return;
    }
    try {
      imageData = await shrink(file, 640);
      setImageError("");
      renderImage();
    } catch (err) {
      setImageError("Không đọc được ảnh. Thử chọn ảnh khác.");
    }
  });

  /* ---------- Điền lại dữ liệu nếu người dùng quay lại bước này ---------- */
  const saved = postDraft.load();
  FIELDS.forEach((f) => {
    if (saved[f] !== undefined && saved[f] !== "") $(f).value = saved[f];
  });
  if (saved.imageData) imageData = saved.imageData;
  renderImage();

  /* ---------- Kiểm tra dữ liệu ---------- */
  function collect() {
    const v = {};
    FIELDS.forEach((f) => (v[f] = $(f).value.trim()));
    v.price = Number(v.price);
    return v;
  }

  function validate(v) {
    let ok = true;
    const bad = (id, msg) => {
      formUi.setError(id, msg);
      ok = false;
    };

    if (!v.name) bad("name", "Nhập tên sản phẩm.");
    else if (v.name.length < 3) bad("name", "Tên sản phẩm cần ít nhất 3 ký tự.");

    if (!v.category) bad("category", "Chọn nhóm sản phẩm.");

    if (!(v.price > 0)) bad("price", "Nhập giá lớn hơn 0.");
    else if (!Number.isInteger(v.price)) bad("price", "Giá phải là số nguyên (đồng).");

    if (!imageData) {
      setImageError("Thêm một ảnh sản phẩm.");
      ok = false;
    }

    if (!v.farmName) bad("farmName", "Nhập tên trang trại.");
    if (!v.district) bad("district", "Nhập quận, huyện nơi trồng.");
    if (!v.province) bad("province", "Nhập tỉnh, thành phố nơi trồng.");
    if (!v.certification) bad("certification", "Chọn chứng nhận chất lượng.");

    if (v.batchCode && !/^[A-Za-z0-9_-]{3,30}$/.test(v.batchCode)) {
      bad("batchCode", "Mã lô chỉ gồm chữ, số, dấu gạch ngang, 3 đến 30 ký tự.");
    }
    return ok;
  }

  function focusFirstError() {
    formUi.focusFirstError(form);
    // ô file bị ẩn nên không nhìn thấy focus: chuyển focus sang nút ảnh
    if (document.activeElement === $("image")) $("imageBtn").focus();
  }

  /* ---------- Gửi form: lưu nháp rồi sang bước 2 ---------- */
  form.addEventListener("submit", (e) => {
    e.preventDefault();
    formUi.clearErrors(form);
    setImageError("");

    const v = collect();
    if (!validate(v)) {
      focusFirstError();
      return;
    }

    v.imageData = imageData;
    if (!postDraft.save(v)) {
      // bộ nhớ tạm đầy, thường do ảnh: lưu lại phần còn lại, không kèm ảnh
      delete v.imageData;
      postDraft.save(v);
      setImageError("Không lưu nháp được ảnh này. Chọn ảnh nhỏ hơn rồi thử lại.");
      $("imageBtn").focus();
      return;
    }
    location.assign(NEXT_URL);
  });
})();