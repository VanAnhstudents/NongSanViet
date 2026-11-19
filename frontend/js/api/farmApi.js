const API_BASE_URL = window.APP_CONFIG?.API_BASE_URL || 'http://localhost:8080/api/v1';

function getAuthHeaders() {
  const token = localStorage.getItem('accessToken') || localStorage.getItem('token') || '';
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  };
}

export const farmApi = {
  /**
   * GET /api/v1/farmer/farm/extended-info
   * Trả về: { farmId, farmName, certificateUrl, certificateStatus }
   */
  async getExtendedInfo() {
    const response = await fetch(`${API_BASE_URL}/farmer/farm/extended-info`, {
      method: 'GET',
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      const err = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Không thể tải thông tin mở rộng của trang trại');
    }

    return response.json();
  },

  /**
   * PUT /api/v1/farmer/farm/extended-info
   * Body JSON: { certificateUrl: string | null }
   */
  async updateExtendedInfo(payload) {
    const response = await fetch(`${API_BASE_URL}/farmer/farm/extended-info`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      const err = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Lưu thông tin mở rộng thất bại');
    }

    return response.json();
  }
};