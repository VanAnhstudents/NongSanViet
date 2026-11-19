/* ============================================
   api/cartApi.js — Giỏ hàng (BUYER)
   GET /cart, POST /cart/items, PATCH /cart/items/{id}, DELETE /cart/items/{id}
   ============================================ */
(function (global) {
  'use strict';
  var api = global.NSV && global.NSV.api;

  function getCart() { return api.get('/cart'); }

  // { batchId, quantity }
  function addItem(payload) { return api.post('/cart/items', payload); }

  function updateItem(cartItemId, quantity) {
    return api.patch('/cart/items/' + encodeURIComponent(cartItemId), { quantity: quantity });
  }

  function removeItem(cartItemId) {
    return api.del('/cart/items/' + encodeURIComponent(cartItemId));
  }

  global.NSV = global.NSV || {};
  global.NSV.cartApi = {
    getCart: getCart,
    addItem: addItem,
    updateItem: updateItem,
    removeItem: removeItem
  };
})(window);
