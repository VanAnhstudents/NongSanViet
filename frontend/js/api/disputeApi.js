/* ============================================
   api/disputeApi.js — Khiếu nại / tranh chấp (BUYER)
   GET /orders/{id}/complaint-eligibility
   POST /orders/{id}/complaints            (JSON kèm evidences)
   POST /orders/{id}/complaints/multipart   (tải file trực tiếp)
   GET /orders/{id}/complaint
   GET /buyer/complaints
   ============================================ */
(function (global) {
  'use strict';
  var api = global.NSV && global.NSV.api;

  function checkEligibility(orderId) {
    return api.get('/orders/' + encodeURIComponent(orderId) + '/complaint-eligibility');
  }

  // Gửi khiếu nại JSON: { reason, description, evidences:[{mediaType:'IMAGE'|'VIDEO', mediaUrl, caption}] }
  function createComplaint(orderId, payload) {
    return api.post('/orders/' + encodeURIComponent(orderId) + '/complaints', payload);
  }

  // Gửi khiếu nại kèm file ảnh/video tải trực tiếp (multipart/form-data)
  function createComplaintMultipart(orderId, fields) {
    // fields: { reason, description, files: [File...], captions: [string...] }
    var fd = new FormData();
    fd.append('reason', fields.reason);
    fd.append('description', fields.description);
    var files = fields.files || [];
    for (var i = 0; i < files.length; i++) fd.append('files', files[i], files[i].name);
    if (fields.captions && fields.captions.length) {
      fields.captions.forEach(function (c) { fd.append('captions', c == null ? '' : c); });
    }
    return api.request('/orders/' + encodeURIComponent(orderId) + '/complaints/multipart', {
      method: 'POST', formData: fd
    });
  }

  function getComplaintByOrder(orderId) {
    return api.get('/orders/' + encodeURIComponent(orderId) + '/complaint');
  }
  function getMyComplaints() { return api.get('/buyer/complaints'); }

  global.NSV = global.NSV || {};
  global.NSV.disputeApi = {
    checkEligibility: checkEligibility,
    createComplaint: createComplaint,
    createComplaintMultipart: createComplaintMultipart,
    getComplaintByOrder: getComplaintByOrder,
    getMyComplaints: getMyComplaints
  };
})(window);
