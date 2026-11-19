/* ============================================
   pages/login.js — Trang Đăng nhập (Public)
   - Nút hiện/ẩn mật khẩu
   - Xác thực ô trống
   - Gọi NSV.auth.login -> POST /auth/login
   - Thành công: chuyển hướng theo vai trò
   - Đã đăng nhập rồi thì không hiện lại trang này
   ============================================ */
(function () {
  'use strict';
  var auth = (window.NSV && window.NSV.auth) || {};
  var form = (window.NSV && window.NSV.form) || {};
  var ui = (window.NSV && window.NSV.ui) || {};
  var renderIcons = (window.NSV && window.NSV.renderIcons) || function () {};

  function $id(id) { return document.getElementById(id); }

  document.addEventListener('DOMContentLoaded', function () {
    // Đã đăng nhập => về đúng khu vực vai trò, không hiện lại login
    if (auth.isLoggedIn && auth.isLoggedIn() && auth.redirectAfterLogin) {
      // Chỉ chuyển khi thật sự có token; tránh vòng lặp nếu token lỗi sẽ tự đăng xuất ở bước sau
      try { auth.redirectAfterLogin(); return; } catch (e) { /* tiếp tục hiện login */ }
    }

    var identifierEl = $id('identifier');
    var passwordEl = $id('password');
    var toggleBtn = $id('togglePw');
    var formEl = $id('loginForm');
    var submitBtn = $id('loginBtn');

    if (toggleBtn && passwordEl && form.togglePassword) {
      var toggle = form.togglePassword(passwordEl, toggleBtn);
      toggleBtn.addEventListener('click', toggle);
    }

    function validate() {
      var ok = true;
      form.clearAll && form.clearAll(formEl);
      if (!identifierEl.value.trim()) { form.setError(identifierEl, 'Vui lòng nhập email hoặc số điện thoại.'); ok = false; }
      if (!passwordEl.value) { form.setError(passwordEl, 'Vui lòng nhập mật khẩu.'); ok = false; }
      return ok;
    }

    // Xoá lỗi đỏ khi người dùng bắt đầu sửa lại
    [identifierEl, passwordEl].forEach(function (el) {
      el.addEventListener('input', function () { form.clearError && form.clearError(el); });
    });

    formEl.addEventListener('submit', function (e) {
      e.preventDefault();
      if (!validate()) { form.focusFirstError && form.focusFirstError(formEl); return; }

      form.setLoading && form.setLoading(submitBtn, true, 'Đang đăng nhập...');
      auth.login(identifierEl.value.trim(), passwordEl.value)
        .then(function (res) {
          if (ui.toast) ui.toast('Đăng nhập thành công.', 'success');
          // Xoá mật khẩu khỏi bộ nhớ tạm phòng ngừa
          passwordEl.value = '';
          // Chuyển hướng theo vai trò (server trả về user.role)
          var role = (res && res.user && res.user.role) || auth.getRole();
          if (auth.redirectAfterLogin) auth.redirectAfterLogin(role);
          else window.location.href = '../buyer/home.html';
        })
        .catch(function (err) {
          var msg = (err && err.message) || 'Đăng nhập thất bại. Vui lòng thử lại.';
          // Phân loại lỗi cơ bản: nếu là lỗi sai thông tin đăng nhập -> báo vào ô identifier
          var status = err && err.status;
          if (status === 401 || status === 404) {
            form.setError(identifierEl, 'Email/số điện thoại hoặc mật khẩu không đúng.');
            form.setError(passwordEl, ' ');
            form.focusFirstError && form.focusFirstError(formEl);
          } else {
            if (ui.toast) ui.toast(msg, 'danger');
          }
        })
        .finally(function () {
          form.setLoading && form.setLoading(submitBtn, false);
        });
    });

    renderIcons(document);
  });
})();
