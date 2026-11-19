/**
 * productApi.js - gọi API sản phẩm của backend.
 *
 * Endpoint: POST /api/v1/farmer/batches  (FarmerBatchController)
 * Body: BatchCreateRequest { productId, totalQuantity, unitPrice, harvestDate }
 *
 * Lưu ý P1.3: màn hình đăng nhập (01) là mock, không gọi /api/v1/auth/login thật
 * nên token chưa được lưu vào localStorage. Request sẽ nhận 401 cho đến khi
 * login thật được tích hợp. Khi tích hợp, lưu token bằng:
 *   localStorage.setItem('nsv_token', <giá trị token từ AuthResponse>)
 *
 * Lưu ý: `productId` trong BatchCreateRequest là bắt buộc — Farmer phải tạo
 * Product trước (qua endpoint riêng chưa có trong SPA), sau đó mới tạo lô.
 * Hiện tại draft.productId cần được điền trước khi gọi API này.
 */
(function (global) {
  const BASE_URL =
    (global.APP_CONFIG && global.APP_CONFIG.API_BASE_URL) || "http://localhost:8080";

  async function request(path, options = {}) {
    const token = localStorage.getItem("nsv_token"); // key đồng nhất với login flow
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

  /**
   * Tạo lô sản phẩm mới. `draft` là dữ liệu gom từ bước 1 và 2.
   * Map sang BatchCreateRequest:
   *   productId     <- draft.productId  (bắt buộc, phải có trước)
   *   totalQuantity <- draft.quantity
   *   unitPrice     <- draft.price
   *   harvestDate   <- draft.harvestDate  (định dạng yyyy-MM-dd)
   */
  function createProduct(draft) {
    const payload = {
      productId: draft.productId,       // cần điền từ luồng chọn/tạo sản phẩm
      totalQuantity: draft.quantity,
      unitPrice: draft.price,
      harvestDate: draft.harvestDate    // "yyyy-MM-dd" — đã đúng từ bước 2
    };
    return request("/api/v1/farmer/batches", { method: "POST", body: JSON.stringify(payload) });
  }

  global.productApi = { createProduct };
})(window);
