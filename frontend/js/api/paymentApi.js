/* ============================================
   api/paymentApi.js — Thanh toán của Buyer (PBI-18)
   POST /buyer/orders/{orderId}/payments   { paymentMethod:'COD'|'ONLINE' }
   POST /buyer/payments/{paymentId}/simulate { outcome:'SUCCESS'|'FAILED' }
   GET  /buyer/orders/{orderId}/payment
   ============================================ */
(function (global) {
  'use strict';
  var api = global.NSV && global.NSV.api;

  function choosePaymentMethod(orderId, paymentMethod) {
    return api.post('/buyer/orders/' + encodeURIComponent(orderId) + '/payments', { paymentMethod: paymentMethod });
  }
  function simulate(paymentId, outcome) {
    return api.post('/buyer/payments/' + encodeURIComponent(paymentId) + '/simulate', { outcome: outcome });
  }
  function getLatest(orderId) {
    return api.get('/buyer/orders/' + encodeURIComponent(orderId) + '/payment');
  }

  global.NSV = global.NSV || {};
  global.NSV.paymentApi = {
    choosePaymentMethod: choosePaymentMethod,
    simulate: simulate,
    getLatest: getLatest
  };
})(window);
