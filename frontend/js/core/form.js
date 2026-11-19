/* ============================================
   core/form.js — Helper form chung cho trang Public/Auth
   - setError / clearError / clearAll / focusFirstError (BEM form-field)
   - togglePassword (eye / eye-off, KHÔNG emoji)
   - setLoading (đổi nhãn nút + disable, KHÔNG spinner animation)
   Phục vụ A11y: aria-invalid + aria-describedby trỏ tới dòng lỗi.
   ============================================ */
(function (global) {
  'use strict';
  var icon = (global.NSV && global.NSV.icon) || function () { return ''; };

  function fieldOf(inputEl) {
    return inputEl && inputEl.closest ? inputEl.closest('.form-field') : null;
  }

  // Đặt / xoá lỗi cho 1 ô input. message rỗng => xoá lỗi.
  function setError(inputEl, message) {
    if (!inputEl) return;
    var field = fieldOf(inputEl);
    var has = !!message;
    if (field) field.classList.toggle('form-field--error', has);
    if (field) {
      var err = field.querySelector('.form-field__error');
      if (err) {
        if (has) {
          err.innerHTML = icon('alert-circle', { size: 14, class: 'icon--sm' });
          err.appendChild(document.createTextNode(' ' + message));
          err.hidden = false;
        } else {
          err.innerHTML = '';
          err.hidden = true;
        }
      }
    }
    inputEl.setAttribute('aria-invalid', has ? 'true' : 'false');
  }

  function clearError(inputEl) { setError(inputEl, ''); }

  function clearAll(formEl) {
    if (!formEl) return;
    formEl.querySelectorAll('.form-field--error').forEach(function (f) {
      f.classList.remove('form-field--error');
      var err = f.querySelector('.form-field__error');
      if (err) { err.innerHTML = ''; err.hidden = true; }
      var inp = f.querySelector('.form-field__input, .form-field__select, .form-field__textarea');
      if (inp) inp.setAttribute('aria-invalid', 'false');
    });
  }

  function focusFirstError(formEl) {
    if (!formEl) return;
    var bad = formEl.querySelector('.form-field--error .form-field__input, ' +
      '.form-field--error .form-field__select, .form-field--error .form-field__textarea');
    if (bad && bad.focus) bad.focus();
  }

  // Khởi tạo nút hiện/ẩn mật khẩu. Trả về hàm toggle.
  function togglePassword(inputEl, btnEl) {
    if (!inputEl || !btnEl) return function () {};
    function paint() {
      var show = inputEl.type === 'text';
      btnEl.innerHTML = icon(show ? 'eye-off' : 'eye', { size: 20 });
      btnEl.setAttribute('aria-pressed', show ? 'true' : 'false');
      btnEl.setAttribute('aria-label', show ? 'Ẩn mật khẩu' : 'Hiện mật khẩu');
    }
    paint();
    return function () {
      inputEl.type = inputEl.type === 'password' ? 'text' : 'password';
      paint();
    };
  }

  // Bật/tắt trạng thái đang xử lý cho nút (đổi nhãn, disable, aria-busy). KHÔNG spinner.
  function setLoading(btnEl, loading, loadingText) {
    if (!btnEl) return;
    if (loading) {
      if (!btnEl.dataset.origLabel) {
        var label = btnEl.querySelector('.btn__label');
        btnEl.dataset.origLabel = label ? label.textContent : btnEl.textContent;
      }
      btnEl.disabled = true;
      btnEl.setAttribute('aria-busy', 'true');
      var label = btnEl.querySelector('.btn__label');
      if (label) label.textContent = loadingText || 'Đang xử lý...';
      else btnEl.textContent = loadingText || 'Đang xử lý...';
    } else {
      btnEl.disabled = false;
      btnEl.removeAttribute('aria-busy');
      if (btnEl.dataset.origLabel) {
        var label = btnEl.querySelector('.btn__label');
        if (label) label.textContent = btnEl.dataset.origLabel;
        else btnEl.textContent = btnEl.dataset.origLabel;
        delete btnEl.dataset.origLabel;
      }
    }
  }

  global.NSV = global.NSV || {};
  global.NSV.form = {
    setError: setError,
    clearError: clearError,
    clearAll: clearAll,
    focusFirstError: focusFirstError,
    togglePassword: togglePassword,
    setLoading: setLoading
  };
})(window);
