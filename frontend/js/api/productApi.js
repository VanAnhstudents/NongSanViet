/* ============================================
   api/productApi.js — Sản phẩm & tìm kiếm (public)
   GET /products, /products/{id}, /products/search, /products/filter-options
   ============================================ */
(function (global) {
  'use strict';
  var api = global.NSV && global.NSV.api;

  // Danh sách sản phẩm (trang chủ) — lọc đơn giản theo keyword/danh mục/tỉnh/giá
  function getProducts(params) {
    return api.get('/products', params || {});
  }

  function getProductDetail(id) {
    return api.get('/products/' + encodeURIComponent(id));
  }

  // Tìm kiếm + phân trang + sắp xếp
  function search(params) {
    var q = {
      keyword: params.keyword || '',
      categoryId: params.categoryId || '',
      provinceId: params.provinceId || '',
      minPrice: params.minPrice || '',
      maxPrice: params.maxPrice || '',
      verifiedOnly: params.verifiedOnly === true ? 'true' : 'false',
      sortBy: params.sortBy || 'NEWEST',
      page: params.page != null ? params.page : 0,
      size: params.size != null ? params.size : 12
    };
    return api.get('/products/search', q);
  }

  // Danh mục + tỉnh để render bộ lọc
  function getFilterOptions() {
    return api.get('/products/filter-options');
  }

  global.NSV = global.NSV || {};
  global.NSV.productApi = {
    getProducts: getProducts,
    getProductDetail: getProductDetail,
    search: search,
    getFilterOptions: getFilterOptions
  };
})(window);
