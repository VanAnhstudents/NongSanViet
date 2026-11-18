/**
 * productApi.js - gọi API sản phẩm của backend.
 * TODO: hỏi @dinhvu endpoint + format thật (JSON hay multipart khi có ảnh) rồi sửa ở đây.
 */
(function (global) {
  const BASE_URL =
    (global.APP_CONFIG && global.APP_CONFIG.API_BASE_URL) || "http://localhost:8080";

  async function request(path, options = {}) {
    const token = localStorage.getItem("token"); // sửa theo cách nhóm lưu đăng nhập
    const headers = Object.assign({ "Content-Type": "application/json" }, options.headers);
    if (token) headers.Authorization = "Bearer " + token;

    let res;
    try {
      res = await fetch(BASE_URL + path, Object.assign({}, options, { headers }));
    } catch (e) {
      throw new Error("Không kết nối được máy chủ. Kiểm tra mạng và thử lại.");
    }

    const body = await res.json().catch(() => null);
    if (!res.ok) {
      throw new Error((body && body.message) || "Đăng bán thất bại (mã " + res.status + ").");
    }
    return body;
  }

  /** Tạo sản phẩm mới. `draft` là dữ liệu gom từ bước 1 và 2. */
  function createProduct(draft) {
    return request("/api/products", { method: "POST", body: JSON.stringify(draft) });
  }

  global.productApi = { createProduct };
})(window);