/* ============================================
   pages/reset-password.js — Đặt lại mật khẩu (Bước 2/2)
   - Đọc số điện thoại từ query (?phone=)
   - 6 ô OTP: tự chuyển ô, hỗ trợ Backspace
   - Xác thực OTP (6 số) + mật khẩu mới (8-72, chữ&số) + xác nhận
   - Gọi NSV.auth.resetPassword -> POST /auth/reset-password
   - Thành công: về trang đăng nhập
   ============================================ */
(function () {
  'use strict';
  var auth = (window.NSV && window.NSV.auth) || {};
  var form = (window.NSV && window.NSV.form) || {};
  var ui = (window.NSV && window.NSV.ui) || {};
  var renderIcons = (window.NSV && window.NSV.renderIcons) || function () {};

  var PHONE_RE = /^(0|\+84)\d{9}$/;
  var PW_RE = /^(?=.*[A-Za-z])(?=.*\d).+$/;

  function $id(id) { return document.getElementById(id); }
  function getQuery(name) {
    try { return new URL(window.location.href).searchParams.get(name); }
    catch (e) { return null; }
  }
  // Che một phần số điện thoại: 0912***678
  function maskPhone(p) {
    if (!p) return '—';
    var s = String(p);
    if (s.length < 6) return s;
    return s.slice(0, s.length - 3).replace(/\d(?=\d{2})/g, '*') + s.slice(-3);
  }

  document.addEventListener('DOMContentLoaded', function () {
    var phone = getQuery('phone') || '';
    var phoneDisplay = $id('phoneDisplay');
    phoneDisplay.textContent = maskPhone(phone);

    var otpInputs = Array.prototype.slice.call(document.querySelectorAll('#otpInputs input'));
    var passwordEl = $id('password');
    var confirmEl = $id('confirmPassword');
    var formEl = $id('resetForm');
    var submitBtn = $id('submitBtn');

    // Nếu không có số điện thoại hợp lệ => quay về bước 1
    if (!phone || !PHONE_RE.test(phone)) {
      if (ui.toast) ui.toast('Vui lòng nhập lại số điện thoại ở bước trước.', 'warning');
      setTimeout(function () { window.location.href = 'forgot-password.html'; }, 600);
      return;
    }

    // ---- OTP: chỉ nhận số, tự chuyển ô, Backspace lùi ----
    otpInputs.forEach(function (inp, idx) {
      inp.addEventListener('input', function () {
        inp.value = inp.value.replace(/\D/g, '').slice(0, 1);
        if (inp.value && idx < otpInputs.length - 1) otpInputs[idx + 1].focus();
        clearOtpError();
      });
      inp.addEventListener('keydown', function (e) {
        if (e.key === 'Backspace' && !inp.value && idx > 0) otpInputs[idx - 1].focus();
      });
      inp.addEventListener('paste', function (e) {
        e.preventDefault();
        var text = (e.clipboardData || window.clipboardData).getData('text') || '';
        var digits = text.replace(/\D/g, '').slice(0, otpInputs.length);
        digits.split('').forEach(function (d, i) { if (otpInputs[i]) otpInputs[i].value = d; });
        var last = Math.min(digits.length, otpInputs.length - 1);
        if (otpInputs[last]) otpInputs[last].focus();
        clearOtpError();
      });
    });

    function clearOtpError() {
      var field = $id('fieldOtp');
      field.classList.remove('form-field--error');
      otpInputs.forEach(function (i) { i.classList.remove('is-invalid'); i.setAttribute('aria-invalid', 'false'); });
      $id('errOtp').hidden = true;
    }
    function setOtpError(message) {
      $id('fieldOtp').classList.add('form-field--error');
      otpInputs.forEach(function (i) { i.classList.add('is-invalid'); i.setAttribute('aria-invalid', 'true'); });
      var err = $id('errOtp');
      err.innerHTML = (window.NSV.icon ? window.NSV.icon('alert-circle', { size: 14, class: 'icon--sm' }) : '');
      err.appendChild(document.createTextNode(' ' + message));
      err.hidden = false;
    }
    function otpValue() { return otpInputs.map(function (i) { return i.value; }).join(''); }

    // ---- Nút hiện/ẩn mật khẩu ----
    if (form.togglePassword) {
      var t1 = form.togglePassword(passwordEl, $id('togglePw'));
      $id('togglePw').addEventListener('click', t1);
      var t2 = form.togglePassword(confirmEl, $id('togglePw2'));
      $id('togglePw2').addEventListener('click', t2);
    }

    // ---- Xác thực ----
    [passwordEl, confirmEl].forEach(function (el) {
      el.addEventListener('input', function () { form.clearError && form.clearError(el); });
    });

    function validate() {
      form.clearAll && form.clearAll(formEl);
      clearOtpError();
      var ok = true;

      var otp = otpValue();
      if (otp.length !== 6 || !/^\d{6}$/.test(otp)) { setOtpError('Vui lòng nhập đủ 6 chữ số.'); ok = false; }

      var pw = passwordEl.value;
      if (!pw) { form.setError(passwordEl, 'Vui lòng nhập mật khẩu mới.'); ok = false; }
      else if (pw.length < 8 || pw.length > 72) { form.setError(passwordEl, 'Mật khẩu từ 8 đến 72 ký tự.'); ok = false; }
      else if (!PW_RE.test(pw)) { form.setError(passwordEl, 'Mật khẩu phải có cả chữ và số.'); ok = false; }

      var cf = confirmEl.value;
      if (!cf) { form.setError(confirmEl, 'Vui lòng nhập lại mật khẩu.'); ok = false; }
      else if (cf !== pw) { form.setError(confirmEl, 'Mật khẩu nhập lại không khớp.'); ok = false; }
      return ok;
    }

    formEl.addEventListener('submit', function (e) {
      e.preventDefault();
      if (!validate()) {
        var firstBad = formEl.querySelector('.form-field--error .form-field__input, #otpInputs input.is-invalid');
        if (firstBad) firstBad.focus();
        return;
      }

      form.setLoading && form.setLoading(submitBtn, true, 'Đang đặt lại mật khẩu...');
      auth.resetPassword(phone, otpValue(), passwordEl.value)
        .then(function () {
          if (ui.toast) ui.toast('Đặt lại mật khẩu thành công. Hãy đăng nhập bằng mật khẩu mới.', 'success');
          passwordEl.value = '';
          confirmEl.value = '';
          otpInputs.forEach(function (i) { i.value = ''; });
          setTimeout(function () { window.location.href = 'login.html'; }, 900);
        })
        .catch(function (err) {
          var msg = (err && err.message) || 'Không đặt lại được mật khẩu. Vui lòng thử lại.';
          var lower = (msg || '').toLowerCase();
          if (lower.indexOf('otp') !== -1 || lower.indexOf('mã') !== -1 || lower.indexOf('hết hạn') !== -1) {
            setOtpError(msg);
            otpInputs[0].focus();
          } else if (ui.toast) {
            ui.toast(msg, 'danger');
          }
        })
        .finally(function () {
          form.setLoading && form.setLoading(submitBtn, false);
        });
    });

    renderIcons(document);
    // Tự focus ô OTP đầu tiên
    setTimeout(function () { otpInputs[0] && otpInputs[0].focus(); }, 100);
  });
})();
