document.addEventListener('DOMContentLoaded', () => {
    const body = document.body;
    const role = body.dataset.role || 'buyer';
    const activeNav = body.dataset.active || '';
    const showBottom = body.dataset.bottom === 'true';

    // 1. Render Brand Header chuẩn của từng vai trò (sử dụng tệp frontend/assets/images/logocnpm 1.png)
    const brandHeader = document.getElementById('app-brand-header');
    if (brandHeader) {
        const homeLinks = {
            auth: '../../auth/login.html',
            buyer: '../../buyer/home.html',
            farmer: '../../farmer/dashboard.html',
            admin: '../../admin/dashboard.html'
        };

        const roleActions = {
            auth: '',
            buyer: `
        <div class="header-actions">
          <a href="../../buyer/cart.html" class="header-pill">Giỏ hàng</a>
          <a href="../../buyer/notifications.html" class="header-pill">Thông báo</a>
        </div>
      `,
            farmer: `
        <div class="header-actions">
          <a href="../../auth/login.html" class="header-pill">Đăng xuất</a>
        </div>
      `,
            admin: `
        <div class="header-actions">
          <a href="../../auth/login.html" class="header-pill">Đăng xuất</a>
        </div>
      `
        };

        brandHeader.innerHTML = `
      <a href="${homeLinks[role]}" class="brand">
        <img src="../../assets/images/logocnpm 1.png" alt="Nông Sản Việt" class="brand-logo" />
        <span>Nông Sản Việt</span>
      </a>
      ${roleActions[role] || ''}
    `;
    }

    // 2. Render thanh điều hướng nghiệp vụ nội bộ cho Admin
    const adminNav = document.getElementById('app-admin-nav');
    if (adminNav && role === 'admin') {
        const adminTabs = [
            { id: 'dashboard', label: 'Tổng quan', href: '../admin/dashboard.html' },
            { id: 'users', label: 'Người dùng', href: '../admin/users.html' },
            { id: 'products', label: 'Sản phẩm', href: '../admin/products.html' },
            { id: 'disputes', label: 'Tranh chấp', href: '../admin/disputes.html' },
            { id: 'categories', label: 'Danh mục', href: '../admin/categories.html' }
        ];
        adminNav.innerHTML = adminTabs
            .map(tab => `<a href="${tab.href}" class="${activeNav === tab.id ? 'active' : ''}">${tab.label}</a>`)
            .join('');
    }

    // 3. Render Bottom Navigation cho Buyer và Farmer
    const bottomNav = document.getElementById('app-bottom-nav');
    if (bottomNav && showBottom) {
        document.querySelector('.app-shell')?.classList.add('has-bottom-nav');
        const navMap = {
            buyer: [
                { id: 'home', icon: '⌂', label: 'Khám phá', href: '../buyer/home.html' },
                { id: 'orders', icon: '☰', label: 'Đơn hàng', href: '../buyer/order-tracking.html' },
                { id: 'chat', icon: '💬', label: 'Tin nhắn', href: '../buyer/chat.html' },
                { id: 'account', icon: '👤', label: 'Tài khoản', href: '../auth/login.html' }
            ],
            farmer: [
                { id: 'dashboard', icon: '⌂', label: 'Tổng quan', href: '../farmer/dashboard.html' },
                { id: 'products', icon: '⊞', label: 'Sản phẩm', href: '../farmer/products.html' },
                { id: 'orders', icon: '☰', label: 'Đơn hàng', href: '../farmer/orders.html' },
                { id: 'profile', icon: '👤', label: 'Gian hàng', href: '../farmer/farm-profile.html' }
            ]
        };

        const items = navMap[role] || [];
        bottomNav.innerHTML = items.map(item => `
      <a href="${item.href}" class="nav-item ${activeNav === item.id ? 'active' : ''}">
        <span style="font-size:16px">${item.icon}</span>
        <span>${item.label}</span>
      </a>
    `).join('');
    }

    // 4. Segmented Tabs & Filter Chips
    document.querySelectorAll('.seg-tabs, .chip-row, .filter-tabs').forEach(group => {
        group.addEventListener('click', (e) => {
            const target = e.target.closest('button, span');
            if (!target) return;
            group.querySelectorAll('button, span').forEach(el => el.classList.remove('active'));
            target.classList.add('active');
        });
    });

    // 5. Bộ tăng giảm số lượng
    document.querySelectorAll('[data-qty-minus]').forEach(btn => {
        btn.addEventListener('click', () => {
            const span = btn.parentElement.querySelector('[data-qty-val]');
            let val = parseInt(span.textContent, 10);
            if (val > 1) span.textContent = `${val - 1} kg`;
        });
    });

    document.querySelectorAll('[data-qty-plus]').forEach(btn => {
        btn.addEventListener('click', () => {
            const span = btn.parentElement.querySelector('[data-qty-val]');
            let val = parseInt(span.textContent, 10);
            span.textContent = `${val + 1} kg`;
        });
    });

    // 6. Đánh giá sao
    const stars = document.querySelectorAll('[data-star]');
    stars.forEach(star => {
        star.addEventListener('click', () => {
            const score = Number(star.dataset.star);
            stars.forEach(s => {
                s.style.color = Number(s.dataset.star) <= score ? '#9b6a1c' : '#cbd3cd';
            });
        });
    });

    // 7. Tự động chuyển ô nhập OTP
    const otpInputs = document.querySelectorAll('.otp-input');
    otpInputs.forEach((inp, idx) => {
        inp.addEventListener('input', () => {
            if (inp.value && idx < otpInputs.length - 1) otpInputs[idx + 1].focus();
        });
    });
});

function showToast(msg) {
    let toast = document.querySelector('.toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.className = 'toast';
        document.body.appendChild(toast);
    }
    toast.textContent = msg;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 2200);
}