# Kiến trúc Hệ thống Modular Monolith — Nông Sản Việt (FarmDirect)

- **Mã truy vết:** NFR-06
- **Công nghệ nền tảng:** Java 21, Spring Boot, Spring Security (JWT), Spring Data JPA, PostgreSQL
- **Test case kiểm chứng:** TC-ARCH-01

---

## 1. Mục tiêu kiến trúc

Hệ thống Backend **Nông Sản Việt** được tổ chức theo mô hình **Modular Monolith** (Nguyên khối mô-đun hóa):
1. **Triển khai đơn giản:** Đóng gói thành một artifact duy nhất, chia sẻ chung connection pool và transaction ACID của PostgreSQL trong giai đoạn đầu.
2. **Tách biệt ranh giới nghiệp vụ (Bounded Contexts):** Mỗi nghiệp vụ cốt lõi được đóng gói thành một module độc lập theo chiều dọc (vertical slice), không truy cập chéo trực tiếp vào `entity` hoặc `repository` nội bộ của module khác.
3. **Sẵn sàng mở rộng (Microservices-ready):** Khi tải hệ thống tăng cao (ví dụ tách riêng dịch vụ `order` hoặc `trace`), các module có thể tách thành microservice độc lập chỉ bằng cách thay thế implementation của tầng `api` (từ gọi hàm nội bộ sang REST/gRPC/Message Broker) mà không phải viết lại logic nghiệp vụ.

---

## 2. Sơ đồ phân rã Module & Cấu trúc Package

```text
com.nsv.backend
├── common/                  # Shared Kernel: Security, Exception, Config, Event/Audit Ports
│   ├── config/
│   ├── contract/            # Cổng giao tiếp hạ tầng dùng chung (Notification, Audit, Event)
│   ├── exception/
│   └── security/
├── user/                    # Module 1: Tài khoản, Xác thực, Trang trại, Địa chỉ, Chứng nhận
│   ├── api/                 # Public Façade & DTO giao tiếp liên module
│   ├── controller/
│   ├── dto/
│   ├── entity/              # Nội bộ module (users, provinces, farms, addresses, farm_certificates)
│   ├── repository/          # Nội bộ module
│   └── service/             # Nội bộ module
├── product/                 # Module 2: Danh mục, Sản phẩm, Hình ảnh, Combo ưu đãi
│   ├── api/
│   ├── controller/
│   ├── dto/
│   ├── entity/              # Nội bộ module (categories, products, product_images, combos, combo_items)
│   ├── repository/
│   └── service/
├── inventory/               # Module 3: Lô hàng thu hoạch, Tồn kho, Khóa bi quan giữ hàng
│   ├── api/
│   ├── dto/
│   ├── entity/              # Nội bộ module (batches)
│   ├── repository/
│   └── service/
├── trace/                   # Module 4: Truy xuất nguồn gốc QR (UUID) & Nhật ký canh tác
│   ├── api/
│   ├── controller/
│   ├── dto/
│   ├── entity/              # Nội bộ module (farming_logs)
│   ├── repository/
│   └── service/
├── order/                   # Module 5: Giỏ hàng, Đơn hàng, Đồng kiểm, Thanh toán & Hoàn tiền
│   ├── api/
│   ├── controller/
│   ├── dto/
│   ├── entity/              # Nội bộ module (cart_items, orders, order_items, order_addresses, payments, refunds, order_co_inspections)
│   ├── repository/
│   └── service/
└── dispute/                 # Module 6: Khiếu nại, Phân xử tranh chấp & Lưu trữ minh chứng bền vững
    ├── controller/
    ├── dto/
    ├── entity/              # Nội bộ module (complaints, complaint_evidences)
    ├── repository/
    ├── service/
    └── storage/
```
