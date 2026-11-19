/* ============================================
   pages/register.js — Trang Đăng ký (Public)
   - Chọn vai trò Farmer/Buyer (mặc định BUYER)
   - Xác thực: họ tên, SĐT, email, mật khẩu, xác nhận, đồng ý điều khoản
   - Gọi NSV.auth.register -> POST /auth/register
   - Thành công: chuyển hướng theo vai trò
   ============================================ */
(function () {
  'use strict';
  var auth = (window.NSV && window.NSV.auth) || {};
  var form = (window.NSV && window.NSV.form) || {};
  var ui = (window.NSV && window.NSV.ui) || {};
  var renderIcons = (window.NSV && window.NSV.renderIcons) || function () {};

  var PHONE_RE = /^(0|\+84)\d{9}$/;
  var EMAIL_RE = /.+@.+\..+/;
  var PW_RE = /^(?=.*[A-Za-z])(?=.*\d).+$/;

  function $id(id) { return document.getElementById(id); }

  document.addEventListener('DOMContentLoaded', function () {
    var roleChooser = $id('roleChooser');
    var fullNameEl = $id('fullName');
    var phoneEl = $id('phone');
    var emailEl = $id('email');
    var passwordEl = $id('password');
    var confirmEl = $id('confirmPassword');
    var termsEl = $id('agreeTerms');
    var formEl = $id('registerForm');
    var submitBtn = $id('registerBtn');

    var selectedRole = 'BUYER';

    // ---- Chọn vai trò ----
    roleChooser.addEventListener('click', function (e) {
      var opt = e.target.closest('.role-chooser__option');
      if (!opt) return;
      selectedRole = opt.getAttribute('data-role');
      roleChooser.querySelectorAll('.role-chooser__option').forEach(function (o) {
        var on = o === opt;
        o.classList.toggle('is-active', on);
        o.setAttribute('aria-checked', on ? 'true' : 'false');
      });
    });

    // ---- Nút hiện/ẩn mật khẩu ----
    if (form.togglePassword) {
      var t1 = form.togglePassword(passwordEl, $id('togglePw'));
      $id('togglePw').addEventListener('click', t1);
      var t2 = form.togglePassword(confirmEl, $id('togglePw2'));
      $id('togglePw2').addEventListener('click', t2);
    }

    // ---- Xác thực ----
    function validate() {
      form.clearAll && form.clearAll(formEl);
      var ok = true;

      var fullName = fullNameEl.value.trim();
      if (!fullName) { form.setError(fullNameEl, 'Vui lòng nhập họ và tên.'); ok = false; }
      else if (fullName.length > 100) { form.setError(fullNameEl, 'Họ tên tối đa 100 ký tự.'); ok = false; }

      var phone = phoneEl.value.trim();
      if (!phone) { form.setError(phoneEl, 'Vui lòng nhập số điện thoại.'); ok = false; }
      else if (!PHONE_RE.test(phone)) { form.setError(phoneEl, 'Số điện thoại không hợp lệ (vd: 0912345678).'); ok = false; }

      var email = emailEl.value.trim();
      if (email && !EMAIL_RE.test(email)) { form.setError(emailEl, 'Email không hợp lệ.'); ok = false; }

      var pw = passwordEl.value;
      if (!pw) { form.setError(passwordEl, 'Vui lòng nhập mật khẩu.'); ok = false; }
      else if (pw.length < 8 || pw.length > 72) { form.setError(passwordEl, 'Mật khẩu từ 8 đến 72 ký tự.'); ok = false; }
      else if (!PW_RE.test(pw)) { form.setError(passwordEl, 'Mật khẩu phải có cả chữ và số.'); ok = false; }

      var cf = confirmEl.value;
      if (!cf) { form.setError(confirmEl, 'Vui lòng nhập lại mật khẩu.'); ok = false; }
      else if (cf !== pw) { form.setError(confirmEl, 'Mật khẩu nhập lại không khớp.'); ok = false; }

      if (!termsEl.checked) {
        var errTerms = $id('errTerms');
        errTerms.innerHTML = (window.NSV.icon ? window.NSV.icon('alert-circle', { size: 14, class: 'icon--sm' }) : '');
        errTerms.appendChild(document.createTextNode(' Vui lòng đồng ý với điều khoản để tiếp tục.'));
        errTerms.hidden = false;
        ok = false;
      } else {
        $id('errTerms').hidden = true;
      }

      return ok;
    }

    [fullNameEl, phoneEl, emailEl, passwordEl, confirmEl].forEach(function (el) {
      el.addEventListener('input', function () { form.clearError && form.clearError(el); });
    });
    termsEl.addEventListener('change', function () { $id('errTerms').hidden = true; });

    // ---- Gửi form ----
    formEl.addEventListener('submit', function (e) {
      e.preventDefault();
      if (!validate()) { form.focusFirstError && form.focusFirstError(formEl); return; }

      var payload = {
        fullName: fullNameEl.value.trim(),
        phone: phoneEl.value.trim(),
        email: emailEl.value.trim() || null,
        password: passwordEl.value,
        role: selectedRole
      };

      form.setLoading && form.setLoading(submitBtn, true, 'Đang tạo tài khoản...');
      auth.register(payload)
        .then(function (res) {
          if (ui.toast) ui.toast('Tạo tài khoản thành công.', 'success');
          passwordEl.value = '';
          confirmEl.value = '';
          var role = (res && res.user && res.user.role) || selectedRole;
          if (auth.redirectAfterLogin) auth.redirectAfterLogin(role);
          else window.location.href = '../buyer/home.html';
        })
        .catch(function (err) {
          var msg = (err && err.message) || 'Đăng ký thất bại. Vui lòng thử lại.';
          // Ánh xạ lỗi nghiệp vụ vào ô tương ứng nếu có thể
          var lower = (msg || '').toLowerCase();
          if (lower.indexOf('email') !== -1) { form.setError(emailEl, msg); form.focusFirstError && form.focusFirstError(formEl); }
          else if (lower.indexOf('điện thoại') !== -1 || lower.indexOf('phone') !== -1) {
            form.setError(phoneEl, msg); form.focusFirstError && form.focusFirstError(formEl);
          } else if (ui.toast) {
            ui.toast(msg, 'danger');
          }
        })
        .finally(function () {
          form.setLoading && form.setLoading(submitBtn, false);
        });
    });

    renderIcons(document);
  });
})();
