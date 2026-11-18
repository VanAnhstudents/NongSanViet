/* =========================================================
   NÔNG SẢN VIỆT (FARMLINK) — INTERACTIVE PROTOTYPE LOGIC
========================================================= */

const SCREEN_GROUPS = [
    {
        group: '1 · Xác thực & Bảo mật',
        items: [
            { id: '01', label: '01 · Đăng nhập' },
            { id: '02', label: '02 · Đăng ký' },
            { id: '34', label: '34 · Xác thực số điện thoại' },
            { id: '35', label: '35 · Quên mật khẩu' }
        ]
    },
    {
        group: '2 · Người mua · Khám phá & Đặt hàng',
        items: [
            { id: '03', label: '03 · Khám phá' },
            { id: '04', label: '04 · Tìm kiếm và bộ lọc' },
            { id: '05', label: '05 · Chi tiết nông sản' },
            { id: '06', label: '06 · Giỏ hàng' },
            { id: '07', label: '07 · Thanh toán COD' },
            { id: '08', label: '08 · Đặt trước và đặt cọc' },
            { id: '33', label: '33 · Đặt cọc thành công' },
            { id: '09', label: '09 · Theo dõi đơn hàng' },
            { id: '10', label: '10 · Truy xuất nguồn gốc' }
        ]
    },
    {
        group: '3 · Hỗ trợ, Khiếu nại & Tin nhắn',
        items: [
            { id: '11', label: '11 · Gửi khiếu nại' },
            { id: '31', label: '31 · Phương án khiếu nại' },
            { id: '32', label: '32 · Xác nhận khiếu nại' },
            { id: '12', label: '12 · Đánh giá sau mua' },
            { id: '13', label: '13 · Thông báo' },
            { id: '14', label: '14 · Tin nhắn' },
            { id: '15', label: '15 · Thiếu hàng và hoàn cọc' }
        ]
    },
    {
        group: '4 · Kênh Nông dân',
        items: [
            { id: '16', label: '16 · Tổng quan nông trại' },
            { id: '17', label: '17 · Hồ sơ trang trại' },
            { id: '18', label: '18 · Sản phẩm và tồn kho' },
            { id: '19', label: '19 · Đăng bán · Bước 1' },
            { id: '20', label: '20 · Đăng bán · Bước 2' },
            { id: '21', label: '21 · Đăng bán · Bước 3' },
            { id: '22', label: '22 · Đơn hàng của nông dân' },
            { id: '23', label: '23 · Nhật ký trồng trọt' },
            { id: '24', label: '24 · Phản hồi khiếu nại' }
        ]
    },
    {
        group: '5 · Quản trị & Đồng kiểm',
        items: [
            { id: '25', label: '25 · Quản trị · Tổng quan' },
            { id: '26', label: '26 · Quản trị · Người dùng' },
            { id: '27', label: '27 · Quản trị · Sản phẩm' },
            { id: '28', label: '28 · Quản trị · Tranh chấp' },
            { id: '29', label: '29 · Quản trị · Danh mục' },
            { id: '30', label: '30 · Đồng kiểm và giao hàng' }
        ]
    }
];

const ORDERED_IDS = Array.from({ length: 35 }, (_, i) => String(i + 1).padStart(2, '0'));

const state = {
    currentScreen: '01',
    loginRole: 'buyer',
    preorderKg: 5,
    otpInterval: null,
    toastTimeout: null
};

const formatVND = (amount) => `${Number(amount).toLocaleString('vi-VN')}đ`;

/* ---------------------------------------------------------
   1. ĐIỀU HƯỚNG MÀN HÌNH & NGĂN KÉO
--------------------------------------------------------- */
function goToScreen(rawId) {
    const id = String(rawId).padStart(2, '0');
    const target = document.getElementById(`screen-${id}`);
    if (!target) return;

    document.querySelectorAll('.screen.active').forEach((el) => el.classList.remove('active'));
    target.classList.add('active');

    const body = target.querySelector('.screen-body');
    if (body) body.scrollTop = 0;

    state.currentScreen = id;
    document.getElementById('stageCurrentLabel').textContent = target.dataset.title || id;

    document.querySelectorAll('.drawer-item').forEach((btn) => {
        btn.classList.toggle('active', btn.dataset.screenId === id);
    });

    if (id === '34') startOtpCountdown();
    closeDrawer();
}

function renderDrawer(filter = '') {
    const container = document.getElementById('drawerList');
    const query = filter.trim().toLowerCase();
    container.innerHTML = '';

    SCREEN_GROUPS.forEach((grp) => {
        const matched = grp.items.filter((item) => item.label.toLowerCase().includes(query));
        if (!matched.length) return;

        const heading = document.createElement('p');
        heading.className = 'drawer-group-title';
        heading.textContent = grp.group;
        container.appendChild(heading);

        matched.forEach((item) => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = `drawer-item ${item.id === state.currentScreen ? 'active' : ''}`;
            btn.dataset.screenId = item.id;
            btn.innerHTML = `<span>${item.label}</span><span>›</span>`;
            btn.addEventListener('click', () => goToScreen(item.id));
            container.appendChild(btn);
        });
    });
}

function openDrawer() {
    document.getElementById('navDrawer').classList.add('open');
    document.getElementById('drawerBackdrop').classList.add('open');
}

function closeDrawer() {
    document.getElementById('navDrawer').classList.remove('open');
    document.getElementById('drawerBackdrop').classList.remove('open');
}

function showToast(message) {
    const banner = document.getElementById('toastBanner');
    banner.textContent = message;
    banner.classList.add('show');
    clearTimeout(state.toastTimeout);
    state.toastTimeout = setTimeout(() => banner.classList.remove('show'), 2600);
}

/* ---------------------------------------------------------
   2. XÁC THỰC & OTP (01, 02, 34)
--------------------------------------------------------- */
function initAuthAndOtp() {
    document.querySelectorAll('.pill-switch').forEach((group) => {
        group.querySelectorAll('.pill-opt').forEach((btn) => {
            btn.addEventListener('click', () => {
                group.querySelectorAll('.pill-opt').forEach((b) => b.classList.remove('active'));
                btn.classList.add('active');
                if (group.dataset.roleSwitch === 'login') {
                    state.loginRole = btn.dataset.role;
                }
            });
        });
    });

    document.getElementById('loginForm').addEventListener('submit', (e) => {
        e.preventDefault();
        if (state.loginRole === 'farmer') {
            showToast('Đăng nhập Kênh Nông dân · Vườn An');
            goToScreen('16');
        } else {
            showToast('Chào mừng bạn trở lại Nông Sản Việt');
            goToScreen('03');
        }
    });

    const otpInputs = Array.from(document.querySelectorAll('.otp-box'));
    otpInputs.forEach((input, idx) => {
        input.addEventListener('input', () => {
            input.value = input.value.replace(/\D/g, '').slice(0, 1);
            if (input.value && idx < otpInputs.length - 1) {
                otpInputs[idx + 1].focus();
            }
        });
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Backspace' && !input.value && idx > 0) {
                otpInputs[idx - 1].focus();
            }
        });
    });

    document.getElementById('btnResendOtp').addEventListener('click', () => {
        startOtpCountdown();
        showToast('Đã gửi lại mã OTP 6 số mới');
    });
}

function startOtpCountdown() {
    clearInterval(state.otpInterval);
    let seconds = 45;
    const label = document.getElementById('otpCountdown');
    label.textContent = '00:45';

    state.otpInterval = setInterval(() => {
        seconds -= 1;
        if (seconds <= 0) {
            clearInterval(state.otpInterval);
            label.textContent = '00:00';
            return;
        }
        label.textContent = `00:${String(seconds).padStart(2, '0')}`;
    }, 1000);
}

/* ---------------------------------------------------------
   3. KHÁM PHÁ, BỘ LỌC, GIỎ HÀNG & ĐẶT CỌC (03–10, 33)
--------------------------------------------------------- */
function initMarketplace() {
    // Lọc danh mục ở màn 03
    document.querySelectorAll('#exploreCatChips .chip').forEach((chip) => {
        chip.addEventListener('click', () => {
            document.querySelectorAll('#exploreCatChips .chip').forEach((c) => c.classList.remove('active'));
            chip.classList.add('active');
            const cat = chip.dataset.cat;
            document.querySelectorAll('#exploreProductGrid .product-card').forEach((card) => {
                card.style.display = cat === 'all' || card.dataset.cat === cat ? '' : 'none';
            });
        });
    });

    // Toggle chip bộ lọc ở màn 04
    document.querySelectorAll('#searchFilterChips .filter-pill').forEach((pill) => {
        pill.addEventListener('click', () => pill.classList.toggle('active'));
    });

    document.getElementById('btnApplyFilter').addEventListener('click', () => {
        const kw = document.getElementById('searchKeywordInput').value.trim().toLowerCase();
        const cards = document.querySelectorAll('#searchResultList .list-item-card');
        let visible = 0;
        cards.forEach((card) => {
            const match = !kw || card.textContent.toLowerCase().includes(kw);
            card.style.display = match ? 'flex' : 'none';
            if (match) visible += 1;
        });
        document.getElementById('searchResultCount').textContent = `${visible} sản phẩm phù hợp`;
        showToast(`Đã lọc được ${visible} sản phẩm phù hợp`);
    });

    // Tăng/giảm số kg trong Giỏ hàng (06) và đồng bộ sang COD (07)
    const recalcCart = () => {
        let subtotal = 0;
        let totalKg = 0;
        document.querySelectorAll('#cartItemsContainer .cart-card').forEach((card) => {
            const price = Number(card.dataset.price);
            const qty = Number(card.dataset.qty);
            subtotal += price * qty;
            totalKg += qty;
            card.querySelector('.step-val').textContent = `${qty} kg`;
        });

        const shipping = 20000;
        const total = subtotal + shipping;
        document.getElementById('cartSubtotal').textContent = formatVND(subtotal);
        document.getElementById('cartTotal').textContent = formatVND(total);
        document.getElementById('codSubtotal').textContent = formatVND(subtotal);
        document.getElementById('codTotal').textContent = formatVND(total);
        document.getElementById('btnPlaceCodOrder').textContent = `Đặt hàng · ${formatVND(total)}`;
        document.getElementById('cartBadgeCount').textContent = totalKg;
    };

    document.querySelectorAll('#cartItemsContainer .step-btn').forEach((btn) => {
        btn.addEventListener('click', () => {
            const card = btn.closest('.cart-card');
            const nextQty = Math.max(1, Number(card.dataset.qty) + Number(btn.dataset.step));
            card.dataset.qty = nextQty;
            recalcCart();
        });
    });

    // Tính cọc 30% tự động ở màn Đặt trước (08) & đồng bộ màn 33
    const updatePreorderCalc = () => {
        const unitPrice = 40000;
        const shipping = 20000;
        const goodsTotal = state.preorderKg * unitPrice;
        const deposit = Math.round(goodsTotal * 0.3);
        const grandTotal = goodsTotal + shipping;
        const remaining = grandTotal - deposit;

        document.getElementById('preorderKgLabel').textContent = `${state.preorderKg} kg`;
        document.getElementById('preorderLineCalc').textContent = `${state.preorderKg} kg × 40.000đ = ${formatVND(goodsTotal)}`;
        document.getElementById('preGoodsAmount').textContent = formatVND(goodsTotal);
        document.getElementById('preDepositAmount').textContent = formatVND(deposit);
        document.getElementById('preRemainingAmount').textContent = formatVND(remaining);
        document.getElementById('btnPayDeposit').textContent = `Thanh toán cọc · ${formatVND(deposit)}`;

        document.getElementById('successPreorderSummary').textContent = `Cà chua bi · ${state.preorderKg} kg`;
        document.getElementById('successDepositPaid').textContent = `Đã nhận cọc ${formatVND(deposit)}`;
        document.getElementById('successOrderTotal').textContent = `Tổng đơn: ${formatVND(grandTotal)} · Còn thanh toán khi nhận: ${formatVND(remaining)}`;
    };

    document.getElementById('preorderMinus').addEventListener('click', () => {
        state.preorderKg = Math.max(1, state.preorderKg - 1);
        updatePreorderCalc();
    });

    document.getElementById('preorderPlus').addEventListener('click', () => {
        state.preorderKg += 1;
        updatePreorderCalc();
    });

    // Hủy đơn trước hạn ở màn 09
    document.getElementById('btnCancelPreorder').addEventListener('click', () => {
        const banner = document.getElementById('orderStatusBanner');
        banner.innerHTML = `
      <strong>Đã hủy trước hạn · Hoàn 100% cọc</strong>
      <p class="muted-p">Đã hoàn 60.000đ về VNPay mô phỏng.</p>
    `;
        showToast('Đã hủy đơn trước hạn và hoàn 100% tiền cọc');
    });

    // Modal Quét QR lô hàng ở màn 10
    const qrModal = document.getElementById('qrModal');
    document.getElementById('btnOpenQrModal').addEventListener('click', () => qrModal.classList.add('open'));
    document.getElementById('btnCloseQrModal').addEventListener('click', () => qrModal.classList.remove('open'));
    document.getElementById('btnLookupBatch').addEventListener('click', () => {
        const code = document.getElementById('batchCodeInput').value.trim() || 'AN-181126';
        qrModal.classList.remove('open');
        showToast(`Đã xác thực nguồn gốc lô ${code} · Vườn An`);
    });
}

/* ---------------------------------------------------------
   4. HỖ TRỢ, ĐÁNH GIÁ SAO, THÔNG BÁO & CHAT (11–14)
--------------------------------------------------------- */
function initSupportAndChat() {
    document.getElementById('btnAttachEvidence').addEventListener('click', () => {
        document.getElementById('evidenceStatusNote').textContent = '✓ Đã đính kèm 2 ảnh minh chứng (1,4 MB)';
        showToast('Đã thêm 2 ảnh minh chứng');
    });

    const ratingTexts = ['Rất tệ', 'Chưa hài lòng', 'Bình thường', 'Hài lòng', 'Rất hài lòng'];
    document.querySelectorAll('#starRatingRow .star-btn').forEach((btn) => {
        btn.addEventListener('click', () => {
            const score = Number(btn.dataset.star);
            document.querySelectorAll('#starRatingRow .star-btn').forEach((s) => {
                s.classList.toggle('active', Number(s.dataset.star) <= score);
            });
            document.getElementById('starRatingText').textContent = ratingTexts[score - 1];
        });
    });

    document.getElementById('btnSubmitReview').addEventListener('click', () => {
        showToast('Cảm ơn bạn! Đánh giá đã được ghi nhận');
    });

    document.querySelectorAll('#notifFilterTabs .u-tab').forEach((tab) => {
        tab.addEventListener('click', () => {
            document.querySelectorAll('#notifFilterTabs .u-tab').forEach((t) => t.classList.remove('active'));
            tab.classList.add('active');
            const onlyUnread = tab.dataset.notif === 'unread';
            document.querySelectorAll('#notifList > div').forEach((item) => {
                item.style.display = !onlyUnread || item.dataset.unread === 'true' ? '' : 'none';
            });
        });
    });

    document.getElementById('chatForm').addEventListener('submit', (e) => {
        e.preventDefault();
        const input = document.getElementById('chatInput');
        const text = input.value.trim();
        if (!text) return;

        const feed = document.getElementById('chatFeed');
        const mine = document.createElement('div');
        mine.className = 'bubble bubble-me';
        mine.textContent = text;
        feed.appendChild(mine);
        input.value = '';
        feed.scrollTop = feed.scrollHeight;

        setTimeout(() => {
            const reply = document.createElement('div');
            reply.className = 'bubble bubble-them';
            reply.textContent = 'Vườn An đã nhận tin nhắn của bạn và sẽ chuẩn bị hàng tươi nhất nhé!';
            feed.appendChild(reply);
            feed.scrollTop = feed.scrollHeight;
        }, 650);
    });
}

/* ---------------------------------------------------------
   5. KÊNH NÔNG DÂN & QUẢN TRỊ (16–30)
--------------------------------------------------------- */
function initFarmerAndAdmin() {
    // Đồng bộ dữ liệu Đăng bán 3 bước (19 -> 20 -> 21 -> 18)
    document.getElementById('btnToPublishStep3').addEventListener('click', () => {
        const name = document.getElementById('pubNameInput').value.trim() || 'Cà chua bi Đà Lạt';
        const cat = document.getElementById('pubCatInput').value;
        const price = document.getElementById('pubPriceInput').value.trim() || '40.000đ';
        const qty = document.getElementById('pubQtyInput').value || '200';
        const harvest = document.getElementById('pubHarvestInput').value || '18/11/2026';
        const cutoff = document.getElementById('pubCutoffInput').value || '17/11/2026 · 08:00';

        document.getElementById('reviewPubName').textContent = name;
        document.getElementById('reviewPubMeta').textContent = `${cat} · ${price} / kg`;
        document.getElementById('reviewPubHarvest').textContent = `Sản lượng: ${qty} kg · Cọc: 30%`;
        document.getElementById('reviewPubDates').textContent = `Thu hoạch dự kiến: ${harvest} · Chốt đơn: ${cutoff}`;
    });

    document.getElementById('btnPublishProduct').addEventListener('click', () => {
        const name = document.getElementById('pubNameInput').value.trim() || 'Nông sản mới';
        const price = document.getElementById('pubPriceInput').value.trim() || '35.000đ';
        const qty = document.getElementById('pubQtyInput').value || '100';

        const card = document.createElement('div');
        card.className = 'card-white';
        card.innerHTML = `
      <div class="card-row-between">
        <strong>${name}</strong>
        <span class="verified-tag">Mới đăng</span>
      </div>
      <p class="muted-p">${price} / kg · Đặt trước</p>
      <p class="muted-p">Còn nhận đặt: ${qty} kg</p>
    `;
        document.getElementById('farmerProductList').prepend(card);
        showToast(`Đã đăng bán "${name}" thành công`);
    });

    // Xác nhận / Từ chối đơn hàng của nông dân (22)
    document.querySelectorAll('[data-order-action]').forEach((btn) => {
        btn.addEventListener('click', () => {
            const card = btn.closest('[data-order-card]');
            const accepted = btn.dataset.orderAction === 'accept';
            card.querySelector('.dual-btn-row').innerHTML = `
        <span class="${accepted ? 'verified-tag' : 'muted-p'}">
          ${accepted ? '✓ Đã xác nhận đơn hàng' : '✕ Đã từ chối đơn hàng'}
        </span>
      `;
            showToast(accepted ? 'Đã xác nhận đơn hàng cho khách' : 'Đã từ chối đơn hàng');
        });
    });

    // Lưu nhật ký trồng trọt (23) -> nối thẳng vào timeline Truy xuất (10)
    document.getElementById('btnSaveFarmLog').addEventListener('click', () => {
        const date = document.getElementById('logDateInput').value.trim() || '11/11/2026';
        const activity = document.getElementById('logActivityInput').value.trim() || 'Chăm sóc vườn';
        const note = document.getElementById('logNoteInput').value.trim();

        const li = document.createElement('li');
        li.className = 'done';
        li.innerHTML = `
      <span class="tl-dot"></span>
      <div>
        <strong>${activity}</strong>
        <p>${date} · ${note}</p>
      </div>
    `;
        document.getElementById('traceabilityTimeline').appendChild(li);
        showToast('Đã lưu nhật ký và cập nhật mã truy xuất lô AN-181126');
        goToScreen('10');
    });

    // Quản trị: duyệt nông trại & khóa tài khoản (26)
    document.getElementById('btnApproveFarm').addEventListener('click', (e) => {
        e.target.textContent = '✓ Đã duyệt';
        e.target.disabled = true;
        showToast('Đã xác minh hồ sơ Nông trại Bình Minh');
    });

    document.getElementById('btnToggleLockUser').addEventListener('click', (e) => {
        const locked = e.target.dataset.locked === 'true';
        e.target.dataset.locked = String(!locked);
        e.target.textContent = locked ? 'Khóa tài khoản' : 'Mở khóa tài khoản';
        document.getElementById('buyerAccountStatus').textContent = locked
            ? 'Trạng thái: Đang hoạt động'
            : 'Trạng thái: Đã tạm khóa';
        showToast(locked ? 'Đã mở khóa tài khoản người mua' : 'Đã khóa tài khoản người mua');
    });

    // Quản trị: gỡ sản phẩm vi phạm (27)
    document.getElementById('btnRemoveReportedProduct').addEventListener('click', (e) => {
        e.target.textContent = '✓ Đã gỡ sản phẩm vi phạm';
        e.target.disabled = true;
        showToast('Đã gỡ sản phẩm và gửi hướng dẫn cho người bán');
    });

    // Quản trị: thêm danh mục mới (29)
    document.getElementById('btnAddCategory').addEventListener('click', () => {
        const name = window.prompt('Nhập tên danh mục nông sản mới:', 'Rau gia vị & Thảo mộc');
        if (!name || !name.trim()) return;
        const item = document.createElement('div');
        item.className = 'card-white';
        item.innerHTML = `
      <div class="card-row-between">
        <strong>${name.trim()}</strong>
        <span class="verified-tag">Mới</span>
      </div>
      <p class="muted-p">00 sản phẩm · Đang hiển thị</p>
    `;
        document.getElementById('adminCategoryList').prepend(item);
        showToast(`Đã thêm danh mục "${name.trim()}"`);
    });

    // Đồng kiểm & giao hàng (30)
    document.getElementById('btnConfirmDelivery').addEventListener('click', (e) => {
        e.target.textContent = '✓ Đã xác nhận giao & thu 70.000đ COD';
        e.target.disabled = true;
        showToast('Hoàn tất đồng kiểm · Bắt đầu mốc khiếu nại 24h');
    });
}

/* ---------------------------------------------------------
   KHỞI TẠO TOÀN BỘ ỨNG DỤNG
--------------------------------------------------------- */
document.addEventListener('DOMContentLoaded', () => {
    renderDrawer();

    // Điều hướng toàn cục qua data-go và data-toast
    document.body.addEventListener('click', (e) => {
        const toastTrigger = e.target.closest('[data-toast]');
        if (toastTrigger) showToast(toastTrigger.dataset.toast);

        const goTrigger = e.target.closest('[data-go]');
        if (goTrigger) goToScreen(goTrigger.dataset.go);

        if (e.target.closest('[data-open-drawer]')) openDrawer();
    });

    document.getElementById('btnOpenDrawer').addEventListener('click', openDrawer);
    document.getElementById('btnCloseDrawer').addEventListener('click', closeDrawer);
    document.getElementById('drawerBackdrop').addEventListener('click', closeDrawer);
    document.getElementById('drawerSearch').addEventListener('input', (e) => renderDrawer(e.target.value));

    document.getElementById('btnPrevScreen').addEventListener('click', () => {
        const idx = ORDERED_IDS.indexOf(state.currentScreen);
        goToScreen(ORDERED_IDS[(idx - 1 + ORDERED_IDS.length) % ORDERED_IDS.length]);
    });

    document.getElementById('btnNextScreen').addEventListener('click', () => {
        const idx = ORDERED_IDS.indexOf(state.currentScreen);
        goToScreen(ORDERED_IDS[(idx + 1) % ORDERED_IDS.length]);
    });

    initAuthAndOtp();
    initMarketplace();
    initSupportAndChat();
    initFarmerAndAdmin();
});