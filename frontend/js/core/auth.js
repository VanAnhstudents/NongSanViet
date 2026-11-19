/* ============================================
   core/auth.js — Quản lý phiên đăng nhập & guard vai trò
   - Lưu access token (nsv_token) + user (nsv_user) + role (nsv_role)
   - login / register / forgot / reset / fetchMe
   - requireAuth / requireRole (chuyển hướng về trang đăng nhập khi thiếu)
   ============================================ */
(function (global) {
  'use strict';

  var cfg = global.APP_CONFIG || {};
  var TOKEN_KEY = cfg.TOKEN_KEY || 'nsv_token';
  var USER_KEY = cfg.USER_KEY || 'nsv_user';
  var ROLE_KEY = cfg.ROLE_KEY || 'nsv_role';
  var api = global.NSV && global.NSV.api;

  // --- Lưu trữ phiên ---
  function saveSession(authResponse) {
    if (!authResponse || !authResponse.accessToken) return;
    api.setToken(authResponse.accessToken);
    var user = authResponse.user || null;
    var role = (user && user.role) || '';
    try {
      localStorage.setItem(USER_KEY, JSON.stringify(user));
      localStorage.setItem(ROLE_KEY, String(role));
    } catch (e) { /* ignore */ }
  }

  function getUser() {
    try { return JSON.parse(localStorage.getItem(USER_KEY) || 'null'); }
    catch (e) { return null; }
  }
  function getRole() {
    try { return localStorage.getItem(ROLE_KEY) || ''; }
    catch (e) { return ''; }
  }
  function isLoggedIn() { return !!api.getToken(); }

  function clearSession() {
    api.clearToken();
    try { localStorage.removeItem(USER_KEY); localStorage.removeItem(ROLE_KEY); } catch (e) {}
  }

  // --- API: đăng nhập / đăng ký / đặt lại mật khẩu ---
  function login(identifier, password) {
    return api.post('/auth/login', { identifier: identifier, password: password }).then(function (res) {
      saveSession(res);
      return res;
    });
  }

  function register(payload) {
    return api.post('/auth/register', payload).then(function (res) {
      saveSession(res);
      return res;
    });
  }

  function forgotPassword(phone) {
    return api.post('/auth/forgot-password', { phone: phone });
  }

  function resetPassword(phone, otp, newPassword) {
    return api.post('/auth/reset-password', { phone: phone, otp: otp, newPassword: newPassword });
  }

  function fetchMe() {
    return api.get('/auth/me').then(function (res) {
      if (res && res.user) {
        try {
          localStorage.setItem(USER_KEY, JSON.stringify(res.user));
          localStorage.setItem(ROLE_KEY, String(res.user.role || ''));
        } catch (e) {}
      }
      return res;
    });
  }

  function logout() {
    clearSession();
  }

  // --- Guards ---
  // loginUrl: đường dẫn tương đối tới trang đăng nhập so với trang hiện tại.
  function requireAuth(loginUrl) {
    if (isLoggedIn()) return true;
    var url = loginUrl || resolveLoginUrl();
    window.location.href = url;
    return false;
  }

  function requireRole(role, loginUrl) {
    if (!isLoggedIn()) { window.location.href = loginUrl || resolveLoginUrl(); return false; }
    if (getRole() !== role) {
      // Sai vai trò → đẩy về trang chủ đúng vai trò hoặc đăng nhập
      redirectByRole(getRole(), loginUrl);
      return false;
    }
    return true;
  }

  // Trang đăng nhập luôn ở pages/auth/login.html; tính tương đối theo độ sâu thư mục.
  // Buyer pages nằm sâu 2 cấp (pages/buyer/x.html) → '../../pages/auth/login.html'
  // Auth pages nằm sâu 2 cấp (pages/auth/x.html) → 'login.html'
  function resolveLoginUrl() {
    var path = location.pathname.replace(/\\/g, '/');
    var depth = 0;
    var parts = path.split('/').filter(Boolean);
    // đếm số thư mục sau domain tới file
    var fileName = parts[parts.length - 1];
    depth = parts.length - 1; // số cấp thư mục (thường 3: learning/NongSan/frontend? thực tế host chỉ serve frontend/)
    // đơn giản: nếu đang trong /buyer hoặc /admin hoặc /farmer → 2 cấp; nếu trong /auth → 'login.html'
    if (path.indexOf('/auth/') !== -1) return 'login.html';
    return '../../pages/auth/login.html';
  }

  function redirectByRole(role, fallback) {
    var target;
    if (role === 'FARMER') target = '../farmer/dashboard.html';
    else if (role === 'ADMIN') target = '../admin/dashboard.html';
    else if (role === 'BUYER') target = 'home.html';
    window.location.href = target || fallback || resolveLoginUrl();
  }

  // Chuyển hướng sau đăng nhập theo vai trò
  function redirectAfterLogin(role) {
    role = role || getRole();
    if (role === 'FARMER') window.location.href = '../farmer/dashboard.html';
    else if (role === 'ADMIN') window.location.href = '../admin/dashboard.html';
    else window.location.href = '../buyer/home.html';
  }

  global.NSV = global.NSV || {};
  global.NSV.auth = {
    saveSession: saveSession,
    getUser: getUser,
    getRole: getRole,
    isLoggedIn: isLoggedIn,
    login: login,
    register: register,
    forgotPassword: forgotPassword,
    resetPassword: resetPassword,
    fetchMe: fetchMe,
    logout: logout,
    clearSession: clearSession,
    requireAuth: requireAuth,
    requireRole: requireRole,
    redirectAfterLogin: redirectAfterLogin,
    redirectByRole: redirectByRole
  };
})(window);
