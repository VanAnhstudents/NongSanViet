/**
 * postProductDraft.js - dùng chung cho 3 bước đăng bán.
 * Dữ liệu nháp lưu trong sessionStorage, mất khi đóng tab.
 */
(function (global) {
  const KEY = "nsv_post_product_draft";

  const postDraft = {
    KEY: KEY,
    load() {
      try {
        return JSON.parse(sessionStorage.getItem(KEY)) || {};
      } catch (e) {
        return {};
      }
    },
    /** Gộp `patch` vào bản nháp hiện có. Trả về false nếu không lưu được. */
    save(patch) {
      const data = Object.assign(postDraft.load(), patch);
      try {
        sessionStorage.setItem(KEY, JSON.stringify(data));
        return true;
      } catch (e) {
        return false; // thường do ảnh quá lớn, vượt dung lượng sessionStorage
      }
    },
    clear() {
      sessionStorage.removeItem(KEY);
    }
  };

  /** Hiện hoặc xóa thông báo lỗi dưới ô có id; <p id="{id}-error"> phải có trong HTML. */
  function setError(id, msg) {
    const input = document.getElementById(id);
    const err = document.getElementById(id + "-error");
    if (!input || !err) return;
    err.textContent = msg || "";
    err.hidden = !msg;
    input.setAttribute("aria-invalid", msg ? "true" : "false");
    if (msg) input.setAttribute("aria-describedby", id + "-error");
  }

  function clearErrors(form) {
    form.querySelectorAll("[aria-invalid]").forEach((el) => setError(el.id, ""));
  }

  function focusFirstError(form) {
    const el = form.querySelector('[aria-invalid="true"]');
    if (el) el.focus();
  }

  global.postDraft = postDraft;
  global.formUi = { setError: setError, clearErrors: clearErrors, focusFirstError: focusFirstError };
})(window);