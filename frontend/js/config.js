/* ============================================
   config.js — Cấu hình chung frontend
   ============================================ */
(function (global) {
  'use strict';

  // Địa chỉ Backend Spring Boot (xem backend/src/main/resources/application.yml).
  // Có thể override bằng window.APP_CONFIG_API_BASE_URL nếu cần.
  var defaultBase = 'http://localhost:8080/api/v1';

  global.APP_CONFIG = {
    // Base URL của REST API (luôn kết thúc bằng /api/v1)
    API_BASE_URL: global.APP_CONFIG_API_BASE_URL || defaultBase,

    // Khóa lưu access token trong localStorage (đồng bộ với productApi/farmApi cũ)
    TOKEN_KEY: 'nsv_token',
    USER_KEY: 'nsv_user',
    ROLE_KEY: 'nsv_role',

    // Thời gian chờ request mặc định (ms)
    REQUEST_TIMEOUT: 15000
  };
})(window);
