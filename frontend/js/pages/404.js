/* ============================================
   pages/404.js — Trang lỗi hệ thống (Public)
   - Đọc ?type= để hiển thị 404/403/500/... cho phù hợp
   - "Về trang chủ": nếu đã đăng nhập -> về đúng khu vực vai trò
   ============================================ */
(function () {
  'use strict';
  var auth = (window.NSV && window.NSV.auth) || {};
  var renderIcons = (window.NSV && window.NSV.renderIcons) || function () {};

  function getQuery(name) {
    try { return new URL(window.location.href).searchParams.get(name); }
    catch (e) { return null; }
  }

  var MESSAGES = {
    '404': { title: 'Không tìm thấy trang', desc: 'Trang bạn tìm có thể đã bị di chuyển hoặc không còn tồn tại. Hãy quay về trang chủ để tiếp tục.' },
    '403': { title: 'Không có quyền truy cập', desc: 'Bạn không đủ quyền để xem trang này. Nếu đây là nhầm lẫn, hãy đăng nhập bằng tài khoản phù hợp.' },
    '500': { title: 'Lỗi máy chủ', desc: 'Hệ thống đang gặp sự cố tạm thời. Vui lòng thử lại sau ít phút.' },
    '502': { title: 'Máy chủ đang bảo trì', desc: 'Hệ thống tạm thời không phản hồi. Vui lòng quay lại sau.' }
  };

  document.addEventListener('DOMContentLoaded', function () {
    var type = getQuery('type');
    var info = MESSAGES[type] || MESSAGES['404'];
    var codeEl = document.getElementById('errCode');
    var titleEl = document.getElementById('errTitle');
    var descEl = document.getElementById('errDesc');
    codeEl.textContent = type || '404';
    titleEl.textContent = info.title;
    descEl.textContent = info.desc;
    document.title = info.title + ' — Nông Sản Việt';

    // Nút "Về trang chủ": nếu đã đăng nhập -> đúng khu vực vai trò
    var homeBtn = document.getElementById('homeBtn');
    if (homeBtn && auth.isLoggedIn && auth.isLoggedIn()) {
      var role = auth.getRole && auth.getRole();
      var target = null;
      if (role === 'FARMER') target = '../farmer/dashboard.html';
      else if (role === 'ADMIN') target = '../admin/dashboard.html';
      else if (role === 'BUYER') target = '../buyer/home.html';
      if (target) homeBtn.setAttribute('href', target);
    }

    renderIcons(document);
  });
})();
