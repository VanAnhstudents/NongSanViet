/* ============================================
   api/traceApi.js — Truy xuất nguồn gốc QR (public)
   GET /trace/{traceabilityCode}, GET /batches/{batchId}/trace
   ============================================ */
(function (global) {
  'use strict';
  var api = global.NSV && global.NSV.api;

  function scanByCode(code) { return api.get('/trace/' + encodeURIComponent(code)); }
  function getByBatch(batchId) { return api.get('/batches/' + encodeURIComponent(batchId) + '/trace'); }

  global.NSV = global.NSV || {};
  global.NSV.traceApi = { scanByCode: scanByCode, getByBatch: getByBatch };
})(window);
