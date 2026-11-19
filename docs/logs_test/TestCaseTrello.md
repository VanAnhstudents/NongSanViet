### PBI-01: Đăng ký, đăng nhập, phân quyền
*Truy vết: FR-01 | Độ ưu tiên: High | Sprint dự kiến: Sprint 1*

**User story:** Là Farmer/Buyer, tôi muốn đăng ký và đăng nhập tài khoản theo đúng vai trò của mình để sử dụng các chức năng phù hợp trên hệ thống.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Người dùng đăng ký thành công với email/số điện thoại hợp lệ và chọn được vai trò (Farmer/Buyer).
- Hệ thống từ chối đăng ký khi thông tin trùng lặp hoặc không hợp lệ, hiển thị thông báo lỗi rõ ràng.
- Sau đăng nhập, người dùng chỉ thấy menu/chức năng tương ứng với vai trò được phân quyền.

**Test case liên quan:** TC-AUTH-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-02: Hồ sơ trang trại - thông tin tối thiểu
*Truy vết: FR-02a | Độ ưu tiên: High | Sprint dự kiến: Sprint 1*

**User story:** Là Farmer, tôi muốn tạo hồ sơ trang trại với các thông tin tối thiểu (tên, địa chỉ, loại nông sản) để người mua biết nguồn gốc sản phẩm.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Farmer điền đủ các trường bắt buộc (tên trang trại, địa chỉ, loại sản phẩm) và lưu thành công.
- Hồ sơ không lưu được nếu thiếu trường bắt buộc, hệ thống báo rõ trường còn thiếu.

**Test case liên quan:** TC-FARM-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-03: Đăng bán sản phẩm (tối đa 3 bước)
*Truy vết: FR-03 | Độ ưu tiên: High | Sprint dự kiến: Sprint 1*

**User story:** Là Farmer, tôi muốn đăng bán sản phẩm trong tối đa 3 bước để tiết kiệm thời gian thao tác.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Farmer hoàn tất đăng một sản phẩm (tên, giá, ảnh, số lượng) trong không quá 3 màn hình/bước.
- Sản phẩm sau khi đăng hiển thị đúng trên trang chủ/trang tìm kiếm trong vòng vài giây.

**Test case liên quan:** TC-PROD-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-04: Khai báo sản lượng và lịch thu hoạch
*Truy vết: FR-05 | Độ ưu tiên: High | Sprint dự kiến: Sprint 1*

**User story:** Là Farmer, tôi muốn khai báo sản lượng dự kiến và lịch thu hoạch để quản lý nguồn hàng và thông báo cho người mua.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Farmer nhập được sản lượng (số lượng/đơn vị) và ngày thu hoạch dự kiến, lưu thành công.
- Hệ thống cảnh báo khi ngày thu hoạch nhập không hợp lệ (ví dụ ngày trong quá khứ).

**Test case liên quan:** TC-HARVEST-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-05: Nén ảnh phía client và xử lý mạng yếu
*Truy vết: NFR-12 | Độ ưu tiên: High | Sprint dự kiến: Sprint 1*

**User story:** Là Farmer, tôi muốn ảnh được nén ngay trên thiết bị trước khi tải lên để đăng sản phẩm nhanh ngay cả khi mạng yếu.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Ảnh được nén còn tối đa kích thước quy định (ví dụ ≤ 500KB) trước khi gửi lên server.
- Việc tải ảnh vẫn hoàn tất thành công khi mô phỏng mạng yếu (ví dụ 3G/throttle), có cơ chế retry.

**Test case liên quan:** TC-NET-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-06: Web responsive (mobile-first)
*Truy vết: NFR-01 | Độ ưu tiên: High | Sprint dự kiến: Sprint 1*

**User story:** Là Farmer/Buyer, tôi muốn giao diện web hiển thị tốt trên điện thoại để thao tác thuận tiện ở mọi nơi.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Các trang chính hiển thị đúng bố cục, không vỡ layout trên màn hình từ 320px trở lên.
- Không xuất hiện thanh cuộn ngang trên các thiết bị di động phổ biến khi kiểm thử.

**Test case liên quan:** TC-UI-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-07: Giao diện tối giản (≤ 3 bước, chữ ≥ 16px)
*Truy vết: NFR-02 | Độ ưu tiên: High | Sprint dự kiến: Sprint 1*

**User story:** Là Farmer, tôi muốn giao diện tối giản, chữ đủ lớn để dễ thao tác dù không rành công nghệ.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Cỡ chữ mặc định trên toàn hệ thống ≥ 16px.
- Các luồng thao tác chính (đăng sản phẩm, đặt hàng) không vượt quá 3 bước.

**Test case liên quan:** TC-UI-02

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-08: Bảo mật HTTPS, băm mật khẩu, phân quyền
*Truy vết: NFR-04 | Độ ưu tiên: High | Sprint dự kiến: Sprint 1*

**User story:** Là Admin, tôi muốn toàn bộ hệ thống sử dụng HTTPS, mật khẩu được băm và phân quyền rõ ràng để đảm bảo an toàn dữ liệu người dùng.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Toàn bộ request đều qua HTTPS, request HTTP bị redirect hoặc từ chối.
- Mật khẩu lưu trong database ở dạng băm (hash), không có mật khẩu dạng plaintext.
- Người dùng không có quyền không thể truy cập API/chức năng ngoài phạm vi vai trò.

**Test case liên quan:** TC-SEC-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-09: Khả năng mở rộng (Modular Monolith)
*Truy vết: NFR-06 | Độ ưu tiên: High | Sprint dự kiến: Sprint 1*

**User story:** Là Admin (đội phát triển), tôi muốn hệ thống được tổ chức theo kiến trúc Modular Monolith để dễ bảo trì và mở rộng về sau.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Mã nguồn được chia thành các module rõ ràng theo nghiệp vụ (user, product, order, ...), không phụ thuộc chéo trực tiếp giữa các module.
- Có tài liệu mô tả kiến trúc module và ranh giới giao tiếp giữa các module.

**Test case liên quan:** TC-ARCH-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-10: Ngôn ngữ tiếng Việt và đơn vị VNĐ
*Truy vết: NFR-10 | Độ ưu tiên: High | Sprint dự kiến: Sprint 1*

**User story:** Là Farmer/Buyer, tôi muốn toàn bộ giao diện hiển thị bằng tiếng Việt và giá tiền theo đơn vị VNĐ để dễ sử dụng.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- 100% nhãn, thông báo trên giao diện hiển thị bằng tiếng Việt, không còn text mặc định tiếng Anh.
- Giá sản phẩm hiển thị đúng định dạng VNĐ (ví dụ: 50.000₫).

**Test case liên quan:** TC-I18N-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-11: Hồ sơ trang trại - thông tin mở rộng
*Truy vết: FR-02b | Độ ưu tiên: Medium | Sprint dự kiến: Sprint 1*

**User story:** Là Farmer, tôi muốn bổ sung thông tin mở rộng cho hồ sơ trang trại (mô tả, hình ảnh, chứng nhận) để tăng độ tin cậy với người mua.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Farmer có thể thêm mô tả, ảnh đại diện và các thông tin mở rộng khác cho hồ sơ, lưu thành công.
- Các trường mở rộng là tuỳ chọn (không bắt buộc), không chặn việc lưu hồ sơ nếu bỏ trống.

**Test case liên quan:** TC-FARM-02

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-12: Quản lý combo sản phẩm
*Truy vết: FR-04 | Độ ưu tiên: Medium | Sprint dự kiến: Sprint 1*

**User story:** Là Farmer, tôi muốn tạo combo gồm nhiều sản phẩm với giá ưu đãi để tăng giá trị đơn hàng.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Farmer tạo được combo gồm ≥ 2 sản phẩm, đặt giá combo và lưu thành công.
- Combo hiển thị đúng danh sách sản phẩm thành phần và giá khi Buyer xem.

**Test case liên quan:** TC-PROD-02

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-13: Hướng dẫn thao tác cho người bán
*Truy vết: FR-06 | Độ ưu tiên: Medium | Sprint dự kiến: Sprint 1*

**User story:** Là Farmer, tôi muốn có hướng dẫn thao tác (onboarding) khi lần đầu sử dụng để biết cách đăng sản phẩm và quản lý cửa hàng.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Farmer mới đăng nhập lần đầu được hiển thị hướng dẫn từng bước (tooltip/wizard).
- Farmer có thể bỏ qua hoặc xem lại hướng dẫn bất kỳ lúc nào.

**Test case liên quan:** TC-ONB-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-14: Tìm kiếm và lọc sản phẩm
*Truy vết: FR-10 | Độ ưu tiên: Medium | Sprint dự kiến: Sprint 1*

**User story:** Là Buyer, tôi muốn tìm kiếm và lọc sản phẩm theo tên, loại, giá, khu vực để nhanh chóng tìm được sản phẩm phù hợp.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Buyer tìm kiếm theo từ khoá trả về đúng kết quả chứa từ khoá trong tên sản phẩm.
- Buyer có thể kết hợp ít nhất 2 bộ lọc (ví dụ: loại sản phẩm + khoảng giá) cùng lúc.

**Test case liên quan:** TC-SEARCH-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-15: Quên mật khẩu và xác thực số điện thoại
*Truy vết: FR-31 | Độ ưu tiên: Medium | Sprint dự kiến: Sprint 1*

**User story:** Là Farmer/Buyer, tôi muốn khôi phục mật khẩu qua số điện thoại/email khi quên để tiếp tục sử dụng tài khoản.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Người dùng nhận được mã OTP/link đặt lại mật khẩu qua SĐT hoặc email đã đăng ký.
- Mật khẩu mới chỉ được chấp nhận sau khi xác thực OTP/link thành công, OTP hết hạn sau thời gian quy định.

**Test case liên quan:** TC-AUTH-02

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-16: Giỏ hàng và đặt hàng
*Truy vết: FR-16 | Độ ưu tiên: High | Sprint dự kiến: Sprint 2*

**User story:** Là Buyer, tôi muốn thêm sản phẩm vào giỏ hàng và đặt hàng để mua nhiều sản phẩm cùng lúc.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Buyer thêm/xoá/sửa số lượng sản phẩm trong giỏ hàng, tổng tiền cập nhật chính xác theo thời gian thực.
- Buyer đặt hàng thành công từ giỏ hàng, đơn hàng được tạo với đầy đủ thông tin sản phẩm và số lượng.

**Test case liên quan:** TC-ORD-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-17: Đặt hàng trước (Pre-order) và đặt cọc
*Truy vết: FR-17 | Độ ưu tiên: High | Sprint dự kiến: Sprint 2*

**User story:** Là Buyer, tôi muốn đặt hàng trước cho sản phẩm chưa thu hoạch và đặt cọc để giữ chỗ hàng.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Buyer đặt trước thành công cho sản phẩm có gắn lịch thu hoạch, hệ thống yêu cầu đặt cọc theo tỉ lệ quy định.
- Đơn Pre-order hiển thị trạng thái riêng, phân biệt rõ với đơn hàng thông thường.

**Test case liên quan:** TC-ORD-02

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-18: Thanh toán (COD và trực tuyến mô phỏng)
*Truy vết: FR-19 | Độ ưu tiên: High | Sprint dự kiến: Sprint 2*

**User story:** Là Buyer, tôi muốn chọn hình thức thanh toán COD hoặc thanh toán trực tuyến (mô phỏng) để hoàn tất đơn hàng.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Buyer chọn được COD hoặc thanh toán online mô phỏng tại bước checkout.
- Trạng thái thanh toán được cập nhật chính xác (đã thanh toán/chưa thanh toán) sau khi hoàn tất giao dịch mô phỏng.

**Test case liên quan:** TC-PAY-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-19: Quản lý đơn hàng và trạng thái
*Truy vết: FR-20 | Độ ưu tiên: High | Sprint dự kiến: Sprint 2*

**User story:** Là Farmer, tôi muốn quản lý danh sách đơn hàng và cập nhật trạng thái (đang xử lý, đang giao, đã giao) để theo dõi tiến độ bán hàng.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Farmer xem được danh sách đơn hàng của mình kèm trạng thái hiện tại.
- Farmer cập nhật trạng thái đơn hàng theo đúng quy trình (không nhảy cóc trạng thái không hợp lệ), Buyer thấy trạng thái cập nhật ngay.

**Test case liên quan:** TC-ORD-03

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-20: Thông báo trong ứng dụng
*Truy vết: FR-29a | Độ ưu tiên: High | Sprint dự kiến: Sprint 2*

**User story:** Là Farmer/Buyer, tôi muốn nhận thông báo trong ứng dụng khi có cập nhật đơn hàng/tin nhắn để không bỏ lỡ thông tin quan trọng.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Hệ thống gửi thông báo trong ứng dụng khi có sự kiện liên quan (đơn hàng mới, đổi trạng thái, tin nhắn mới).
- Người dùng có thể xem danh sách thông báo và đánh dấu đã đọc.

**Test case liên quan:** TC-NOTI-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-21: Địa chỉ giao hàng và tính phí vận chuyển
*Truy vết: FR-30 | Độ ưu tiên: High | Sprint dự kiến: Sprint 2*

**User story:** Là Buyer, tôi muốn quản lý địa chỉ giao hàng và xem phí vận chuyển được tính tự động để biết tổng chi phí trước khi đặt hàng.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Buyer thêm/sửa/xoá được địa chỉ giao hàng và chọn địa chỉ mặc định.
- Phí vận chuyển được tính và hiển thị chính xác theo địa chỉ/khoảng cách trước khi Buyer xác nhận đặt hàng.

**Test case liên quan:** TC-SHIP-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-22: Đúng đắn giao dịch tài chính mô phỏng
*Truy vết: NFR-09 | Độ ưu tiên: High | Sprint dự kiến: Sprint 2*

**User story:** Là Admin (hệ thống), tôi muốn các giao dịch tài chính mô phỏng (thanh toán, hoàn tiền, đặt cọc) được xử lý chính xác, không sai lệch số tiền.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Tổng số tiền giao dịch ghi nhận khớp 100% với số tiền đơn hàng trong mọi trường hợp kiểm thử.
- Không có trường hợp giao dịch bị trừ/cộng tiền trùng lặp khi test đồng thời (concurrency).

**Test case liên quan:** TC-FIN-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-23: Ghi log thao tác quản trị và giao dịch
*Truy vết: NFR-13 | Độ ưu tiên: High | Sprint dự kiến: Sprint 2*

**User story:** Là Admin, tôi muốn mọi thao tác quản trị và giao dịch quan trọng được ghi log để phục vụ truy vết và kiểm tra khi có sự cố.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Mọi thao tác quản trị (khoá tài khoản, hoàn tiền, gỡ sản phẩm) đều được ghi log kèm thời gian và người thực hiện.
- Log giao dịch tài chính có thể tra cứu lại theo đơn hàng/người dùng.

**Test case liên quan:** TC-LOG-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-24: Chat mua - bán
*Truy vết: FR-11 | Độ ưu tiên: Medium | Sprint dự kiến: Sprint 2*

**User story:** Là Buyer, tôi muốn nhắn tin trực tiếp với Farmer để hỏi thêm thông tin sản phẩm trước khi mua.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Buyer và Farmer gửi/nhận tin nhắn theo thời gian thực (hoặc gần thực) trong cùng một đơn/sản phẩm.
- Lịch sử chat được lưu lại và hiển thị đầy đủ khi mở lại cuộc trò chuyện.

**Test case liên quan:** TC-CHAT-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-25: Yêu cầu đồng kiểm
*Truy vết: FR-22 | Độ ưu tiên: Medium | Sprint dự kiến: Sprint 2*

**User story:** Là Buyer, tôi muốn yêu cầu đồng kiểm hàng khi nhận hàng để đảm bảo hàng đúng chất lượng/số lượng trước khi xác nhận.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Buyer gửi được yêu cầu đồng kiểm gắn với đơn hàng cụ thể trước khi xác nhận đã nhận hàng.
- Yêu cầu đồng kiểm hiển thị cho Farmer/đơn vị giao hàng để phối hợp thực hiện.

**Test case liên quan:** TC-CHECK-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-26: Kết quả đồng kiểm và báo "Đã giao"
*Truy vết: FR-23 | Độ ưu tiên: Medium | Sprint dự kiến: Sprint 2*

**User story:** Là Buyer, tôi muốn ghi nhận kết quả đồng kiểm và xác nhận "Đã giao" để hoàn tất đơn hàng.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Buyer nhập được kết quả đồng kiểm (đạt/không đạt, ghi chú) gắn với đơn hàng.
- Khi xác nhận "Đã giao", trạng thái đơn hàng chuyển đúng sang hoàn tất, không thể chỉnh sửa lại kết quả sau đó.

**Test case liên quan:** TC-CHECK-02

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-27: Cập nhật nhật ký trồng trọt
*Truy vết: FR-08 | Độ ưu tiên: High | Sprint dự kiến: Sprint 3*

**User story:** Là Farmer, tôi muốn cập nhật nhật ký trồng trọt (ngày gieo trồng, phân bón, thuốc BVTV...) để minh bạch quy trình canh tác.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Farmer thêm được các mục nhật ký theo mốc thời gian, mỗi mục có ngày và nội dung.
- Nhật ký hiển thị theo đúng thứ tự thời gian và không thể xoá sau khi đã công khai (phục vụ truy xuất).

**Test case liên quan:** TC-DIARY-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-28: Truy xuất nguồn gốc bằng mã QR
*Truy vết: FR-09 | Độ ưu tiên: High | Sprint dự kiến: Sprint 3*

**User story:** Là Buyer, tôi muốn quét mã QR trên sản phẩm để xem thông tin truy xuất nguồn gốc (nhật ký trồng trọt, trang trại).

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Mỗi sản phẩm/lô hàng có một mã QR duy nhất gắn với thông tin truy xuất.
- Quét mã QR hiển thị đúng và đầy đủ thông tin trang trại, nhật ký trồng trọt liên quan trong vòng vài giây.

**Test case liên quan:** TC-QR-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-29: Gửi khiếu nại kèm ảnh/video (24h)
*Truy vết: FR-24 | Độ ưu tiên: High | Sprint dự kiến: Sprint 3*

**User story:** Là Buyer, tôi muốn gửi khiếu nại kèm ảnh/video trong vòng 24h sau khi nhận hàng để phản ánh vấn đề về chất lượng sản phẩm.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Buyer gửi được khiếu nại kèm tối thiểu 1 ảnh/video trong vòng 24h kể từ khi đơn hàng được đánh dấu "Đã giao".
- Hệ thống từ chối khiếu nại gửi sau thời hạn 24h, có thông báo rõ lý do.

**Test case liên quan:** TC-COMPLAIN-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-30: Phản hồi khiếu nại và SLA 24h
*Truy vết: FR-25 | Độ ưu tiên: High | Sprint dự kiến: Sprint 3*

**User story:** Là Farmer, tôi muốn phản hồi khiếu nại của Buyer trong vòng 24h (SLA) để kịp thời giải quyết vấn đề.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Farmer phản hồi được khiếu nại, thời gian phản hồi được hệ thống ghi nhận so với SLA 24h.
- Hệ thống cảnh báo/leo thang (escalate) khiếu nại lên Admin nếu Farmer không phản hồi trong 24h.

**Test case liên quan:** TC-COMPLAIN-02

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-31: Admin phân xử tranh chấp và hoàn tiền
*Truy vết: FR-26 | Độ ưu tiên: High | Sprint dự kiến: Sprint 3*

**User story:** Là Admin, tôi muốn phân xử các tranh chấp giữa Buyer và Farmer, ra quyết định hoàn tiền khi cần để đảm bảo công bằng.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Admin xem được đầy đủ bằng chứng khiếu nại (ảnh/video, nhật ký) để ra quyết định.
- Khi Admin quyết định hoàn tiền, giao dịch hoàn tiền được thực hiện chính xác và cập nhật trạng thái đơn hàng/khiếu nại.

**Test case liên quan:** TC-DISPUTE-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-32: Tự động hoàn tiền khi thiếu hàng Pre-order
*Truy vết: FR-27 | Độ ưu tiên: High | Sprint dự kiến: Sprint 3*

**User story:** Là Buyer, tôi muốn được tự động hoàn tiền cọc khi Farmer không đủ hàng để giao cho đơn Pre-order.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Khi Farmer báo thiếu hàng cho đơn Pre-order, hệ thống tự động tạo giao dịch hoàn tiền cọc tương ứng.
- Buyer nhận được thông báo về việc hoàn tiền và trạng thái đơn hàng chuyển đúng sang "Đã hoàn tiền"/"Đã huỷ".

**Test case liên quan:** TC-REFUND-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-33: Admin khóa tài khoản và gỡ sản phẩm
*Truy vết: FR-28a | Độ ưu tiên: High | Sprint dự kiến: Sprint 3*

**User story:** Là Admin, tôi muốn khoá tài khoản vi phạm và gỡ sản phẩm không hợp lệ để bảo vệ chất lượng chợ nông sản.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Admin khoá được tài khoản, tài khoản bị khoá không thể đăng nhập/thao tác sau đó.
- Admin gỡ được sản phẩm vi phạm, sản phẩm không còn hiển thị với Buyer ngay sau khi gỡ.

**Test case liên quan:** TC-ADMIN-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-34: Lưu trữ minh chứng khiếu nại
*Truy vết: NFR-05 | Độ ưu tiên: High | Sprint dự kiến: Sprint 3*

**User story:** Là Admin (hệ thống), tôi muốn ảnh/video minh chứng khiếu nại được lưu trữ an toàn, không bị mất, để phục vụ phân xử tranh chấp.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Minh chứng khiếu nại được lưu trữ bền vững (persistent storage), không mất dữ liệu khi hệ thống restart.
- Chỉ các bên liên quan (Buyer, Farmer, Admin của khiếu nại đó) mới truy cập được minh chứng.

**Test case liên quan:** TC-STORAGE-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-35: Toàn vẹn dữ liệu nhật ký và QR
*Truy vết: NFR-07 | Độ ưu tiên: High | Sprint dự kiến: Sprint 3*

**User story:** Là Admin (hệ thống), tôi muốn dữ liệu nhật ký trồng trọt và mã QR không thể bị chỉnh sửa trái phép để đảm bảo độ tin cậy truy xuất nguồn gốc.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Nhật ký trồng trọt sau khi công khai không thể bị sửa/xoá trực tiếp bởi Farmer (chỉ có thể bổ sung mục mới).
- Dữ liệu gắn với mã QR khớp 100% với dữ liệu gốc khi kiểm tra đối chiếu (checksum/hash).

**Test case liên quan:** TC-INTEGRITY-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-36: Đánh giá và bình luận sản phẩm
*Truy vết: FR-14 | Độ ưu tiên: Medium | Sprint dự kiến: Sprint 3*

**User story:** Là Buyer, tôi muốn đánh giá (sao) và bình luận sản phẩm sau khi mua để chia sẻ trải nghiệm với người mua khác.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Buyer chỉ có thể đánh giá/bình luận sản phẩm sau khi đơn hàng đã hoàn tất ("Đã giao").
- Đánh giá (số sao trung bình) và bình luận hiển thị đúng trên trang sản phẩm.

**Test case liên quan:** TC-REVIEW-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-37: Admin xác minh chứng nhận người bán
*Truy vết: FR-28b | Độ ưu tiên: Medium | Sprint dự kiến: Sprint 3*

**User story:** Là Admin, tôi muốn xét duyệt và xác minh chứng nhận (VietGAP, hữu cơ...) của Farmer để đảm bảo độ tin cậy thông tin trên chợ.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Admin xem được danh sách chứng nhận Farmer nộp (file/ảnh) và phê duyệt/từ chối từng chứng nhận.
- Chứng nhận đã được duyệt hiển thị huy hiệu (badge) xác minh trên hồ sơ trang trại.

**Test case liên quan:** TC-VERIFY-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-38: Hiển thị chỉ số uy tín người bán
*Truy vết: FR-15 | Độ ưu tiên: Low | Sprint dự kiến: Sprint 3*

**User story:** Là Buyer, tôi muốn xem chỉ số uy tín của Farmer (đánh giá trung bình, tỉ lệ giao đúng hạn...) để quyết định có nên mua hàng hay không.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Chỉ số uy tín được tính toán tự động dựa trên dữ liệu đánh giá/đơn hàng và hiển thị trên hồ sơ Farmer.
- Chỉ số cập nhật lại khi có đánh giá mới hoặc đơn hàng hoàn tất mới.

**Test case liên quan:** TC-REP-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-39: Sao lưu dữ liệu giao dịch
*Truy vết: NFR-11 | Độ ưu tiên: Medium | Sprint dự kiến: Tuần 6 (buffer)*

**User story:** Là Admin (hệ thống), tôi muốn dữ liệu giao dịch được sao lưu định kỳ để có thể khôi phục khi xảy ra sự cố.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Hệ thống thực hiện sao lưu dữ liệu giao dịch tự động theo lịch định kỳ (ví dụ hằng ngày).
- Có thể khôi phục dữ liệu từ bản sao lưu gần nhất thành công khi thử nghiệm khôi phục.

**Test case liên quan:** TC-BACKUP-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-40: Hiệu năng trang chính < 3s
*Truy vết: NFR-03 | Độ ưu tiên: High | Sprint dự kiến: Sprint Test*

**User story:** Là Buyer, tôi muốn trang chính tải trong dưới 3 giây để trải nghiệm mua sắm mượt mà.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Thời gian tải trang chính (từ request đến hiển thị nội dung chính) < 3 giây trong điều kiện mạng trung bình khi kiểm thử hiệu năng.
- Kết quả đo hiệu năng đạt ngưỡng trên với ít nhất 95% số lần đo trong bộ kiểm thử.

**Test case liên quan:** TC-PERF-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop

---

### PBI-41: Khả dụng (uptime 99,5%)
*Truy vết: NFR-08 | Độ ưu tiên: High | Sprint dự kiến: Sprint Test*

**User story:** Là Admin (hệ thống), tôi muốn hệ thống đạt uptime tối thiểu 99,5% để đảm bảo người dùng luôn truy cập được dịch vụ.

**Tiêu chí chấp nhận (≥ 2, đo đếm được):**
- Hệ thống giám sát (monitoring) ghi nhận uptime ≥ 99,5% trong giai đoạn kiểm thử/vận hành thử.
- Có cơ chế cảnh báo (alert) tự động khi hệ thống downtime vượt ngưỡng cho phép.

**Test case liên quan:** TC-AVAIL-01

**Người phụ trách / Người test độc lập:** @... / @...

**Definition of Done:**
- [ ] Code review xong
- [ ] Unit test pass
- [ ] Test case theo yêu cầu pass
- [ ] Merge vào develop
