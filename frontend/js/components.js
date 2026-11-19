/* ============================================
   components.js — Helper giao diện chia sẻ cho trang Buyer/Public
   - Toast, Modal/confirm
   - Render product card, skeleton, empty/error state
   - Badge trạng thái đơn hàng & lô hàng (icon + chữ, không chỉ màu)
   - mountBuyerChrome: header + thanh danh mục + bottom nav + footer
   - refreshBadges: số giỏ hàng + thông báo chưa đọc
   ============================================ */
(function (global) {
  'use strict';
  var fmt = (global.NSV && global.NSV.format) || {};
  var auth = (global.NSV && global.NSV.auth) || {};
  var icon = (global.NSV && global.NSV.icon) || function () { return ''; };
  var renderIcons = (global.NSV && global.NSV.renderIcons) || function () {};

  /* ---------- Trạng thái đơn hàng ---------- */
  var ORDER_STATUS = {
    PENDING: { label: 'Chờ xác nhận', cls: 'badge--warning', icon: 'clock' },
    AWAITING_DEPOSIT: { label: 'Chờ đặt cọc', cls: 'badge--warning', icon: 'clock' },
    DEPOSIT_PAID: { label: 'Đã đặt cọc giữ chỗ', cls: 'badge--info', icon: 'wallet' },
    CONFIRMED: { label: 'Đã xác nhận', cls: 'badge--info', icon: 'check-circle' },
    SHIPPING: { label: 'Đang giao hàng', cls: 'badge--info', icon: 'truck' },
    DELIVERED: { label: 'Đã giao hàng', cls: 'badge--success', icon: 'package' },
    COMPLETED: { label: 'Hoàn tất', cls: 'badge--success', icon: 'check-circle' },
    CANCELLED: { label: 'Đã hủy', cls: 'badge--danger', icon: 'x-circle' }
  };

  function orderStatusInfo(status) {
    var key = (status || '').toUpperCase();
    return ORDER_STATUS[key] || { label: status || 'Đang xử lý', cls: 'badge--neutral', icon: 'info' };
  }
  function orderStatusLabel(status) { return orderStatusInfo(status).label; }
  function orderStatusBadge(status) {
    var info = orderStatusInfo(status);
    return '<span class="badge ' + info.cls + '">' + icon(info.icon, { size: 14, class: 'icon--sm' }) + ' ' + fmt.escape(info.label) + '</span>';
  }

  /* ---------- Badge lô hàng (product card) ---------- */
  function batchBadge(product) {
    var status = (product.batchStatus || '').toUpperCase();
    var isPreOrder = status === 'PRE_ORDER' || (product.harvestDate && fmt.toDate(product.harvestDate) && fmt.toDate(product.harvestDate) > new Date());
    var soldOut = status === 'SOLD_OUT' || (product.remainingQuantity != null && Number(product.remainingQuantity) <= 0);
    if (soldOut) return '<span class="badge badge--neutral">' + icon('x-circle', { size: 14, class: 'icon--sm' }) + ' Hết hàng</span>';
    if (isPreOrder) return '<span class="badge badge--seasonal">' + icon('clock', { size: 14, class: 'icon--sm' }) + ' Đặt trước</span>';
    return '';
  }

  /* ---------- Product card ---------- */
  function productCard(product) {
    var img = product.primaryImageUrl || (product.imageUrls && product.imageUrls[0]) || '';
    var imgHtml = img
      ? '<img src="' + fmt.escape(img) + '" alt="' + fmt.escape(product.name) + '" loading="lazy">'
      : '<div style="width:100%;height:100%;display:grid;place-items:center;color:var(--color-neutral-400)">' + icon('leaf', { size: 40 }) + '</div>';
    var farm = product.farmName ? '<p class="product-card__farm">' + icon('map-pin', { size: 14, class: 'icon--sm' }) + ' ' + fmt.escape(product.farmName) + (product.provinceName ? ', ' + fmt.escape(product.provinceName) : '') + '</p>' : '';
    var verified = product.farmVerified ? ' <span class="badge badge--success" style="font-size:11px">' + icon('badge-check', { size: 12, class: 'icon--sm' }) + ' Đã xác minh</span>' : '';
    var badge = batchBadge(product);
    var unit = product.unit ? ' / ' + fmt.escape(product.unit) : '';
    var href = 'product-detail.html?id=' + encodeURIComponent(product.id);
    return '' +
      '<article class="product-card">' +
        '<a class="product-card__link" href="' + href + '">' +
          '<div class="product-card__media">' + imgHtml + badge + '</div>' +
        '</a>' +
        '<div class="product-card__body">' +
          '<a class="product-card__link" href="' + href + '">' +
            '<h3 class="product-card__title">' + fmt.escape(product.name) + '</h3>' +
          '</a>' +
          farm +
          (product.categoryName ? '<p class="product-card__rating">' + icon('leaf', { size: 14, class: 'icon--sm' }) + ' ' + fmt.escape(product.categoryName) + verified + '</p>' : '') +
          '<div class="product-card__footer">' +
            '<span class="price price--sm">' + fmt.vnd(product.unitPrice) + '<span class="price__unit">' + unit + '</span></span>' +
            '<button class="btn btn--primary btn--sm add-cart-btn" type="button" data-batch-id="' + fmt.escape(product.batchId || '') + '" aria-label="Thêm ' + fmt.escape(product.name) + ' vào giỏ">' + icon('shopping-cart', { size: 16, class: 'icon--sm' }) + '</button>' +
          '</div>' +
        '</div>' +
      '</article>';
  }

  /* ---------- Skeletons ---------- */
  function skeletonCard() {
    return '<article class="product-card"><div class="product-card__media skeleton skeleton--media"></div>' +
      '<div class="product-card__body">' +
      '<div class="skeleton skeleton--text" style="height:18px"></div>' +
      '<div class="skeleton skeleton--text" style="width:60%"></div>' +
      '<div class="skeleton skeleton--text" style="width:40%"></div>' +
      '</div></article>';
  }
  function skeletonGrid(n) {
    n = n || 8;
    var html = '';
    for (var i = 0; i < n; i++) html += skeletonCard();
    return html;
  }

  /* ---------- Empty / Error ---------- */
  function emptyState(opts) {
    opts = opts || {};
    var action = opts.actionText ? '<a class="btn btn--primary" href="' + fmt.escape(opts.actionHref || '#') + '">' + fmt.escape(opts.actionText) + '</a>' : '';
    return '<div class="state">' +
      '<span class="state__icon">' + icon(opts.icon || 'leaf', { size: 56, class: 'icon--xl' }) + '</span>' +
      '<h3 class="state__title">' + fmt.escape(opts.title || 'Chưa có dữ liệu') + '</h3>' +
      '<p class="state__desc">' + fmt.escape(opts.desc || '') + '</p>' +
      action + '</div>';
  }
  function errorState(message, onRetry) {
    var id = 'errRetry' + Math.random().toString(36).slice(2, 8);
    var html = '<div class="state state--error" id="' + id + '">' +
      '<span class="state__icon">' + icon('alert-circle', { size: 56, class: 'icon--xl' }) + '</span>' +
      '<h3 class="state__title">Đã xảy ra lỗi</h3>' +
      '<p class="state__desc">' + fmt.escape(message || 'Không thể tải dữ liệu.') + '</p>' +
      '<button class="btn btn--secondary" type="button">' + icon('refresh-cw', { size: 16, class: 'icon--sm' }) + ' Thử lại</button>' +
      '</div>';
    if (typeof onRetry === 'function') {
      // gắn sau khi render
      setTimeout(function () {
        var el = document.getElementById(id);
        if (el) el.querySelector('button').addEventListener('click', onRetry);
      }, 0);
    }
    return html;
  }

  /* ---------- Loading overlay ---------- */
  function spinner(opts) {
    opts = opts || {};
    return '<span class="spinner' + (opts.lg ? ' spinner--lg' : '') + '" role="status" aria-label="Đang tải"></span>';
  }

  /* ---------- Toast ---------- */
  function ensureToastContainer() {
    var c = document.getElementById('toastContainer');
    if (!c) {
      c = document.createElement('div');
      c.className = 'toast-container';
      c.id = 'toastContainer';
      document.body.appendChild(c);
    }
    return c;
  }
  function toast(message, type, opts) {
    opts = opts || {};
    var c = ensureToastContainer();
    var iconName = type === 'success' ? 'check-circle' : type === 'danger' ? 'alert-circle' : type === 'warning' ? 'alert-triangle' : 'info';
    var cls = type ? ' toast--' + type : '';
    var el = document.createElement('div');
    el.className = 'toast' + cls;
    el.setAttribute('role', opts.permanent ? 'alert' : 'status');
    el.innerHTML = '<span class="toast__icon">' + icon(iconName, { size: 18 }) + '</span>' +
      '<span class="toast__msg">' + fmt.escape(message) + '</span>' +
      '<button class="toast__close" type="button" aria-label="Đóng thông báo">' + icon('x', { size: 16, class: 'icon--sm' }) + '</button>';
    c.appendChild(el);
    renderIcons(el);
    requestAnimationFrame(function () { el.classList.add('show'); });
    var close = function () { el.classList.remove('show'); setTimeout(function () { el.remove(); }, 250); };
    el.querySelector('.toast__close').addEventListener('click', close);
    if (!opts.permanent) setTimeout(close, opts.duration || 4000);
    return el;
  }

  /* ---------- Modal / confirm ---------- */
  function openModal(opts) {
    opts = opts || {};
    var modal = document.createElement('div');
    modal.className = 'modal';
    modal.setAttribute('role', 'dialog');
    modal.setAttribute('aria-modal', 'true');
    modal.innerHTML =
      '<div class="modal__overlay" data-close></div>' +
      '<div class="modal__dialog' + (opts.size === 'sm' ? ' modal__dialog--sm' : '') + (opts.danger ? ' modal__dialog--danger' : '') + '">' +
        (opts.closable !== false ? '<button class="modal__close" type="button" aria-label="Đóng" data-close>' + icon('x', { size: 20 }) + '</button>' : '') +
        '<h3 class="modal__title">' + fmt.escape(opts.title || '') + '</h3>' +
        '<div class="modal__body">' + (opts.body || '') + '</div>' +
        '<div class="modal__footer"></div>' +
      '</div>';
    document.body.appendChild(modal);
    renderIcons(modal);
    var footer = modal.querySelector('.modal__footer');
    var actions = opts.actions || [];
    actions.forEach(function (a) {
      var b = document.createElement('button');
      b.type = 'button';
      b.className = 'btn ' + (a.cls || 'btn--secondary');
      b.innerHTML = (a.icon ? icon(a.icon, { size: 16, class: 'icon--sm' }) : '') + '<span>' + fmt.escape(a.label) + '</span>';
      b.addEventListener('click', function () {
        if (a.onClick) {
          var res = a.onClick(modal);
          if (res !== false) closeModal();
        } else {
          closeModal();
        }
      });
      footer.appendChild(b);
      renderIcons(b);
    });
    function closeModal() {
      modal.classList.remove('is-open');
      setTimeout(function () { modal.remove(); document.removeEventListener('keydown', onKey); }, 200);
    }
    function onKey(e) { if (e.key === 'Escape') closeModal(); }
    if (opts.closable !== false) {
      modal.querySelectorAll('[data-close]').forEach(function (el) { el.addEventListener('click', closeModal); });
      document.addEventListener('keydown', onKey);
    }
    requestAnimationFrame(function () { modal.classList.add('is-open'); });
    return { el: modal, close: closeModal };
  }
  function confirm(opts) {
    opts = opts || {};
    return new Promise(function (resolve) {
      openModal({
        title: opts.title || 'Xác nhận',
        body: opts.body || '',
        danger: !!opts.danger,
        size: opts.size,
        closable: opts.closable !== false,
        actions: [
          { label: opts.cancelLabel || 'Hủy', cls: 'btn--ghost', onClick: function () { resolve(false); } },
          { label: opts.confirmLabel || 'Xác nhận', cls: opts.danger ? 'btn--danger' : 'btn--primary', icon: opts.confirmIcon, onClick: function () { resolve(true); } }
        ]
      });
    });
  }

  /* ---------- Query params ---------- */
  function getQueryParam(name) {
    var u = new URL(window.location.href);
    return u.searchParams.get(name);
  }
  function buildQuery(params) {
    var s = new URLSearchParams();
    Object.keys(params || {}).forEach(function (k) {
      var v = params[k];
      if (v !== undefined && v !== null && v !== '') s.append(k, v);
    });
    return s.toString();
  }

  /* ---------- Buyer chrome (header + category bar + bottom nav + footer) ---------- */
  var CATEGORIES = [
    { id: '', name: 'Tất cả', icon: 'list' },
    { id: 'seasonal', name: 'Pre-order theo mùa', icon: 'clock' },
    { id: 'combo', name: 'Combo', icon: 'shopping-bag' },
    { id: 'region', name: 'Theo khu vực', icon: 'map-pin' }
    // các danh mục thật (Rau củ, Trái cây...) được render động từ /products/filter-options
  ];

  var BOTTOM_NAV = [
    { id: 'home', label: 'Trang chủ', icon: 'home', href: 'home.html' },
    { id: 'search', label: 'Tìm kiếm', icon: 'search', href: 'search.html' },
    { id: 'cart', label: 'Giỏ hàng', icon: 'shopping-cart', href: 'cart.html', badge: true },
    { id: 'orders', label: 'Đơn hàng', icon: 'package', href: 'orders.html' },
    { id: 'profile', label: 'Tôi', icon: 'user', href: 'profile.html' }
  ];

  function mountBuyerChrome(opts) {
    opts = opts || {};
    var header = document.getElementById('appHeader');
    if (header && !header.dataset.mounted) {
      var user = auth.getUser && auth.getUser() || {};
      var name = user.fullName ? (user.fullName.split(' ').slice(-1)[0]) : '';
      header.dataset.mounted = '1';
      header.className = 'app-header';
      header.innerHTML =
        '<div class="app-header__inner">' +
          '<a class="app-header__brand" href="home.html" aria-label="Nông Sản Việt - Trang chủ">' +
            '<img src="../../assets/images/logocnpm 1.png" alt="" />' +
            '<span>Nông Sản Việt</span>' +
          '</a>' +
          '<form class="app-header__search" id="headerSearch" role="search">' +
            '<span class="app-header__search__icon">' + icon('search', { size: 18 }) + '</span>' +
            '<input class="form-field__input" type="search" id="headerSearchInput" placeholder="Tìm rau củ, trái cây, trang trại..." aria-label="Tìm kiếm sản phẩm">' +
            '<button class="app-header__search__btn" type="submit">Tìm</button>' +
          '</form>' +
          '<div class="app-header__actions">' +
            '<a class="icon-btn" href="cart.html" aria-label="Giỏ hàng">' + icon('shopping-cart', { size: 22 }) + '<span class="icon-btn__badge" id="cartBadge" hidden>0</span></a>' +
            '<a class="icon-btn" href="notifications.html" aria-label="Thông báo">' + icon('bell', { size: 22 }) + '<span class="icon-btn__badge" id="notifBadge" hidden>0</span></a>' +
            '<div class="dropdown" id="accountDropdown">' +
              '<button class="app-header__avatar-btn" type="button" aria-haspopup="true" aria-expanded="false" id="accountBtn">' +
                '<span class="avatar avatar--sm">' + fmt.escape((name || '?').charAt(0).toUpperCase()) + '</span>' +
                '<span class="app-header__username">' + fmt.escape(name || 'Tài khoản') + '</span>' +
                icon('chevron-down', { size: 16, class: 'icon--sm' }) +
              '</button>' +
              '<div class="dropdown__menu" id="accountMenu" role="menu">' +
                '<a class="dropdown__item" href="profile.html" role="menuitem">' + icon('user', { size: 18, class: 'icon--sm' }) + ' Hồ sơ cá nhân</a>' +
                '<a class="dropdown__item" href="orders.html" role="menuitem">' + icon('package', { size: 18, class: 'icon--sm' }) + ' Đơn hàng của tôi</a>' +
                '<a class="dropdown__item" href="notifications.html" role="menuitem">' + icon('bell', { size: 18, class: 'icon--sm' }) + ' Thông báo</a>' +
                '<button class="dropdown__item" type="button" id="logoutBtn" role="menuitem">' + icon('log-out', { size: 18, class: 'icon--sm' }) + ' Đăng xuất</button>' +
              '</div>' +
            '</div>' +
          '</div>' +
        '</div>';
      renderIcons(header);
      wireHeader(header, opts);
    }

    var cat = document.getElementById('categoryBar');
    if (cat && !cat.dataset.mounted) {
      cat.dataset.mounted = '1';
      cat.className = 'category-bar';
      cat.innerHTML = '<div class="category-bar__inner" id="categoryBarInner">' +
        CATEGORIES.map(function (c) {
          return '<a class="category-bar__link' + (opts.category === c.id ? ' is-active' : '') + '" href="search.html?cat=' + encodeURIComponent(c.id) + '">' +
            icon(c.icon, { size: 16, class: 'icon--sm' }) + ' ' + fmt.escape(c.name) + '</a>';
        }).join('') + '</div>';
      renderIcons(cat);
      // bổ sung danh mục thật từ backend
      if (global.NSV.productApi && global.NSV.productApi.getFilterOptions) {
        global.NSV.productApi.getFilterOptions().then(function (res) {
          if (!res || !res.categories) return;
          var inner = document.getElementById('categoryBarInner');
          if (!inner) return;
          var realCats = res.categories.map(function (c) {
            return '<a class="category-bar__link" href="search.html?categoryId=' + encodeURIComponent(c.id) + '">' + icon('leaf', { size: 16, class: 'icon--sm' }) + ' ' + fmt.escape(c.name) + '</a>';
          }).join('');
          var div = document.createElement('div');
          div.innerHTML = realCats;
          while (div.firstChild) inner.appendChild(div.firstChild);
          renderIcons(inner);
        }).catch(function () { /* bỏ qua nếu chưa đăng nhập/lỗi */ });
      }
    }

    var footer = document.getElementById('appFooter');
    if (footer && !footer.dataset.mounted) {
      footer.dataset.mounted = '1';
      footer.className = 'app-footer';
      footer.innerHTML =
        '<div class="app-footer__inner">' +
          '<div><span class="app-footer__brand"><img src="../../assets/images/logocnpm 1.png" alt="" style="width:28px;height:28px"> Nông Sản Việt</span>' +
          '<p style="font-size:var(--fs-sm);margin-top:var(--space-3);max-width:260px">Nền tảng kết nối nông sản trực tiếp từ nông dân đến người tiêu dùng. Tươi từ gốc, an tâm mỗi ngày.</p></div>' +
          '<div><h4>Nông sản</h4><ul><li><a href="search.html">Tất cả sản phẩm</a></li><li><a href="search.html?cat=seasonal">Pre-order theo mùa</a></li><li><a href="search.html?cat=combo">Combo tiết kiệm</a></li></ul></div>' +
          '<div><h4>Tài khoản</h4><ul><li><a href="orders.html">Đơn hàng của tôi</a></li><li><a href="profile.html">Hồ sơ & sổ địa chỉ</a></li><li><a href="notifications.html">Thông báo</a></li></ul></div>' +
          '<div><h4>Hỗ trợ</h4><ul><li><a href="orders.html">Khiếu nại đơn hàng</a></li><li><a href="#">Chính sách bảo mật</a></li><li><a href="#">Điều khoản sử dụng</a></li></ul></div>' +
        '</div>' +
        '<div class="app-footer__bottom">© ' + new Date().getFullYear() + ' Nông Sản Việt (FarmDirect). Đồ án môn Nhập môn Công nghệ Phần mềm.</div>';
      renderIcons(footer);
    }

    var bn = document.getElementById('bottomNav');
    if (bn && !bn.dataset.mounted) {
      bn.dataset.mounted = '1';
      bn.className = 'bottom-nav';
      bn.innerHTML = BOTTOM_NAV.map(function (it) {
        var badge = it.badge ? '<span class="bottom-nav__item__badge" id="navBadge_' + it.id + '" hidden>0</span>' : '';
        return '<a class="bottom-nav__item' + (opts.active === it.id ? ' is-active' : '') + '" href="' + it.href + '" aria-label="' + it.label + '">' +
          '<span class="bottom-nav__item__icon">' + icon(it.icon, { size: 22 }) + badge + '</span>' +
          '<span class="bottom-nav__item__label">' + it.label + '</span></a>';
      }).join('');
      renderIcons(bn);
    }

    // header scroll shadow
    if (header) {
      var onScroll = function () { header.classList.toggle('is-scrolled', window.scrollY > 0); };
      window.addEventListener('scroll', onScroll, { passive: true }); onScroll();
    }

    refreshBadges();
  }

  function wireHeader(header, opts) {
    var form = header.querySelector('#headerSearch');
    if (form) {
      form.addEventListener('submit', function (e) {
        e.preventDefault();
        var q = header.querySelector('#headerSearchInput').value.trim();
        if (typeof opts.onSearch === 'function') { opts.onSearch(q); return; }
        window.location.href = 'search.html' + (q ? '?q=' + encodeURIComponent(q) : '');
      });
    }
    var dd = header.querySelector('#accountDropdown');
    var btn = header.querySelector('#accountBtn');
    if (dd && btn) {
      btn.addEventListener('click', function (e) {
        e.stopPropagation();
        dd.classList.toggle('is-open');
        btn.setAttribute('aria-expanded', dd.classList.contains('is-open'));
      });
      document.addEventListener('click', function (e) {
        if (!dd.contains(e.target)) { dd.classList.remove('is-open'); btn.setAttribute('aria-expanded', 'false'); }
      });
    }
    var lo = header.querySelector('#logoutBtn');
    if (lo) lo.addEventListener('click', function () {
      confirm({ title: 'Đăng xuất', body: 'Bạn chắc chắn muốn đăng xuất?', confirmLabel: 'Đăng xuất', danger: true })
        .then(function (ok) { if (ok) { auth.logout && auth.logout(); window.location.href = '../auth/login.html'; } });
    });
  }

  function refreshBadges() {
    if (!auth.isLoggedIn || !auth.isLoggedIn()) return;
    // thông báo chưa đọc
    if (global.NSV.notificationApi && global.NSV.notificationApi.getUnreadCount) {
      global.NSV.notificationApi.getUnreadCount().then(function (res) {
        var n = res && res.unreadCount != null ? Number(res.unreadCount) : 0;
        setBadge('notifBadge', n); setBadge('navBadge_search', n);
      }).catch(function () {});
    }
    // giỏ hàng
    if (global.NSV.cartApi && global.NSV.cartApi.getCart) {
      global.NSV.cartApi.getCart().then(function (cart) {
        var n = cart && cart.totalItems != null ? Number(cart.totalItems) : 0;
        setBadge('cartBadge', n); setBadge('navBadge_cart', n);
      }).catch(function () {});
    }
  }
  function setBadge(id, n) {
    var el = document.getElementById(id);
    if (!el) return;
    if (n > 0) { el.textContent = n > 99 ? '99+' : String(n); el.hidden = false; }
    else { el.hidden = true; }
  }

  function requireBuyer() {
    return auth.requireRole ? auth.requireRole('BUYER', '../auth/login.html') : true;
  }

  // Gắn hành vi "Thêm vào giỏ" cho các nút .add-cart-btn bên trong scope
  function wireAddToCart(scope) {
    scope = scope || document;
    scope.querySelectorAll('.add-cart-btn').forEach(function (btn) {
      if (btn.dataset.wired) return; btn.dataset.wired = '1';
      btn.addEventListener('click', function (e) {
        e.preventDefault();
        var batchId = btn.getAttribute('data-batch-id');
        addToCartFlow(batchId, btn);
      });
    });
  }
  function addToCartFlow(batchId, btn) {
    if (!batchId || batchId === 'null' || batchId === '') { toast('Sản phẩm chưa có lô hàng để thêm vào giỏ.', 'danger'); return; }
    if (!auth.isLoggedIn || !auth.isLoggedIn()) {
      toast('Vui lòng đăng nhập để thêm vào giỏ.', 'info');
      setTimeout(function () { window.location.href = '../auth/login.html'; }, 800);
      return;
    }
    var qty = 1;
    global.NSV.cartApi.addItem({ batchId: Number(batchId), quantity: qty }).then(function () {
      toast('Đã thêm vào giỏ hàng.', 'success');
      refreshBadges();
    }).catch(function (err) { toast((err && err.message) || 'Thêm vào giỏ thất bại.', 'danger'); });
  }

  global.NSV = global.NSV || {};
  global.NSV.ui = {
    toast: toast, openModal: openModal, confirm: confirm,
    productCard: productCard, skeletonGrid: skeletonGrid, skeletonCard: skeletonCard,
    emptyState: emptyState, errorState: errorState, spinner: spinner,
    orderStatusLabel: orderStatusLabel, orderStatusInfo: orderStatusInfo, orderStatusBadge: orderStatusBadge,
    batchBadge: batchBadge,
    getQueryParam: getQueryParam, buildQuery: buildQuery,
    mountBuyerChrome: mountBuyerChrome, refreshBadges: refreshBadges,
    requireBuyer: requireBuyer, wireAddToCart: wireAddToCart,
    renderIcons: renderIcons
  };
})(window);
