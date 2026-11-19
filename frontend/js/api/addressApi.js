/* ============================================
   api/addressApi.js — Địa chỉ & phí vận chuyển (BUYER)
   GET /buyer/provinces, /buyer/addresses, POST /buyer/addresses,
   PUT /buyer/addresses/{id}, DELETE /buyer/addresses/{id}, GET /buyer/shipping-fee
   ============================================ */
(function (global) {
  'use strict';
  var api = global.NSV && global.NSV.api;

  function getProvinces() { return api.get('/buyer/provinces'); }
  function getAddresses() { return api.get('/buyer/addresses'); }
  // { provinceId, receiverName, receiverPhone, addressLine }
  function createAddress(payload) { return api.post('/buyer/addresses', payload); }
  function updateAddress(id, payload) { return api.put('/buyer/addresses/' + encodeURIComponent(id), payload); }
  function deleteAddress(id) { return api.del('/buyer/addresses/' + encodeURIComponent(id)); }
  // shippingFee(addressId, farmerId)
  function getShippingFee(addressId, farmerId) {
    return api.get('/buyer/shipping-fee', { addressId: addressId, farmerId: farmerId });
  }

  global.NSV = global.NSV || {};
  global.NSV.addressApi = {
    getProvinces: getProvinces,
    getAddresses: getAddresses,
    createAddress: createAddress,
    updateAddress: updateAddress,
    deleteAddress: deleteAddress,
    getShippingFee: getShippingFee
  };
})(window);
