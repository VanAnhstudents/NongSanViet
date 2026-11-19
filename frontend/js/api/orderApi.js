/* ============================================
   api/orderApi.js — Đơn hàng & đặt trước (BUYER)
   POST /orders/checkout, GET /orders, GET /orders/{id}
   POST /orders/pre-order, POST /orders/{id}/deposit, GET /orders/pre-orders, GET /orders/pre-orders/{id}
   ============================================ */
(function (global) {
  'use strict';
  var api = global.NSV && global.NSV.api;

  // Đặt hàng từ giỏ: { addressId, paymentMethod:'COD'|'ONLINE', shippingFee, cartItemIds }
  function checkout(payload) { return api.post('/orders/checkout', payload); }

  function getOrders() { return api.get('/orders'); }
  function getOrderDetail(id) { return api.get('/orders/' + encodeURIComponent(id)); }

  // Đặt trước (pre-order): { batchId, quantity, addressId, shippingFee, depositRate }
  function createPreOrder(payload) { return api.post('/orders/pre-order', payload); }
  function payDeposit(orderId) { return api.post('/orders/' + encodeURIComponent(orderId) + '/deposit', {}); }
  function getPreOrders() { return api.get('/orders/pre-orders'); }
  function getPreOrderDetail(orderId) {
    return api.get('/orders/pre-orders/' + encodeURIComponent(orderId));
  }

  global.NSV = global.NSV || {};
  global.NSV.orderApi = {
    checkout: checkout,
    getOrders: getOrders,
    getOrderDetail: getOrderDetail,
    createPreOrder: createPreOrder,
    payDeposit: payDeposit,
    getPreOrders: getPreOrders,
    getPreOrderDetail: getPreOrderDetail
  };
})(window);
