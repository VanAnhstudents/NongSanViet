import { farmApi } from '../api/farmApi.js';

const CERT_STATUS_LABELS = {
  PENDING: 'Chứng nhận · Đang chờ quản trị xét duyệt',
  APPROVED: 'VietGAP · Đã được quản trị xác minh',
  VERIFIED: 'VietGAP · Đã được quản trị xác minh',
  REJECTED: 'Chứng nhận · Bị từ chối, vui lòng cập nhật lại',
  NONE: 'Chưa cập nhật chứng nhận chất lượng'
};

document.addEventListener('DOMContentLoaded', async () => {
  const menuToggle = document.getElementById('menuToggle');
  const sidebar = document.getElementById('sidebar');
  const form = document.getElementById('farmProfileForm');
  const btnSave = document.getElementById('btnSaveProfile');
  const toast = document.getElementById('toast');

  const farmNameInput = document.getElementById('farmName');
  const farmAddressInput = document.getElementById('farmAddress');
  const farmPhoneInput = document.getElementById('farmPhone');
  const certUrlInput = document.getElementById('certificateUrl');
  const charCount = document.getElementById('charCount');

  const errorFarmName = document.getElementById('errorFarmName');
  const errorFarmAddress = document.getElementById('errorFarmAddress');
  const errorFarmPhone = document.getElementById('errorFarmPhone');
  const errorCertUrl = document.getElementById('errorCertUrl');

  const cardFarmName = document.getElementById('cardFarmName');
  const cardCertStatus = document.getElementById('cardCertStatus');
  const cardCertLink = document.getElementById('cardCertLink');
  const sidebarFarmName = document.getElementById('sidebarFarmName');
  const topbarFarmStatus = document.getElementById('topbarFarmStatus');

  // Toggle Sidebar trên Mobile
  menuToggle?.addEventListener('click', () => {
    sidebar.classList.toggle('open');
  });

  document.addEventListener('click', (e) => {
    if (window.innerWidth <= 900 && sidebar?.classList.contains('open')) {
      if (!sidebar.contains(e.target) && !menuToggle.contains(e.target)) {
        sidebar.classList.remove('open');
      }
    }
  });

  // Đếm ký tự cho certificateUrl
  function updateCharCount() {
    if (!certUrlInput || !charCount) return;
    const len = certUrlInput.value.length;
    charCount.textContent = `${len}/500`;
  }
  certUrlInput?.addEventListener('input', updateCharCount);

  function showToast(message, isError = false) {
    if (!toast) return;
    toast.textContent = message;
    toast.style.backgroundColor = isError ? '#b3261e' : '#1c3a28';
    toast.hidden = false;
    setTimeout(() => {
      toast.hidden = true;
    }, 3000);
  }

  function renderExtendedInfo(data) {
    if (!data) return;

    if (data.farmName) {
      farmNameInput.value = data.farmName;
      cardFarmName.innerHTML = `${data.farmName} <span class="verified-badge">✓ Đã xác minh</span>`;
      sidebarFarmName.textContent = data.farmName.toUpperCase();
      topbarFarmStatus.textContent = `${data.farmName} · Đã xác minh`;
    }

    const url = data.certificateUrl || '';
    certUrlInput.value = url;
    updateCharCount();

    if (url) {
      cardCertLink.href = url;
      cardCertLink.hidden = false;
    } else {
      cardCertLink.hidden = true;
    }

    if (data.certificateStatus && CERT_STATUS_LABELS[data.certificateStatus]) {
      cardCertStatus.textContent = CERT_STATUS_LABELS[data.certificateStatus];
    } else if (!url) {
      cardCertStatus.textContent = CERT_STATUS_LABELS.NONE;
    }
  }

  // Tải dữ liệu ban đầu (GET)
  try {
    const responseData = await farmApi.getExtendedInfo();
    renderExtendedInfo(responseData);
  } catch (error) {
    console.warn('Không thể tải thông tin từ server:', error.message);
  }

  // Kiểm tra ràng buộc khi submit
  function validateForm() {
    let isValid = true;

    // Reset lỗi
    errorFarmName.hidden = true;
    errorFarmAddress.hidden = true;
    errorFarmPhone.hidden = true;
    errorCertUrl.hidden = true;
    farmNameInput.removeAttribute('aria-invalid');
    farmAddressInput.removeAttribute('aria-invalid');
    farmPhoneInput.removeAttribute('aria-invalid');
    certUrlInput.removeAttribute('aria-invalid');

    // Ràng buộc: Tên trang trại không được để trống
    if (!farmNameInput.value.trim()) {
      errorFarmName.hidden = false;
      farmNameInput.setAttribute('aria-invalid', 'true');
      isValid = false;
    }

    // Ràng buộc: Địa chỉ không được để trống
    if (!farmAddressInput.value.trim()) {
      errorFarmAddress.hidden = false;
      farmAddressInput.setAttribute('aria-invalid', 'true');
      isValid = false;
    }

    // Ràng buộc: Số điện thoại không được để trống
    if (!farmPhoneInput.value.trim()) {
      errorFarmPhone.hidden = false;
      farmPhoneInput.setAttribute('aria-invalid', 'true');
      isValid = false;
    }

    // Ràng buộc: URL chứng nhận tối đa 500 ký tự (Dù là tùy chọn nhưng nếu nhập thì không được quá giới hạn)
    const rawUrl = certUrlInput.value.trim();
    if (rawUrl.length > 500) {
      errorCertUrl.hidden = false;
      certUrlInput.setAttribute('aria-invalid', 'true');
      isValid = false;
    }

    return isValid;
  }

  // Xử lý Submit Form (PUT)
  form?.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      showToast('Vui lòng kiểm tra lại các trường bắt buộc.', true);
      return;
    }

    const rawUrl = certUrlInput.value.trim();
    const payload = {
      certificateUrl: rawUrl === '' ? null : rawUrl
    };

    btnSave.disabled = true;
    btnSave.textContent = 'Đang lưu...';

    try {
      const updatedData = await farmApi.updateExtendedInfo(payload);
      renderExtendedInfo(updatedData);
      showToast('Đã lưu thông tin trang trại thành công!');
    } catch (error) {
      showToast(error.message || 'Có lỗi xảy ra khi lưu thông tin', true);
    } finally {
      btnSave.disabled = false;
      btnSave.textContent = 'Lưu thông tin trang trại';
    }
  });
});