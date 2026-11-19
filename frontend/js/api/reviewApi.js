/* ============================================
   api/reviewApi.js — Đánh giá & uy tín Farmer
   POST /buyer/reviews, GET /buyer/products/{id}/reviews
   GET /buyer/farmers/{farmerId}/reputation
   ============================================ */
(function (global) {
  'use strict';
  var api = global.NSV && global.NSV.api;

  // { orderItemId, rating(1-5), comment }
  function createReview(payload) { return api.post('/buyer/reviews', payload); }

  function getProductReviews(productId) {
    return api.get('/buyer/products/' + encodeURIComponent(productId) + '/reviews');
  }

  function getFarmerReputation(farmerId) {
    return api.get('/buyer/farmers/' + encodeURIComponent(farmerId) + '/reputation');
  }

  global.NSV = global.NSV || {};
  global.NSV.reviewApi = {
    createReview: createReview,
    getProductReviews: getProductReviews,
    getFarmerReputation: getFarmerReputation
  };
})(window);
