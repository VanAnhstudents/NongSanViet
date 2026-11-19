/* ============================================
   pages/forgot-password.js — Quên mật khẩu (Bước 1/2)
   - Xác thực số điện thoại VN
   - Gọi NSV.auth.forgotPassword -> POST /auth/forgot-password
   - Thành công: sang reset-password.html kèm số điện thoại
   ============================================ */
(function () {
  'use strict';
  var auth = (window.NSV && window.NSV.auth) || {};
  var form = (window.NSV && window.NSV.form) || {};
  var ui = (window.NSV && window.NSV.ui) || {};
  var renderIcons = (window.NSV && window.NSV.renderIcons) || function () {};

  var PHONE_RE = /^(0|\+84)\d{9}$/;

  function $id(id) { return document.getElementById(id); }

  document.addEventListener('DOMContentLoaded', function () {
    var phoneEl = $id('phone');
    var formEl = $id('forgotForm');
    var submitBtn = $id('submitBtn');

    phoneEl.addEventListener('input', function () { form.clearError && form.clearError(phoneEl); });

    formEl.addEventListener('submit', function (e) {
      e.preventDefault();
      form.clearAll && form.clearAll(formEl);

      var phone = phoneEl.value.trim();
      if (!phone) { form.setError(phoneEl, 'Vui lòng nhập số điện thoại.'); form.focusFirstError && form.focusFirstError(formEl); return; }
      if (!PHONE_RE.test(phone)) { form.setError(phoneEl, 'Số điện thoại không hợp lệ (vd: 0912345678).'); form.focusFirstError && form.focusFirstError(formEl); return; }

      form.setLoading && form.setLoading(submitBtn, true, 'Đang gửi mã...');
      auth.forgotPassword(phone)
        .then(function () {
          if (ui.toast) ui.toast('Mã xác thực đã được gửi đến số điện thoại (xem trong log backend khi mô phỏng).', 'info');
          window.location.href = 'reset-password.html?phone=' + encodeURIComponent(phone);
        })
        .catch(function (err) {
          var msg = (err && err.message) || 'Không gửi được mã. Vui lòng thử lại.';
          form.setError(phoneEl, msg);
          form.focusFirstError && form.focusFirstError(formEl);
        })
        .finally(function () {
          form.setLoading && form.setLoading(submitBtn, false);
        });
    });

    renderIcons(document);
  });
})();
