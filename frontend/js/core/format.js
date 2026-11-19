/* ============================================
   core/format.js — Tiện ích định dạng (VNĐ, ngày giờ, số)
   Tiếng Việt, đơn vị VNĐ: 1.250.000₫ (dấu chấm ngăn nghìn, ₫ sau số)
   ============================================ */
(function (global) {
  'use strict';

  function formatVND(value) {
    var n = Number(value);
    if (value === null || value === undefined || isNaN(n)) return '—';
    var neg = n < 0;
    var s = Math.abs(n).toFixed(0);
    s = s.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
    return (neg ? '-' : '') + s + '₫';
  }

  // Hiển thị giá kèm đơn vị: 65.000₫ / kg
  function formatPrice(value, unit) {
    return formatVND(value) + (unit ? ' / ' + unit : '');
  }

  // Chuẩn hóa chuỗi ngày: hỗ trợ ISO (OffsetDateTime/LocalDate), fallback trả về chuỗi gốc.
  function toDate(value) {
    if (!value) return null;
    if (value instanceof Date) return value;
    var s = String(value);
    // LocalDate 'yyyy-MM-dd'
    if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
      var p = s.split('-');
      return new Date(Number(p[0]), Number(p[1]) - 1, Number(p[2]));
    }
    // ISO với 'Z' hay offset → Date tự parse; Java OffsetDateTime có offset (+07:00)
    var d = new Date(s);
    return isNaN(d.getTime()) ? null : d;
  }

  function formatDate(value) {
    var d = toDate(value);
    if (!d) return '—';
    var dd = String(d.getDate()).padStart(2, '0');
    var mm = String(d.getMonth() + 1).padStart(2, '0');
    return dd + '/' + mm + '/' + d.getFullYear();
  }

  function formatDateTime(value) {
    var d = toDate(value);
    if (!d) return '—';
    var dd = String(d.getDate()).padStart(2, '0');
    var mm = String(d.getMonth() + 1).padStart(2, '0');
    var hh = String(d.getHours()).padStart(2, '0');
    var mi = String(d.getMinutes()).padStart(2, '0');
    return dd + '/' + mm + '/' + d.getFullYear() + ' · ' + hh + ':' + mi;
  }

  // Thời gian tương đối ngắn gọn: "5 phút trước", "2 ngày trước"
  function timeAgo(value) {
    var d = toDate(value);
    if (!d) return '';
    var diff = Date.now() - d.getTime();
    var sec = Math.floor(diff / 1000);
    if (sec < 60) return 'Vừa xong';
    var min = Math.floor(sec / 60);
    if (min < 60) return min + ' phút trước';
    var hr = Math.floor(min / 60);
    if (hr < 24) return hr + ' giờ trước';
    var day = Math.floor(hr / 24);
    if (day < 30) return day + ' ngày trước';
    return formatDate(d);
  }

  function formatNumber(value, digits) {
    var n = Number(value);
    if (isNaN(n)) return '—';
    return n.toLocaleString('vi-VN', { maximumFractionDigits: digits == null ? 2 : digits });
  }

  function pluralize(n, unit) {
    return formatNumber(n, 2).replace(/\.?0+$/, '') + ' ' + (unit || '');
  }

  function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  global.NSV = global.NSV || {};
  global.NSV.format = {
    vnd: formatVND,
    price: formatPrice,
    date: formatDate,
    dateTime: formatDateTime,
    timeAgo: timeAgo,
    number: formatNumber,
    pluralize: pluralize,
    escape: escapeHtml,
    toDate: toDate
  };
})(window);
