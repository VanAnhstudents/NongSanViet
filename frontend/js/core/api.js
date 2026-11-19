/* ============================================
   core/api.js — Client HTTP tập trung
   - Tự gắn Authorization: Bearer <token>
   - Parse JSON, bắt lỗi mạng, lỗi nghiệp vụ (BusinessException)
   - Trả về data hoặc ném Error có .status và .message tiếng Việt
   ============================================ */
(function (global) {
  'use strict';

  var TOKEN_KEY = (global.APP_CONFIG && global.APP_CONFIG.TOKEN_KEY) || 'nsv_token';

  function getToken() {
    try { return localStorage.getItem(TOKEN_KEY) || ''; } catch (e) { return ''; }
  }
  function setToken(token) {
    try {
      if (token) { localStorage.setItem(TOKEN_KEY, token); }
      else { localStorage.removeItem(TOKEN_KEY); }
    } catch (e) { /* ignore quota */ }
  }
  function clearToken() { setToken(null); }

  function withTimeout(promise, ms) {
    return Promise.race([
      promise,
      new Promise(function (_, reject) {
        setTimeout(function () {
          reject(new ApiError(0, 'REQUEST_TIMEOUT', 'Yêu cầu quá thời gian chờ. Vui lòng thử lại.'));
        }, ms);
      })
    ]);
  }

  // Lớp lỗi chuẩn có status + code + message tiếng Việt
  function ApiError(status, code, message) {
    this.name = 'ApiError';
    this.status = status || 0;
    this.code = code || '';
    this.message = message || 'Lỗi không xác định';
    this.stack = (new Error(message)).stack;
  }
  ApiError.prototype = Object.create(Error.prototype);

  // Đọc message lỗi từ response JSON của BusinessException (Spring)
  function readErrorBody(res, fallback) {
    return res.json().then(function (body) {
      var code = '';
      var msg = fallback;
      if (body) {
        code = body.error || body.code || body.errorCode || '';
        if (Array.isArray(body.fieldErrors) && body.fieldErrors.length) {
          msg = body.fieldErrors.map(function (e) { return e.message || e; }).join('; ');
        } else if (Array.isArray(body.errors) && body.errors.length) {
          msg = body.errors.map(function (e) {
            return (e && (e.defaultMessage || e.message)) || String(e);
          }).join('; ');
        } else {
          msg = body.message || body.error || body.detail || fallback;
        }
      }
      return { code: code, message: msg };
    }).catch(function () { return { code: '', message: fallback }; });
  }

  // Trả về dữ liệu JSON (giả định backend luôn trả JSON object).
  // Nếu 204 / body rỗng → trả null.
  function parseJson(res) {
    if (res.status === 204) return null;
    var ct = res.headers.get('content-type') || '';
    if (ct.indexOf('application/json') === -1) return res.text().then(function () { return null; });
    return res.json().catch(function () { return null; });
  }

  /**
   * request(path, options)
   *   path: đường dẫn bắt đầu bằng '/', ví dụ '/products'
   *   options.method: GET/POST/PUT/PATCH/DELETE (mặc định GET)
   *   options.body: object → tự JSON.stringify
   *   options.formData: FormData → gửi trực tiếp (không set Content-Type)
   *   options.auth: true (mặc định) → gắn token nếu có
   *   options.headers: gộp thêm
   *   options.query: object → build query string
   *   options.rawResponse: true → trả về Response object gốc (dùng cho blob)
   */
  function request(path, options) {
    options = options || {};
    var method = (options.method || 'GET').toUpperCase();
    var headers = Object.assign({}, options.headers || {});

    var url = (global.APP_CONFIG.API_BASE_URL || '').replace(/\/$/, '') + path;
    if (options.query) {
      var qs = buildQuery(options.query);
      if (qs) url += (url.indexOf('?') === -1 ? '?' : '&') + qs;
    }

    var init = { method: method, headers: headers, credentials: 'same-origin' };

    if (options.formData instanceof FormData) {
      init.body = options.formData;
      // không set Content-Type để trình duyệt tự đặt boundary
    } else if (options.body !== undefined && options.body !== null) {
      headers['Content-Type'] = headers['Content-Type'] || 'application/json';
      init.headers = headers;
      init.body = typeof options.body === 'string' ? options.body : JSON.stringify(options.body);
    }

    if (options.auth !== false) {
      var token = getToken();
      if (token) headers['Authorization'] = 'Bearer ' + token;
    }
    init.headers = headers;

    return withTimeout(fetch(url, init), global.APP_CONFIG.REQUEST_TIMEOUT || 15000)
      .then(function (res) {
        if (options.rawResponse) return res;
        if (res.ok) return parseJson(res);
        // lỗi → cố gắng đọc body
        return readErrorBody(res, 'Yêu cầu thất bại (mã ' + res.status + ').').then(function (info) {
          throw new ApiError(res.status, info.code, info.message);
        });
      })
      .catch(function (err) {
        if (err instanceof ApiError) throw err;
        // lỗi mạng (fetch reject)
        throw new ApiError(0, 'NETWORK_ERROR', 'Không kết nối được máy chủ. Kiểm tra mạng và thử lại.');
      });
  }

  function buildQuery(obj) {
    if (!obj) return '';
    var parts = [];
    Object.keys(obj).forEach(function (k) {
      var v = obj[k];
      if (v === undefined || v === null || v === '') return;
      if (Array.isArray(v)) {
        v.forEach(function (item) { parts.push(encodeURIComponent(k) + '=' + encodeURIComponent(item)); });
      } else {
        parts.push(encodeURIComponent(k) + '=' + encodeURIComponent(v));
      }
    });
    return parts.join('&');
  }

  // Helper tiện lợi
  var api = {
    get: function (path, query, opts) { return request(path, Object.assign({}, opts, { method: 'GET', query: query })); },
    post: function (path, body, opts) { return request(path, Object.assign({}, opts, { method: 'POST', body: body })); },
    put: function (path, body, opts) { return request(path, Object.assign({}, opts, { method: 'PUT', body: body })); },
    patch: function (path, body, opts) { return request(path, Object.assign({}, opts, { method: 'PATCH', body: body })); },
    del: function (path, opts) { return request(path, Object.assign({}, opts, { method: 'DELETE' })); },
    request: request,
    getToken: getToken,
    setToken: setToken,
    clearToken: clearToken,
    ApiError: ApiError
  };

  global.NSV = global.NSV || {};
  global.NSV.api = api;
  global.NSV.ApiError = ApiError;
})(window);
