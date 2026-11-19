/* ============================================
   api/notificationApi.js — Thông báo trong ứng dụng (BUYER/FARMER/ADMIN)
   GET /notifications?unreadOnly, /notifications/unread-count,
   PATCH /notifications/{id}/read, /notifications/read-all
   ============================================ */
(function (global) {
  'use strict';
  var api = global.NSV && global.NSV.api;

  function getNotifications(unreadOnly) {
    return api.get('/notifications', { unreadOnly: unreadOnly ? 'true' : 'false' });
  }
  function getUnreadCount() { return api.get('/notifications/unread-count'); }
  function markRead(notificationId) {
    return api.patch('/notifications/' + encodeURIComponent(notificationId) + '/read', {});
  }
  function markAllRead() { return api.patch('/notifications/read-all', {}); }

  global.NSV = global.NSV || {};
  global.NSV.notificationApi = {
    getNotifications: getNotifications,
    getUnreadCount: getUnreadCount,
    markRead: markRead,
    markAllRead: markAllRead
  };
})(window);
