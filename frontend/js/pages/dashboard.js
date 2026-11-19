(function () {
  "use strict";

  const STATUS_LABEL = { selling: "Đang bán", pending: "Chờ duyệt", out: "Hết hàng" };
  const ORDER_LABEL = { new: "Mới", shipping: "Đang giao", done: "Hoàn tất" };

  // Dữ liệu mẫu — thay bằng dữ liệu từ API khi có backend
  const products = [
    { id: 1, name: "Cà chua bi hữu cơ", unit: "kg", price: 45000, stock: 120, status: "selling", icon: "🍅", tint: "#fbe1d8" },
    { id: 2, name: "Cam sành chín cây", unit: "kg", price: 38000, stock: 340, status: "selling", icon: "🍊", tint: "#fde8c4" },
    { id: 3, name: "Rau cải xanh VietGAP", unit: "bó", price: 22000, stock: 0, status: "out", icon: "🥬", tint: "#dcebc8" },
    { id: 4, name: "Xoài cát Hòa Lộc", unit: "kg", price: 65000, stock: 80, status: "pending", icon: "🥭", tint: "#fbf0b8" },
  ];

  const orders = [
    { id: "DH1024", buyer: "Nguyễn Thị Mai", items: "Cà chua bi × 3 kg", total: 135000, status: "new" },
    { id: "DH1023", buyer: "Trần Quốc Huy", items: "Cam sành × 5 kg", total: 190000, status: "new" },
    { id: "DH1021", buyer: "Lê Hoàng Anh", items: "Cà chua bi × 2 kg, Cam sành × 2 kg", total: 166000, status: "shipping" },
    { id: "DH1018", buyer: "Phạm Thu Hà", items: "Cam sành × 10 kg", total: 380000, status: "done" },
  ];

  let activeStatus = "all";

  const money = (n) => n.toLocaleString("vi-VN") + "đ";
  const $ = (id) => document.getElementById(id);

  function renderStats() {
    $("statProducts").textContent = products.filter((p) => p.status === "selling").length;
    $("statNewOrders").textContent = orders.filter((o) => o.status === "new").length;
    $("statRevenue").textContent = money(orders.reduce((sum, o) => sum + o.total, 0));
  }

  function renderProducts() {
    const list = products.filter((p) => activeStatus === "all" || p.status === activeStatus);
    $("productList").innerHTML = list.length
      ? list.map((p) => `
        <li class="row">
          <div class="row__icon" style="background:${p.tint}" aria-hidden="true">${p.icon}</div>
          <div class="row__main">
            <h3 class="row__title">${p.name}</h3>
            <p class="row__meta">Tồn kho: ${p.stock} ${p.unit}</p>
          </div>
          <div class="row__side">
            <span class="badge badge--${p.status}">${STATUS_LABEL[p.status]}</span>
            <span class="row__price">${money(p.price)} / ${p.unit}</span>
          </div>
        </li>`).join("")
      : `<li class="empty">Chưa có sản phẩm nào ở trạng thái này.</li>`;
  }

  function renderOrders() {
    $("orderList").innerHTML = orders.map((o) => {
      const action = o.status === "new" ? "Xác nhận giao" : o.status === "shipping" ? "Hoàn tất" : "";
      return `
        <li class="row">
          <div class="row__main">
            <h3 class="row__title">${o.id} · ${o.buyer}</h3>
            <p class="row__meta">${o.items}</p>
          </div>
          <div class="row__side">
            <span class="badge badge--${o.status}">${ORDER_LABEL[o.status]}</span>
            <span class="row__price">${money(o.total)}</span>
            ${action ? `<button type="button" class="btn btn--green btn--sm" data-order="${o.id}">${action}</button>` : ""}
          </div>
        </li>`;
    }).join("");
  }

  // Lọc sản phẩm theo trạng thái
  $("productChips").addEventListener("click", (e) => {
    const btn = e.target.closest(".chip");
    if (!btn) return;
    activeStatus = btn.dataset.status;
    document.querySelectorAll("#productChips .chip").forEach((c) => c.classList.remove("is-active"));
    btn.classList.add("is-active");
    renderProducts();
  });

  // Chuyển trạng thái đơn hàng: Mới → Đang giao → Hoàn tất
  $("orderList").addEventListener("click", (e) => {
    const btn = e.target.closest("[data-order]");
    if (!btn) return;
    const order = orders.find((o) => o.id === btn.dataset.order);
    if (!order) return;
    order.status = order.status === "new" ? "shipping" : "done";
    renderOrders();
    renderStats();
  });

  // Menu 3 gạch (mobile)
  const menuBtn = $("menuBtn");
  const mainNav = $("mainNav");

  function setMenu(open) {
    mainNav.classList.toggle("is-open", open);
    menuBtn.setAttribute("aria-expanded", String(open));
    menuBtn.setAttribute("aria-label", open ? "Đóng menu" : "Mở menu");
  }

  menuBtn.addEventListener("click", () => setMenu(!mainNav.classList.contains("is-open")));
  mainNav.addEventListener("click", (e) => { if (e.target.closest("a")) setMenu(false); });
  document.addEventListener("click", (e) => { if (!e.target.closest(".header")) setMenu(false); });
  document.addEventListener("keydown", (e) => { if (e.key === "Escape") setMenu(false); });
  window.matchMedia("(min-width: 768px)").addEventListener("change", () => setMenu(false));

  // Tên người dùng đã đăng nhập
  try {
    const raw = localStorage.getItem("nsv_user_info");
    if (raw) {
      const user = JSON.parse(raw);
      if (user && user.name) {
        $("userLink").textContent = user.name;
        $("farmerName").textContent = user.name;
      }
    }
  } catch (_) {}

  renderStats();
  renderProducts();
  renderOrders();
})();