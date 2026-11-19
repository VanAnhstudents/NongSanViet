/* ============================================
   api/chatApi.js — Chat Buyer ↔ Farmer (PBI-24)
   POST /chat/messages, GET /chat/conversations/{partnerId}/messages?afterId&limit
   ============================================ */
(function (global) {
  'use strict';
  var api = global.NSV && global.NSV.api;

  // { receiverId, content }
  function send(payload) { return api.post('/chat/messages', payload); }

  function history(partnerId, afterId, limit) {
    var q = {};
    if (afterId != null) q.afterId = afterId;
    if (limit != null) q.limit = limit;
    return api.get('/chat/conversations/' + encodeURIComponent(partnerId) + '/messages', q);
  }

  global.NSV = global.NSV || {};
  global.NSV.chatApi = { send: send, history: history };
})(window);
