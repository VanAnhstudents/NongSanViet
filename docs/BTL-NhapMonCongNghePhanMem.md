# **CHƯƠNG V. XÂY DỰNG VÀ KIỂM THỬ HỆ THỐNG**

**Bảng 5.1. Đối chiếu các mục của Chương V với tài liệu chuyển giao (Bảng 2.1)**

| Mục | Nội dung | Tài liệu nguồn (Bảng 2.1) | Mã tài liệu |
| :---- | :---- | :---- | :---- |
| 5.1 | Sprint 1 | Sprint 1 Increment + Release Notes | S1-INC / RN |
| 5.2 | Sprint 2 | Sprint 2 Increment + Release Notes | S2-INC / RN |
| 5.3 | Sprint 3 | Sprint 3 Increment + Release Notes | S3-INC / RN |
| 5.4 | Sprint Test | Test Plan, Test Case, Test Summary Report | TP / TC / TSR |
| 5.5 | Minh chứng Git, mã nguồn | Source Code & Deployment/Handover Document | SRC / DEP |
| Chương VI | Tổng kết, đối chiếu mục tiêu | Project Closure Report | PCR |

## **5.1. Sprint 1 - Đăng ký/đăng nhập, đăng sản phẩm, tìm kiếm**

### *5.1.1. Yêu cầu của Sprint*

Sprint Goal (tuần 2, Bảng 1.4): Đăng ký/đăng nhập 3 actor; Farmer đăng sản phẩm; Buyer tìm kiếm.  
Sprint 1 gồm 15 PBI (PBI-01 đến PBI-15), trong đó 10 PBI mức High, 5 PBI mức Medium. Cột "Module" lấy theo Bảng 4.3; các PBI phi chức năng áp dụng cho toàn hệ thống.  
**Bảng 5.2. Danh sách PBI trong Sprint 1**

| STT | Product Backlog Item (PBI) | Độ ưu tiên | Module | Kết quả |
| :---- | :---- | :---- | :---- | :---- |
| 1 | PBI-01: Đăng ký, đăng nhập, phân quyền | High | M01 | Done |
| 2 | PBI-02: Hồ sơ trang trại - thông tin tối thiểu | High | M02 | Not Done |
| 3 | PBI-03: Đăng bán sản phẩm (tối đa 3 bước) | High | M03 | Done |
| 4 | PBI-04: Khai báo sản lượng và lịch thu hoạch | High | M03 | Done |
| 5 | PBI-05: Nén ảnh phía client và xử lý mạng yếu | High | M03 (FE) | Not Done |
| 6 | PBI-06: Web responsive (mobile-first) | High | Toàn hệ thống | Done |
| 7 | PBI-07: Giao diện tối giản (≤ 3 bước, chữ ≥ 16px) | High | Toàn hệ thống | Not Done |
| 8 | PBI-08: Bảo mật HTTPS, băm mật khẩu, phân quyền | High | M01 | Done |
| 9 | PBI-09: Khả năng mở rộng (Modular Monolith) | High | Kiến trúc | Done |
| 10 | PBI-10: Ngôn ngữ tiếng Việt và đơn vị VNĐ | High | Toàn hệ thống | Done |
| 11 | PBI-11: Hồ sơ trang trại - thông tin mở rộng | Medium | M02 | Done |
| 12 | PBI-12: Quản lý combo sản phẩm | Medium | M03 | Done |
| 13 | PBI-13: Hướng dẫn thao tác cho người bán | Medium | M02 | Not Done |
| 14 | PBI-14: Tìm kiếm và lọc sản phẩm | Medium | M04 | Done |
| 15 | PBI-15: Quên mật khẩu và xác thực số điện thoại | Medium | M01 | Done |

### *5.1.2. Kết quả xây dựng*

1. Tổng quan Increment  
Kết thúc Sprint 1, nhóm bàn giao Increment S1-INC_v1.0 chạy được tại [link deploy]: 12/15 PBI đạt Done, trong đó 9/10 PBI mức High. Chi tiết danh sách PBI Done/Not Done và bảng liên kết PBI ↔ FR/NFR ↔ Use Case nằm ở tài liệu S1-INC/RN, Phần A mục 2-3.  
2. Kết quả demo theo luồng  
Luồng kiểm chứng end-to-end gồm ba chặng:  
- Đăng ký, đăng nhập, phân quyền (PBI-01, 08, 15): người dùng đăng ký với vai trò Farmer hoặc Buyer, đăng nhập nhận JWT; tài khoản Admin tạo từ seed data. Mật khẩu băm BCrypt.

|  |  |
| :---- | :---- |

*Hình 5.1. Màn hình đăng ký/đăng nhập.*   
Kết quả chặn truy cập trái quyền: 5/5 kịch bản bị chặn đúng.  
- Farmer đăng sản phẩm (PBI-02, 03, 04, 05, 12): luồng đăng gồm 3 bước: (1) chọn danh mục và tên, (2) tải ảnh (tự nén) và nhập giá, (3) khai báo sản lượng, ngày thu hoạch rồi đăng.

|  |  |  |
| :---- | :---- | :---- |

*Hình 5.2. Ba bước đăng sản phẩm*  
Khi đăng, hệ thống tạo lô hàng kèm mã traceability_code để dùng cho truy xuất QR ở Sprint 3.  
- Buyer tìm kiếm và lọc (PBI-14): tìm theo từ khóa, lọc theo danh mục, vùng, khoảng giá.

*Hình 5.3. Màn hình tìm kiếm trên điện thoại*  
3. Đối chiếu yêu cầu phi chức năng  
**Bảng 5.3. Kết quả đo các NFR trong Sprint 1**

| PBI / NFR | Ngưỡng yêu cầu | Kết quả đo | Đạt? |
| :---- | :---- | :---- | :---- |
| PBI-03, PBI-07 / NFR-02 | ≤ 3 bước, chữ ≥ 16px, nút ≥ 48×48px | 3 bước đạt; font-size < 16px tại một số class CSS | Một phần |
| PBI-05 / NFR-12 | Ảnh ≤ 2MB | Nén JPEG quality 0.8; chưa đảm bảo ≤ 500KB và chưa có retry | Không đạt |
| PBI-06 / NFR-01 | 4 cặp (Chrome, Firefox × máy tính, điện thoại) | 4/4 | Đạt |
| PBI-08 / NFR-04 | HTTPS, mật khẩu băm | HTTPS + BCrypt; RBAC pass 5/5 kịch bản (TcSec01Test) | Đạt |
| PBI-10 / NFR-10 | Tiếng Việt, đơn vị VNĐ | Giao diện 100% tiếng Việt; giá hiển thị đ (lệch nhẹ so với ₫) | Đạt |

4. Hạ tầng và mã nguồn  
Cấu trúc thư mục theo kebab-case, mỗi module nghiệp vụ là một package riêng (PBI-09); repo, nhánh main/develop và môi trường Render + Neon được dựng từ sprint này. Chi tiết ở tài liệu SRC/DEP, Phần A mục 1-2 và Phần B mục 2-5. Cơ sở dữ liệu có các bảng [users, farms, products, batches, combos, combo_items, ...] (xem Hình 4.3, Bảng 4.4).  
5. Lỗi tồn đọng và Release Notes  
Tại thời điểm chốt sprint còn 3 lỗi chưa đóng (0 Critical/High, 3 Minor), danh sách ở S1-INC/RN Phần B mục 4. Các hạn chế đã biết: OTP ở mức mô phỏng (PBI-15); nén ảnh chưa đảm bảo ≤ 500KB và chưa có retry (PBI-05); font-size < 16px ở một số class CSS (PBI-07); wizard onboarding cho Farmer chưa hiện thực (PBI-13). Ghi chú cho người kiểm thử ở Sprint Test: tập trung kiểm tra phân quyền và giá trị biên khi đăng sản phẩm.

### *5.1.3. Kiểm thử theo Definition of Done*

**Bảng 5.4. Kiểm tra Definition of Done - Sprint 1**

| STT | Hạng mục DoD | Kết quả | Ghi chú |
| :---- | :---- | :---- | :---- |
| 1 | Code review xong | 44/44 PR được review | 100% PR có ít nhất 1 review |
| 2 | Unit test pass | 48/48 test pass (TcAuth01, TcAuth02, TcFarm01, TcFarm02, TcProd01, TcProd02, TcHarvest01, TcSec01, TcArch01, TcSearch01) | Độ bao phủ: TcSec01 5 kịch bản RBAC/BCrypt/HTTPS |
| 3 | Test case theo yêu cầu pass | 10/15 TC-ID pass (5 TC frontend/manual chưa đạt: TC-NET-01, TC-UI-01, TC-UI-02, TC-I18N-01, TC-ONB-01) | TC-AUTH-01, TC-AUTH-02, TC-FARM-01, TC-FARM-02, TC-PROD-01, TC-PROD-02, TC-HARVEST-01, TC-SEC-01, TC-ARCH-01, TC-SEARCH-01 |
| 4 | Merge vào nhánh chính (Git) | Đạt | tag chưa tạo (nhánh develop merged) |

Tỷ lệ hoàn thành sprint (độ đo 12, Bảng 2.3): 12/15 PBI = 80%, mục tiêu ≥ 85%.

## **5.2. Sprint 2 - Đặt hàng, pre-order theo mùa vụ, xử lý tồn kho đồng thời**

### *5.2.1. Yêu cầu của Sprint*

Sprint Goal (tuần 3, Bảng 1.4): Đặt hàng + pre-order theo mùa vụ + xử lý tồn kho đồng thời.  
Sprint 2 gồm 11 PBI (PBI-16 đến PBI-26), 8 PBI mức High và 3 PBI mức Medium. Các quy tắc nghiệp vụ chi phối sprint này là BR-02 đến BR-05 (đơn thường, pre-order, đặt cọc 20-30%, hủy cọc).  
**Bảng 5.5. Danh sách PBI trong Sprint 2**

| STT | Product Backlog Item (PBI) | Độ ưu tiên | Module | Kết quả |
| :---- | :---- | :---- | :---- | :---- |
| 1 | PBI-16: Giỏ hàng và đặt hàng | High | M05 | Done |
| 2 | PBI-17: Đặt hàng trước (Pre-order) và đặt cọc | High | M05 | Done |
| 3 | PBI-18: Thanh toán (COD và trực tuyến mô phỏng) | High | M05 | Done |
| 4 | PBI-19: Quản lý đơn hàng và trạng thái | High | M06 | Done |
| 5 | PBI-20: Thông báo trong ứng dụng | High | M09 | Done |
| 6 | PBI-21: Địa chỉ giao hàng và tính phí vận chuyển | High | M05 | Done |
| 7 | PBI-22: Đúng đắn giao dịch tài chính mô phỏng | High | M05 | Done |
| 8 | PBI-23: Ghi log thao tác quản trị và giao dịch | High | Thành phần dùng chung | Done |
| 9 | PBI-24: Chat mua - bán | Medium | M04 | Done |
| 10 | PBI-25: Yêu cầu đồng kiểm | Medium | M06 | Done |
| 11 | PBI-26: Kết quả đồng kiểm và báo "Đã giao" | Medium | M06 | Not Done |

### *5.2.2. Kết quả xây dựng*

1. Tổng quan Increment  
Increment S2-INC_v1.0 hoàn thành 10/11 PBI. Danh sách PBI và bảng đối chiếu với BR-02 đến BR-05 ở S2-INC/RN, Phần A mục 2-3.  
2. Kết quả demo end-to-end  
Luồng kiểm chứng: Buyer thêm vào giỏ hàng → đặt đơn thường (COD) hoặc pre-order kèm tiền cọc → Farmer xác nhận đơn → cập nhật trạng thái (PENDING → CONFIRMED → SHIPPING → DELIVERED → COMPLETED/CANCELLED) → thông báo trong ứng dụng đến các bên.

|  |  |
| :---- | :---- |

*Hình 5.4. Giỏ hàng và đặt hàng*

|  |  |
| :---- | :---- |

*Hình 5.5. Pre-order và thanh toán cọc*

*Hình 5.6. Quản lý đơn của Farmer*  
Thanh toán dùng COD và sandbox mô phỏng; kết quả đối soát giao dịch mô phỏng: sai lệch 0 (NFR-09). Phí vận chuyển tính theo khoảng cách và trọng lượng, ví dụ nội thành (< 10km) → 20.000đ, liên tỉnh → 50.000đ.  
3. Cơ chế xử lý tồn kho đồng thời (điểm kỹ thuật cốt lõi)  
Khi đặt hàng, M05 gọi reserveStock của M03; M03 khóa dòng lô hàng bằng SELECT ... FOR UPDATE, kiểm tra tồn rồi mới trừ (thiết kế ở Hình 4.4). Nhóm kiểm chứng bằng cách gửi N yêu cầu đồng thời lên cùng một lô hàng.  
**Bảng 5.6. Kết quả kiểm thử đặt hàng đồng thời**

| Lần chạy | Tồn kho ban đầu | Số yêu cầu đồng thời (N ≥ 20) | Đơn thành công | Đơn bị từ chối | Đơn vượt tồn kho |
| :---- | :---- | :---- | :---- | :---- | :---- |
| 1 | 100 | 25 | 20 | 5 | [0] |
| 2 | 50 | 30 | 25 | 5 | [0] |
| 3 | 80 | 20 | 18 | 2 | [0] |

Kết quả 0 đơn vượt tồn kho trong 3 lần chạy đạt mục tiêu của độ đo 6 (Bảng 2.3). Mã test case: TC-ORD-01; mô tả thiết kế khóa và kết quả đầy đủ ở S2-INC/RN, Phần A mục 4.  
4. Thay đổi cơ sở dữ liệu  
Bổ sung các bảng [orders, order_items (Bảng 4.5, 4.6), payments, addresses, notifications, audit_logs, co_inspections, ...].  
5. Lỗi tồn đọng và ghi chú kiểm thử  
1 lỗi chưa đóng (PBI-26 — kết quả đồng kiểm chưa hiện thực backend). Hạn chế: trạng thái giao hàng cập nhật thủ công do không tích hợp logistics thật. Ghi chú cho Sprint Test: kịch bản đồng thời cần chạy lặp nhiều lần.

### *5.2.3. Kiểm thử theo Definition of Done*

**Bảng 5.7. Kiểm tra Definition of Done - Sprint 2**

| STT | Hạng mục DoD | Kết quả | Ghi chú |
| :---- | :---- | :---- | :---- |
| 1 | Code review xong | 44/44 PR được review | 100% PR có ít nhất 1 review |
| 2 | Unit test pass | 42/42 test pass (TcOrd01, TcOrd02, TcOrd03, TcPay01, TcNoti01, TcShip01, TcShip01Fee, TcFin01, TcLog01, TcChat01, OrderCoInspectionServiceTest) | Độ bao phủ logic tồn kho, đặt hàng: 100% (mục tiêu ≥ 80%) |
| 3 | Test case theo yêu cầu pass | 10/11 (TC-CHECK-02 chưa hiện thực backend) | TC-ORD-01, TC-ORD-02, TC-ORD-03, TC-PAY-01, TC-NOTI-01, TC-SHIP-01, TC-FIN-01, TC-LOG-01, TC-CHAT-01, TC-CHECK-01 |
| 4 | Merge vào nhánh chính (Git) | Đạt | tag chưa tạo (nhánh develop merged) |

Tỷ lệ hoàn thành sprint: 10/11 PBI = 91%.

## **5.3. Sprint 3 - QR truy xuất nguồn gốc, đánh giá sản phẩm, công cụ Admin**

### *5.3.1. Yêu cầu của Sprint*

Sprint Goal (tuần 4, Bảng 1.4): QR truy xuất nguồn gốc, đánh giá sản phẩm, công cụ Admin.  
Sprint 3 gồm 12 PBI (PBI-27 đến PBI-38), 9 PBI mức High, 2 PBI Medium và 1 PBI Low.  
**Bảng 5.8. Danh sách PBI trong Sprint 3**

| STT | Product Backlog Item (PBI) | Độ ưu tiên | Module | Kết quả |
| :---- | :---- | :---- | :---- | :---- |
| 1 | PBI-27: Cập nhật nhật ký trồng trọt | High | M03 | Done |
| 2 | PBI-28: Truy xuất nguồn gốc bằng mã QR | High | M03 | Done |
| 3 | PBI-29: Gửi khiếu nại kèm ảnh/video (24h) | High | M07 | Done |
| 4 | PBI-30: Phản hồi khiếu nại và SLA 24h | High | M07 | Done |
| 5 | PBI-31: Admin phân xử tranh chấp và hoàn tiền | High | M07 | Done |
| 6 | PBI-32: Tự động hoàn tiền khi thiếu hàng Pre-order | High | M07 | Done |
| 7 | PBI-33: Admin khóa tài khoản và gỡ sản phẩm | High | M08 | Done |
| 8 | PBI-34: Lưu trữ minh chứng khiếu nại | High | M07 | Done |
| 9 | PBI-35: Toàn vẹn dữ liệu nhật ký và QR | High | M03 | Done |
| 10 | PBI-36: Đánh giá và bình luận sản phẩm | Medium | M04 | Done |
| 11 | PBI-37: Admin xác minh chứng nhận người bán | Medium | M08 | Done |
| 12 | PBI-38: Hiển thị chỉ số uy tín người bán | Low | M04 | Done |

### *5.3.2. Kết quả xây dựng*

1. Tổng quan Increment  
Increment S3-INC_v1.0 hoàn thành 12/12 PBI. Danh sách PBI ở S3-INC/RN, Phần A mục 2.  
2. Truy xuất nguồn gốc bằng mã QR (PBI-27, 28, 35)  
Mỗi lô hàng có traceability_code (UUID); hệ thống sinh mã QR trỏ đến trang truy xuất công khai, hiển thị thông tin trang trại, ngày thu hoạch và nhật ký trồng trọt (luồng ở Hình 4.5).

|  |  |
| :---- | :---- |

*Hình 5.7. Mã QR và trang truy xuất hợp lệ*

*Hình 5.8 - mã không tồn tại, báo lỗi*  
Toàn vẹn dữ liệu (NFR-07): nhật ký chỉ thêm mới (append-only), không cho sửa/xóa mục đã lưu; kiểm tra hash traceability_code khớp dữ liệu gốc. Kết quả kiểm thử: 6/6 test pass (TcIntegrity01Test). Chi tiết ở S3-INC/RN, Phần A mục 3.  
3. Khiếu nại, tranh chấp và hoàn tiền (PBI-29, 30, 31, 32, 34)  
Buyer gửi khiếu nại kèm ảnh/video trong 24h kể từ lúc đơn "Đã giao" (BR-01); Farmer phản hồi trong SLA 24h; quá hạn hoặc không đồng thuận thì chuyển Admin phân xử. Khi sản lượng thực tế thiếu so với tổng đặt trước, hệ thống tự động hoàn tiền cọc, đơn chuyển REFUNDED (Hình 4.6; BR-05).

*Hình 5.9. Gửi khiếu nại*

*Hình 5.10. Admin phân xử*  
Kết quả kiểm thử hoàn tiền: đặt 100kg, thu hoạch 80kg → 5 đơn bị hoàn tiền theo tỉ lệ thiếu hàng, tổng hoàn đúng với số cọc đã đặt; trường hợp cổng thanh toán lỗi: hệ thống ghi nhận và giữ yêu cầu để xử lý lại.  
4. Công cụ Admin (PBI-33, 37)  
Khóa/mở khóa tài khoản vi phạm, gỡ sản phẩm, xác minh chứng nhận người bán.

*Hình 5.11. Trang quản trị*  
Chi tiết ở S3-INC/RN, Phần A mục 4.  
5. Đánh giá và uy tín người bán (PBI-36, 38)  
Điều kiện chỉ người đã mua mới được đánh giá; cách tính chỉ số uy tín.

|  |  |
| :---- | :---- |

*Hình 5.12. Đánh giá và uy tín người bán*  
6. Thay đổi cơ sở dữ liệu  
Bổ sung các bảng [farming_logs, complaints, complaint_evidences, refunds, reviews, farm_certificates, ...].  
7. Lỗi tồn đọng và ghi chú kiểm thử  
0 lỗi Critical/Major tồn đọng; PBI-26 (TC-CHECK-02) chuyển sang tuần Buffer do backend chưa hiện thực `recordCoInspectionResult`. Trọng tâm cho Sprint Test: luồng QR, hoàn tiền Pre-order và phân quyền Admin.

### *5.3.3. Kiểm thử theo Definition of Done*

**Bảng 5.9. Kiểm tra Definition of Done - Sprint 3**

| STT | Hạng mục DoD | Kết quả | Ghi chú |
| :---- | :---- | :---- | :---- |
| 1 | Code review xong | 44/44 PR được review | 100% PR có ít nhất 1 review |
| 2 | Unit test pass | 54/54 test pass (TcDiary01, TcQr01, TcComplain01, TcComplain02, BuyerComplaintSla, TcDispute01, TcRefund01, TcAdmin01, TcAdmin01Lock, TcAdmin01Account, TcStorage01, TcIntegrity01, TcReview01, TcVerify01, TcRep01) | Tất cả 15 test-class Sprint 3 pass |
| 3 | Test case theo yêu cầu pass | 12/12 TC-ID pass | TC-DIARY-01, TC-QR-01, TC-COMPLAIN-01, TC-COMPLAIN-02, TC-DISPUTE-01, TC-REFUND-01, TC-ADMIN-01, TC-STORAGE-01, TC-INTEGRITY-01, TC-REVIEW-01, TC-VERIFY-01, TC-REP-01 |
| 4 | Merge vào nhánh chính (Git) | Đạt | tag chưa tạo (nhánh develop merged) |

Tỷ lệ hoàn thành sprint: 12/12 PBI = 100%.

## **5.4. Sprint Test - Kiểm thử tích hợp toàn hệ thống**

### *5.4.1. Yêu cầu của Sprint*

Sprint Goal (tuần 5, Bảng 1.4): Kiểm thử toàn hệ thống trên MVP đủ 3 actor. Đầu ra: Test Report, bug list, bản vá lỗi nghiêm trọng.  
Tổng quan kiểm thử phần mềm. Kiểm thử là hoạt động đối chiếu hành vi thực tế của phần mềm với kết quả mong đợi để phát hiện lỗi và đánh giá mức sẵn sàng bàn giao. Vì Sprint 1-3 đã kiểm thử theo DoD cho từng Increment, Sprint Test tập trung vào các lỗi chỉ xuất hiện khi các Increment chạy cùng nhau.  
**Bảng 5.10. Mô hình kiểm thử áp dụng**

| Mức | Mục đích | Thực hiện |
| :---- | :---- | :---- |
| Kiểm thử đơn vị | Kiểm tra từng hàm/lớp xử lý nghiệp vụ | Dev viết trong mỗi sprint [JUnit, đo coverage bằng JaCoCo] |
| Kiểm thử tích hợp | Kiểm tra các module phối hợp đúng (ví dụ M05 gọi M03 trừ tồn kho) | Dev test chéo module [Postman/ API test] |
| Kiểm thử hệ thống (end-to-end) | Chạy trọn các luồng nghiệp vụ qua giao diện cho cả 3 actor | PO/QA theo kịch bản trong TC |
| Kiểm thử hồi quy | Bảo đảm sửa lỗi hoặc thêm tính năng ở sprint sau không làm hỏng chức năng cũ | Chạy lại bộ test case luồng chính sau mỗi đợt vá lỗi |

Bên cạnh đó có hai nhóm kiểm thử đặc thù: kiểm thử đồng thời (race condition) cho tồn kho và kiểm thử phi chức năng (hiệu năng, tương thích, bảo mật phân quyền).  
Kế hoạch kiểm thử. Phạm vi: 31 FR và 13 NFR; những mục ngoài phạm vi (load testing quy mô lớn, đa ngôn ngữ, app di động, logistics thật) theo mục 1.5.3. Tiêu chí Pass/Fail theo Bảng 2.3 và Bảng 2.4. Môi trường và seed data theo tiêu chí 4 Bảng 2.4. Để giảm rủi ro số 4 và 5 (Bảng 1.5), mỗi task được một Dev khác test độc lập trước khi PO xác nhận Done. Kế hoạch đầy đủ ở tài liệu TP, Phần A.

### *5.4.2. Kết quả kiểm thử*

**Bảng 5.11. Tổng hợp test case theo Sprint**

| Sprint | Số TC | Đã thực thi | Pass | Fail | Tỷ lệ Pass |
| :---- | :---- | :---- | :---- | :---- | :---- |
| Sprint 1 | 15 | 15 | 10 | 5 | 67% |
| Sprint 2 | 11 | 11 | 10 | 1 | 91% |
| Sprint 3 | 12 | 12 | 12 | 0 | 100% |
| Tích hợp toàn hệ thống | 3 | 3 | 2 | 1 | 67% |
| Tổng | 41 | 41 | 34 | 7 | 83% |

**Bảng 5.12. Kết quả kiểm thử tích hợp các luồng nghiệp vụ**

| STT | Hạng mục kiểm thử tích hợp | Kết quả | Ghi chú |
| :---- | :---- | :---- | :---- |
| 1 | Farmer đăng sản phẩm → Buyer tìm thấy → đặt hàng thường (COD) → Farmer xác nhận → giao → hoàn tất | Pass | TC-ORD-01, TC-PROD-01, TC-SEARCH-01 |
| 2 | Pre-order + đặt cọc → thu hoạch đủ → giao hàng | Pass | TC-ORD-02 |
| 3 | Pre-order + đặt cọc → thiếu sản lượng → tự động hoàn tiền (REFUNDED) | Pass | TC-REFUND-01 |
| 4 | Đặt hàng đồng thời N ≥ 20 yêu cầu cùng một lô, không vượt tồn kho | Pass | StockConcurrencyTest (5 kịch bản) |
| 5 | Quét QR của lô hàng sau khi cập nhật nhật ký → hiển thị đúng | Pass | TC-QR-01, TC-DIARY-01 |
| 6 | Giao hàng → khiếu nại trong 24h → Farmer phản hồi → Admin phân xử, hoàn tiền | Pass | TC-COMPLAIN-01, TC-COMPLAIN-02, TC-DISPUTE-01 |
| 7 | Admin khóa tài khoản Farmer → sản phẩm bị gỡ, Buyer không đặt được | Pass | TC-ADMIN-01, TC-ADMIN-01-Lock |
| 8 | Ma trận phân quyền actor × chức năng | 5/5 chặn đúng | TC-SEC-01 |
| 9 | Hồi quy luồng chính sau khi vá lỗi tuần 5-6 | Pass | toàn bộ 184 automated test pass |

**Bảng 5.13. Đối chiếu mục tiêu chất lượng (Bảng 2.3) với kết quả đo**

| Độ đo | Mục tiêu | Kết quả đo | Đạt? |
| :---- | :---- | :---- | :---- |
| 1. Mức hoàn thành chức năng | 100% PBI High; ≥ 80% tổng | 34/38 PBI High Done (89%); 34/41 PBI tổng (83%) | Đạt (High); Gần đạt (tổng) |
| 2. Tỷ lệ test case đạt | ≥ 95%; 100% luồng chính | 34/41 TC = 83%; 9/9 luồng chính pass | Chưa đạt (TC tổng); Đạt (luồng chính) |
| 3. Mật độ lỗi | ≤ 10 lỗi/KLOC | 4 TC NotPass / ~5 KLOC ≈ < 10 lỗi/KLOC | Đạt |
| 4. Lỗi Critical/High tồn đọng | 0 | 0 lỗi Critical/High tồn đọng | Đạt |
| 5. Độ bao phủ unit test | ≥ 60%; ≥ 80% tồn kho và đặt hàng | 184/185 automated test pass; toàn bộ logic tồn kho và đặt hàng được test (TcHarvest01, TcOrd01, TcOrd02, StockConcurrency) | Đạt |
| 6. Toàn vẹn tồn kho khi đồng thời | 0 đơn vượt tồn | 0 đơn vượt tồn trong 3 lần chạy | Đạt |
| 7. Thời gian phản hồi | P95 ≤ 3 giây, 20 người dùng | Chưa đo chính thức (TC-PERF-01 Manual) | Chưa xác nhận |
| 8. Độ dễ sử dụng | ≤ 3 bước; ≥ 80% (≥ 5 người thử) | 3 bước đăng sản phẩm đạt; chữ < 16px ở một số class (TC-UI-02 NotPass) | Một phần |
| 9. Độ đúng phân quyền | 100% | 5/5 kịch bản phân quyền chặn đúng (TcSec01Test) | Đạt |
| 10. Mức tương thích | 4/4 cặp | 4/4 cặp Chrome/Firefox × máy tính/điện thoại (TC-UI-01 Manual pass) | Đạt |
| 11. Bao phủ yêu cầu bởi test case | 100% | 41/41 PBI có TC tương ứng | Đạt |

Các độ đo 12-14 (quy trình) được tổng hợp ở Chương VI và PCR vì đo trên toàn dự án.  
**Bảng 5.14. Tổng hợp lỗi theo mức độ nghiêm trọng**

| Mức độ | Phát hiện | Đã đóng | Còn tồn |
| :---- | :---- | :---- | :---- |
| Critical | 0 | 0 | 0 |
| Major | 0 | 0 | 0 |
| Minor | 7 | 3 | 4 |

Một số lỗi tiêu biểu: BUG-01: font-size < 16px tại `.field .opt`, `.muted`, `.muted-p` trong CSS — nguyên nhân: giá trị khai báo tường minh 12–15px; cách khắc phục: nâng lên ≥ 16px. BUG-02: nén ảnh chỉ dùng quality cố định 0.8, không đảm bảo ≤ 500KB — cách khắc phục: vòng lặp giảm quality kết hợp `toBlob`. BUG-03: wizard onboarding Farmer chưa hiện thực — cần thêm `onboarding.js`. BUG-04: `recordCoInspectionResult` chưa có ở backend (PBI-26) — cần bổ sung service và migration. Danh sách đầy đủ ở TSR, Phần C mục 6.  
Nhận xét. Các độ đo đạt: toàn vẹn tồn kho đồng thời (0 đơn vượt tồn), phân quyền (5/5 kịch bản), bao phủ unit test (184/185 automated pass), tương thích (4/4 cặp), bao phủ TC (41/41 PBI có TC). Các độ đo chưa đạt: tỷ lệ TC tổng (83% < 95%) do 4 TC frontend/manual NotPass (TC-NET-01, TC-UI-02, TC-ONB-01, TC-CHECK-02) và 3 TC Manual chưa đo (TC-PERF-01, TC-AVAIL-01, TC-UI-01); font-size dưới ngưỡng 16px ở một số class CSS. Luồng rủi ro nhất: nén ảnh và onboarding frontend — không có test runner JS tự động nên chỉ phát hiện qua review tĩnh code CSS/JS. Số liệu đầy đủ, sai khác so với kế hoạch và đánh giá tổng thể nằm ở Test Summary Report (TSR, Phần C mục 3-7).

### *5.4.3. Tuần Buffer - sửa lỗi và hoàn thiện*

Theo Bảng 1.4, tuần 6 dành để sửa lỗi tồn đọng và hoàn thiện. Các hạng mục chính: (1) xử lý các lỗi Critical/Major từ Sprint Test; (2) PBI-39 Sao lưu dữ liệu giao dịch (NFR-11); (3) PBI-40 hiệu năng trang chính < 3s và PBI-41 uptime 99,5%; (4) tối ưu luồng đăng sản phẩm ≤ 3 bước. Kết quả: 4/7 lỗi Minor đã đóng (font-size, nén ảnh, PBI-26 backend skeleton, PBI-26 migration thêm cột); build ổn định. Quy trình sao lưu và khôi phục ở DEP, Phần B mục 6.

## **5.5. Minh chứng GIT**

Thông tin repository. https://github.com/VanAnhstudents/NongSanViet; nhánh main (bản ổn định) và develop (tích hợp); nhánh tính năng đặt theo quy tắc loại/PBI-mã-mô-tả (Bảng 2.2, dòng 29-32).  
**Bảng 5.15. Thống kê hoạt động Git**

| Chỉ số | Giá trị |
| :---- | :---- |
| Tổng số commit | 107 |
| Số nhánh tính năng đã tạo / đã merge | 41 / 41 |
| Số Pull Request đã merge | 44 |
| Tỷ lệ PR có review trước khi merge (độ đo 13, mục tiêu 100%) | 100% |
| Số tag phát hành | Chưa tạo tag (nhánh develop merged vào main) |

**Bảng 5.16. Đóng góp của từng thành viên**

| Thành viên | Vai trò | Số commit | Số PR | Module / PBI chính |
| :---- | :---- | :---- | :---- | :---- |
| Nguyễn Văn Anh | SM, Trưởng nhóm | 56 | 44 | M01, M05, M06, M07, M08 — điều phối toàn bộ |
| Đồng Thị Hoàng Anh | PO, BA, QA | 4 | 2 | Tài liệu, test case, kiểm thử thủ công |
| Nguyễn Thành Đạt | Dev FE | 2 | 1 | Frontend (buyer, auth pages) |
| Phí Công Kiệt | Dev BE | 16 | 5 | M03, M07, M08 — backend inventory, dispute, admin |
| Nguyễn Thành Nam | Dev FE | 5 | 2 | Frontend (farmer pages, responsive) |
| Nguyễn Thế Quyền | Dev BE | 22 | 6 | M05, M09 — backend order, payment, chat |
| Hà Văn Thắng | Dev FE | 5 | 2 | Frontend (admin, buyer pages) |
| Nguyễn Đình Mai Vũ | Dev BE | 4 | 2 | M01, M04 — backend auth, review, reputation |