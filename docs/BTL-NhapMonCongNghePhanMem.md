# **LỜI NÓI ĐẦU**

Nông nghiệp từ lâu đã giữ vai trò là một trong những trụ cột của nền kinh tế Việt Nam, không chỉ đảm bảo an ninh lương thực quốc gia mà còn đóng góp đáng kể vào kim ngạch xuất khẩu và sinh kế của phần lớn dân cư nông thôn. Tuy nhiên, chuỗi cung ứng nông sản trong nước hiện nay vẫn vận hành chủ yếu qua nhiều tầng trung gian, từ nông dân đến thương lái, chợ đầu mối, tiểu thương rồi mới tới người tiêu dùng cuối. Mô hình phân phối nhiều tầng nấc này khiến chi phí giao dịch tăng lên qua mỗi khâu trung gian, trong khi giá trị thông tin về nguồn gốc, chất lượng sản phẩm lại không được cộng thêm tương xứng. Hệ quả là nông dân thường ở thế bị động về đầu ra và dễ chịu thiệt khi thị trường biến động, còn người tiêu dùng đô thị thì khó xác minh được nguồn gốc thực sự của nông sản mình mua.  
Trong bối cảnh đó, chủ trương chuyển đổi số nông nghiệp, nông thôn đã được Nhà nước thúc đẩy mạnh mẽ trong những năm gần đây, tiêu biểu là Kế hoạch số 1034/QĐ-BTTTT ngày 21/7/2021 của Bộ Thông tin và Truyền thông về hỗ trợ đưa hộ sản xuất nông nghiệp lên sàn thương mại điện tử. Song song với nỗ lực từ phía Nhà nước, khu vực tư nhân cũng chứng kiến sự xuất hiện của nhiều mô hình khởi nghiệp công nghệ nông nghiệp (agri-tech) nhằm rút ngắn khoảng cách giữa người sản xuất và người tiêu dùng. Đây chính là bối cảnh thực tiễn mà đề tài "Nền tảng kết nối nông sản trực tiếp từ nông dân đến người tiêu dùng" hướng tới mô phỏng và giải quyết, ở quy mô phù hợp với một đồ án học phần Nhập môn Công nghệ phần mềm.  
Báo cáo này được thực hiện nhằm trình bày toàn bộ quá trình nhóm tiếp cận, phân tích và xây dựng đề tài theo đúng bốn bước của vòng đời phát triển phần mềm: thu thập và phân tích yêu cầu (Requirement Engineering), thiết kế (Design), triển khai (Implementation) và kiểm thử (Test). Phần mở đầu của báo cáo tập trung giới thiệu tổng quan về đề tài, phân tích bài toán thực tiễn làm nền tảng cho việc xây dựng hệ thống, xác định các mục tiêu cụ thể mà nhóm hướng đến, đồng thời khảo sát một số hệ thống tương tự đã và đang vận hành trong thực tế nhằm rút ra bài học thiết kế và định vị sự khác biệt cho sản phẩm của nhóm.

# **DANH MỤC HÌNH ẢNH**

xxx

# **DANH MỤC BẢNG BIỂU**

xxx

# **DANH MỤC TỪ VIẾT TẮT**

xxx

# **CHƯƠNG I. GIỚI THIỆU**

## **1.1. Tổng quan về đề tài**

Chuỗi cung ứng nông sản tại Việt Nam hiện vẫn vận hành qua nhiều tầng trung gian: nông dân, thương lái, chợ đầu mối, tiểu thương rồi mới tới người tiêu dùng. Mỗi tầng làm phát sinh thêm chi phí vận chuyển, hao hụt và lợi nhuận trung gian nhưng không bổ sung thông tin về nguồn gốc sản phẩm. Trong khi đó, chuyển đổi số nông nghiệp đang được thúc đẩy mạnh: tính đến đầu năm 2022, hơn 3,5 triệu hộ sản xuất nông nghiệp đã được đào tạo kỹ năng số và tiếp cận thương mại điện tử, cùng hơn 60.000 sản phẩm nông sản, đặc sản địa phương được đưa lên sàn \[trích trong baochinhphu.vn, "Đưa nông sản lên sàn thương mại điện tử: Hướng đi chiến lược cho kinh tế số nông nghiệp"\].  
Đề tài xây dựng một ứng dụng web thương mại điện tử chuyên biệt cho nông sản, đóng vai trò trung gian công nghệ chứ không phải trung gian thương mại, phục vụ ba nhóm người dùng: Nông dân (Farmer) đăng bán sản phẩm, quản lý mùa vụ, tồn kho và xử lý đơn hàng; Người tiêu dùng (Buyer) tìm kiếm, đặt hàng hoặc đặt trước, theo dõi đơn, đánh giá sản phẩm và tra cứu nguồn gốc; Quản trị viên (Admin) duyệt tài khoản nông dân, quản lý danh mục, xử lý khiếu nại và xem báo cáo. Sản phẩm có ba điểm khác biệt so với sàn thương mại điện tử tổng hợp: kết nối trực tiếp, không có vai trò thương lái; đặt hàng trước (pre-order) theo mùa vụ; truy xuất nguồn gốc bằng mã QR gắn với từng lô sản phẩm.  
Đề tài được chọn vì ba lý do. Thứ nhất, đây là vấn đề có thật tại Việt Nam nên có tính thực tiễn cao. Thứ hai, độ phức tạp phù hợp với nhóm tám thành viên trong tám tuần: đủ số actor và module để mọi người tham gia cả bốn bước của vòng đời phần mềm, nhưng không đòi hỏi kiến trúc vi dịch vụ hay mô hình AI. Thứ ba, đề tài có các bài toán kỹ thuật rõ ràng để thực hành, như xử lý tồn kho khi nhiều người đặt trước cùng lúc (race condition), tính phí vận chuyển theo khoảng cách và sinh mã truy xuất nguồn gốc.

## 

## **1.2. Giới thiệu bài toán**

Ở phía nông dân, khó khăn lớn nhất là thế bị động về đầu ra: phụ thuộc thương lái, dễ bị ép giá và thiếu kênh dự báo nhu cầu để cân đối sản lượng, dẫn đến tình trạng "được mùa mất giá" lặp lại. Năm 2021, Hội Nông dân Việt Nam từng kêu gọi giải cứu 20.000 tấn hành tím tại Vĩnh Châu (Sóc Trăng), còn giá mít Thái giảm từ 80.000 xuống 15.000 đồng/kg \[trích trong vi.wikipedia.org, "Giải cứu nông sản"\]. Theo Viện Khoa học Nông nghiệp Việt Nam, dịch COVID-19 chỉ làm trầm trọng thêm vấn đề; nguyên nhân gốc là nông dân sản xuất theo kinh nghiệm, thiếu thông tin thị trường và không kết nối được với nơi tiêu thụ \[trích trong vaas.vn, "Giải cứu nông sản: Để không còn 'đến hẹn lại lên'"\]. Nông sản tươi bảo quản ngắn nên thất thoát sau thu hoạch cũng lớn: ước tính 3,5 đến 4,1 tỷ USD mỗi năm, tương đương khoảng 12% GDP ngành nông nghiệp \[trích trong vietnamplus.vn, "Bảo quản sau thu hoạch yếu kém gây tổn thất hàng tỷ USD"\]; khảo sát năm 2020 cho thấy tỷ lệ thất thoát trung bình khoảng 10% với trái cây và từ 20% đến 50% với rau củ \[trích trong moitruongachau.com, "Giảm lãng phí thực phẩm để đảm bảo mục tiêu phát triển bền vững"\]. Ngoài ra, nhiều nông dân ở vùng sâu vùng xa còn gặp rào cản khi dùng các nền tảng số phức tạp.  
Ở phía người tiêu dùng, vấn đề là thiếu minh bạch về nguồn gốc nên khó xác minh nông sản có đúng vùng trồng và quy trình như quảng cáo; giá bán bị đội lên qua nhiều tầng trung gian mà phần chênh lệch không quay lại người sản xuất; người ở đô thị cũng khó tiếp cận đặc sản vùng miền qua kênh uy tín.  
Từ đó, bài toán của đề tài là xây dựng một nền tảng cầu nối trực tiếp, minh bạch giữa nông dân và người tiêu dùng, cho phép nông dân chủ động đăng bán và nhận đặt hàng trước theo mùa vụ, cho phép người mua tra cứu nguồn gốc sản phẩm, và không đòi hỏi hạ tầng logistics phức tạp như sàn thương mại điện tử tổng hợp.

## 

## **1.3. Mục tiêu của đề tài**

Mục tiêu của đề tài chia thành hai nhóm. Về sản phẩm, hệ thống hướng đến sáu mục tiêu: (1) rút ngắn chuỗi cung ứng, cho phép nông dân đăng bán và người mua đặt hàng trực tiếp; (2) minh bạch thông tin sản phẩm, mỗi lô hàng có thông tin trang trại, ngày thu hoạch và mã QR truy xuất; (3) hỗ trợ đặt hàng trước theo mùa vụ để nông dân dự đoán nhu cầu trước khi thu hoạch; (4) dễ dùng với nông dân ít rành công nghệ, luồng đăng sản phẩm tối đa ba bước; (5) xây dựng niềm tin qua đánh giá sau giao dịch và lịch sử người bán; (6) hỗ trợ quản trị với các công cụ duyệt tài khoản, quản lý danh mục, xử lý khiếu nại và báo cáo tổng quan.  
Về thực hiện dự án, nhóm hướng đến sáu mục tiêu: (1) thực hành đủ bốn bước của vòng đời phát triển phần mềm (yêu cầu, thiết kế, triển khai, kiểm thử); (2) áp dụng phương pháp Agile với backlog, kế hoạch sprint và minh chứng vận hành; (3) tạo ra sản phẩm khả dụng tối thiểu (MVP) chạy được từ đầu đến cuối các luồng đăng sản phẩm, tìm kiếm, đặt hàng, xác nhận đơn và truy xuất nguồn gốc; (4) làm việc nhóm bảy đến tám người với phân công vai trò rõ ràng; (5) hoàn thành trong 8 tuần; (6) hoàn chỉnh bộ hồ sơ gồm đặc tả yêu cầu, thiết kế, mã nguồn, test case, báo cáo kiểm thử, slide và kịch bản demo. Khi hai nhóm mục tiêu xung đột về nguồn lực, nhóm ưu tiên mục tiêu thực hiện dự án: đúng quy trình, có MVP và có kiểm thử, hơn là thêm tính năng nhưng thiếu tài liệu.

## 

## **1.4. Các đề tài/hệ thống liên quan**

Nhóm khảo sát ba hệ thống có mô hình tương đồng để rút bài học thiết kế và định vị sản phẩm.  
FoodMap là sàn thương mại điện tử nông sản của Việt Nam, thành lập năm 2018, kết nối trực tiếp nông dân, nhà sản xuất với khách hàng theo cả mô hình B2B và B2C \[trích trong baodautu.vn, "Startup nông nghiệp Việt FoodMap gọi thành công 1 triệu USD vốn đầu tư"\]. Nền tảng đã huy động tổng cộng khoảng 4,5 triệu USD, cung ứng sản phẩm từ hơn 300 trang trại và phục vụ hơn 100.000 khách hàng \[trích trong baodautu.vn, cùng bài trên; forbes.vn, "Nền tảng thương mại điện tử nông sản FoodMap gọi đầu tư 2,9 triệu USD"\]. Toàn bộ sản phẩm đều có mã QR truy xuất nguồn gốc \[trích trong forbes.vn, cùng bài trên\]. Hạn chế: mô hình thiên về sàn có kho vận tập trung, phù hợp nhà sản xuất quy mô vừa trở lên hơn là hộ nhỏ lẻ, và chưa nhấn mạnh đặt hàng trước theo mùa vụ.  
Postmart.vn (nay là Buudien.vn từ 31/3/2024) do Tổng công ty Bưu điện Việt Nam vận hành, là một trong những sàn nông sản, đặc sản đầu tiên tại Việt Nam \[trích trong nhandan.vn, "Sàn thương mại điện tử Postmart đổi tên thành Buudien.vn, chuyên giao dịch về nông sản"\]. Riêng năm 2021, đơn vị này đã hỗ trợ 2,5 triệu hộ nông dân đưa nông sản lên sàn \[trích trong tuoitre.vn, "Nông dân băn khoăn cách đưa nông sản lên sàn thương mại điện tử"\]. Nền tảng miễn phí phần mềm quản lý kho, đơn hàng và phí giao dịch cho hộ sản xuất, đồng thời tận dụng mạng lưới bưu cục làm điểm thu gom, vận chuyển. Hạn chế: là sàn đa ngành với luồng nghiệp vụ tối ưu cho quy mô quốc gia, khá phức tạp so với một MVP và ít cá nhân hóa quan hệ giữa một người mua với một nông dân cụ thể.  
Local Line là phần mềm SaaS của Canada cho nông trại và food hub, hỗ trợ bán trực tiếp, bán sỉ và mô hình đăng ký nông sản định kỳ (CSA), thu phí thuê bao khoảng 99 đến 399 USD mỗi tháng \[trích trong farmzz.com, "Local Line Pricing (2026): Plans, Fees & Farm Review"\]. Tính năng đáng tham khảo là "order cycles", cho phép nông trại chủ động mở và đóng chu kỳ đặt hàng để quản lý thu hoạch chính xác hơn và giảm thất thoát \[trích trong localline.co, "How This CSA Fulfills 300 Orders and Saves 20 Hours Weekly with Local Line"\]. Hạn chế: phục vụ nông trại Bắc Mỹ có quy mô và mức độ số hóa cao hơn nhiều so với hộ nhỏ lẻ tại Việt Nam, và là công cụ B2B thu phí thuê bao.  
Do không thể cạnh tranh về quy mô hay hạ tầng, sản phẩm của đề tài được định vị là MVP tập trung vào hai điểm nhấn: truy xuất nguồn gốc bằng mã QR theo từng lô hàng (tham khảo FoodMap) và đặt hàng trước theo mùa vụ kết hợp xử lý tồn kho đồng thời (tham khảo "order cycle" của Local Line), cùng nguyên tắc giao diện tối giản cho nông dân, tối đa ba bước để đăng một sản phẩm (tham khảo triết lý dễ dùng, miễn phí của Postmart).

## 

## **1.5. Phương pháp phát triển**

### *1.5.1. Lý do chọn mô hình Agile \- Scrum rút gọn*

**Bảng 1.1. Lý do lựa chọn mô hình Agile \- Scrum rút gọn** 

| Đặc điểm dự án | Vì sao Agile phù hợp |
| :---- | :---- |
| Thời gian ngắn, cố định (8 tuần) | Sprint 1 tuần giúp kiểm soát tiến độ và phát hiện trễ sớm. |
| Yêu cầu chưa hoàn toàn rõ từ đầu | Tinh chỉnh backlog linh hoạt sau mỗi sprint. |
| Nhóm kỹ năng không đồng đều | Review liên tục để hỗ trợ thành viên kịp thời. |
| Cần demo được cuối kỳ | Ưu tiên tiêu chí "phần mềm chạy được" sau mỗi sprint. |

### *1.5.2. Cách thức áp dụng Scrum rút gọn*

Sprint: Chu kỳ 1 tuần, gồm Sprint 0 (chuẩn bị) \+ 3 sprint phát triển \+ 1 sprint test.  
Product Owner: Trưởng nhóm hoặc BA đảm nhận \- chịu trách nhiệm quyết định độ ưu tiên backlog.  
Scrum Master: Trưởng nhóm hoặc DevOps đảm nhận \- điều phối công việc và gỡ vướng cho nhóm.  
Họp nhóm: Tần suất 2-3 lần/tuần (thay vì họp daily) \+ họp Sprint Review / Retrospective vào cuối tuần.  
Task board: Sử dụng công cụ Trello hoặc GitHub Projects để quản lý tác vụ.

### *1.5.3. Phạm vi nghiên cứu*

Trong phạm vi:  
1\. Xây dựng ứng dụng web responsive, phục vụ đầy đủ 3 actors (Farmer, Buyer, Admin).  
2\. Tính năng thanh toán mô phỏng và sử dụng dữ liệu seed data tự tạo.  
3\. Tích hợp QR truy xuất nguồn gốc ở mức ứng dụng (không sử dụng blockchain).  
4, Áp dụng kiến trúc Modular Monolith và triển khai lên hạ tầng deploy free-tier.  
Ngoài phạm vi:  
1\. Giao dịch thương mại thật và gợi ý bằng thuật toán AI/ML recommendation.  
2\. Thực hiện load testing quy mô lớn, hỗ trợ đa ngôn ngữ hay phát triển app di động.  
3\. Tích hợp hệ thống logistics thật \- trạng thái giao hàng chỉ cập nhật thủ công.

## 

## **1.6. Vai trò của thành viên dự án**

**Bảng 1.2. Vai trò của các thành viên trong dự án**

| STT | Họ và tên | Vai trò Scrum |  |
| :---- | :---- | :---- | ----- |
| 1 | Nguyễn Văn Anh | Scrum Master, Trưởng nhóm  |  |
| 2 | Đồng Thị Hoàng Anh | Product Owner kiêm Developer (BA, QA/Testing) |  |
| 3 | Nguyễn Thành Đạt | Developer (FE, UI/UX Design) |  |
| 4 | Phí Công Kiệt | Developer (BE, DB) |  |
| 5 | Nguyễn Thành Nam | Developer (FE, UI/UX Design) |  |
| 6 | Nguyễn Thế Quyền | Developer (BE, DB) |  |
| 7 | Hà Văn Thắng | Developer (FE, UI/UX Design) |  |
| 8 | Nguyễn Đình Mai Vũ | Developer (BE, DB) |  |

## 

## **1.7. Phân công nhiệm vụ**

**Bảng 1.3. Phân công nhiệm vụ biên soạn báo cáo**

| Chương / Mục | SM | PO/BA/QA  | Dev | Cả nhóm |
| :---- | :---- | :---- | :---- | :---- |
| Chương 1\. Giới thiệu | P |  |  |  |
| 2.1-2.4 (Sản phẩm chuyển giao, tiêu chuẩn)  | P | S |  | S |
| 3.1-3.2 (Vai trò & phương pháp xác định yêu cầu)  | S | P  |  |  |
| 3.3. Danh sách yêu cầu hệ thống  | S | P  | S |  |
| 3.4-3.5 (Usecase, sơ đồ phân rã chức năng)  |  | P | S |  |
| 3.6. Kết quả đánh giá chất lượng yêu cầu  |  | P |  |  |
| 4.1-4.2 (Vai trò & các kiến trúc phổ biến)  | S | S | P |  |
| 4.3-4.7 (Kiến trúc, công nghệ, cơ sở dữ liệu, thiết kế chi tiết)  |  |  | P |  |
| 5.1. Kết quả xây dựng module/nhóm chức năng |  |  | P |  |
| 5.2. Kiểm thử hệ thống  | S | P | S  |  |
| 5.3. Minh chứng GIT  | S |  | P |  |
| 6.1-6.3 (Đánh giá, ưu nhược điểm, hướng phát triển)  | S | S |  | P |
| Tài liệu tham khảo, Phụ lục  | P |  |  |  |

Ghi chú:  
	\+ P: Primary (Phụ trách chính)  
	\+ S: Secondary (Hỗ trợ)

## 

## **1.8. Kế hoạch triển khai chi tiết**

### *1.8.1. Lộ trình thực hiện 8 tuần*

**Bảng 1.4. Kế hoạch thực hiện dự án trong 8 tuần**

| Tuần | Giai đoạn | Sprint Goal | Đầu ra chính | Vai trò chủ trì |
| :---- | :---- | :---- | :---- | :---- |
| 1 | Sprint 0 \- Chuẩn bị  | Chốt Product Backlog, kiến trúc, công nghệ, môi trường làm việc  | Product Backlog ưu tiên hoá; kiến trúc Modular Monolith; repo \+ task board; 1.6-1.7 hoàn tất  | PO chủ trì backlog; SM chủ trì setup quy trình  |
| 2 | Sprint 1  | Đăng ký/đăng nhập 3 actor; Farmer đăng sản phẩm; Buyer tìm kiếm  | Increment: đăng sản phẩm \+ tìm kiếm  | Dev team tự nhận task; PO làm rõ tiêu chí "Done" từng hạng mục  |
| 3 | Sprint 2  | Đặt hàng \+ pre-order theo mùa vụ \+ xử lý tồn kho đồng thời  | Increment: đặt hàng, pre-order, xác nhận đơn  | Dev team; PO ưu tiên nghiệp vụ pre-order theo mục tiêu 1.3  |
| 4 | Sprint 3 | QR truy xuất nguồn gốc, đánh giá sản phẩm, công cụ Admin  | Increment: QR, đánh giá, trang quản trị  | Dev team  |
| 5 | Sprint Test  | Kiểm thử toàn hệ thống trên MVP đủ 3 actor  | Test report, bug list, bản vá lỗi nghiêm trọng  | PO chủ trì QA, Dev team hỗ trợ test chéo  |
| 6 | Buffer & hoàn thiện  | Sửa lỗi tồn đọng, tối ưu luồng đăng sản phẩm ≤ 3 bước  | Bản build ổn định  | Dev team; SM giám sát tiến độ  |
| 7 | Hoàn thiện hồ sơ  | Viết/rà soát Chương 2-6 theo 1.7, chuẩn hoá minh chứng GIT  | Bản nháp báo cáo đầy đủ  | Cả nhóm; SM tổng hợp  |
| 8 | Chốt & bảo vệ  | Rà soát cuối, slide, kịch bản demo  | Báo cáo hoàn chỉnh, slide, demo  | Cả nhóm |

### *1.8.2. Quản lý rủi ro dự án*

**Bảng 1.5. Danh mục quản lý rủi ro dự án**

| STT | Rủi ro | Khả năng xảy ra  | Mức ảnh hưởng  | Biện pháp giảm thiểu  | Người theo dõi  |
| :---- | :---- | :---- | :---- | :---- | :---- |
| 1 | Thành viên không hoàn thành task đúng sprint  | Trung bình  | Cao  | Chia task nhỏ (≤1 ngày công); theo dõi qua task board  | SM |
| 2 | Backlog bị đổi ưu tiên giữa chừng, lệch kế hoạch sprint  | Trung bình  | Trung bình  | Chỉ PO có quyền đổi ưu tiên; công bố đầu sprint, không đổi giữa sprint  | PO |
| 3 | Race condition khi nhiều Buyer đặt trước cùng lô hàng  | Thấp  | Cao  | Thiết kế khoá tồn kho (locking) từ Sprint 2; viết test case riêng cho tình huống đồng thời  | Dev phụ trách task đó  |
| 4 | PO đồng thời là người test (QA) chính \- dễ có thiên vị khi tự đánh giá chất lượng chính tính năng mình ưu tiên đưa vào backlog  | Trung bình  | Trung bình  | Bắt buộc mỗi task còn phải được một Dev khác test độc lập trước khi PO xác nhận Done cuối cùng \- QA không chỉ dựa vào một người  | SM  |
| 5 | Bus factor QA \- chỉ 1 người (Hoàng Anh) nắm rõ kỹ năng test bài bản, nếu vắng mặt đúng Sprint Test thì cả nhóm không có ai thay thế  | Thấp  | Cao  | PO hướng dẫn nhanh 1-2 Dev khác về cách viết test case ngay từ Sprint 0-1, không đợi đến Sprint Test mới học  | PO  |
| 6 | Viết báo cáo bị dồn cuối kỳ vì ưu tiên code trước  | Cao  | Trung bình  | Viết báo cáo song song từng sprint theo 1.7; review tiến độ ở mỗi Sprint Review  | SM  |
| 7 | Xung đột code khi nhiều Dev tự nhận task chồng chéo (không phân module cố định)  | Trung bình  | Trung bình  | Chỉ nhận task không trùng nhau tại Sprint Planning; nhánh Git riêng theo tính năng, review trước merge  | Dev team \+ SM |

# **CHƯƠNG II. SẢN PHẨM CHUYỂN GIAO GIỮA CÁC PHA VÀ TIÊU CHUẨN ĐÁNH GIÁ**

## **2.1. Danh mục tài liệu và sản phẩm**

**Bảng 2.1. Danh mục tài liệu và sản phẩm chuyển giao giữa các pha**

| STT | Loại tài liệu/Sản phẩm | Biểu mẫu | Tiêu chuẩn đánh giá | P1 | P2 | P3S1 | P3S2 | P3S3 | P4ST |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| 1 | Business Requirements Document (BRD) \- Kết quả thu thập yêu cầu  | [BRD](https://docs.google.com/document/d/1lvXIaN2OhV2WOpwdTNe76Br81VKswwyh/edit?usp=sharing&ouid=102411615496158609974&rtpof=true&sd=true) (Phần A)  | Tuân chuẩn BABOK v3: minh chứng biên bản phỏng vấn 3 nhóm actor, phiếu khảo sát (108 phản hồi), mục tiêu và kết luận rõ ràng cho từng kỹ thuật elicitation.  | Out  | In  |  |  |  |  |
| 2 | Stakeholder Analysis & Problem-Solution Matrix  | [SAM / PSM](https://docs.google.com/document/d/1lvXIaN2OhV2WOpwdTNe76Br81VKswwyh/edit?usp=sharing&ouid=102411615496158609974&rtpof=true&sd=true) (Phần B.1-B.2)  | Bảng phân tích đầy đủ các bên liên quan (RACI/Power-Interest), ánh xạ rõ ràng giữa vấn đề thực tế và giải pháp đề xuất.  |  | Out  | In  |  |  |  |
| 3 | Software Requirements Specification (SRS) \- FR/NFR & Quy tắc nghiệp vụ  | [SRS](https://docs.google.com/document/d/1lvXIaN2OhV2WOpwdTNe76Br81VKswwyh/edit?usp=sharing&ouid=102411615496158609974&rtpof=true&sd=true) (Phần B.3-B.4)  | Tuân chuẩn IEEE 830: yêu cầu có mã định danh (FR-xx/NFR-xx), mô tả chi tiết, gắn quy tắc nghiệp vụ (BR), phân loại MoSCoW.  |  | Out  | In  |  |  |  |
| 4 | Mô hình hóa hệ thống (Use Case Diagram, DFD, BFD theo UML/BPMN)  | [UML / DFD](https://docs.google.com/document/d/1lvXIaN2OhV2WOpwdTNe76Br81VKswwyh/edit?usp=sharing&ouid=102411615496158609974&rtpof=true&sd=true) (Phần C)  | Tuân ký hiệu UML (Use Case) và BPMN/DFD chuẩn: sơ đồ đầy đủ, nhất quán với danh sách FR, có actor và luồng rẽ nhánh rõ ràng.  |  | Out  | In  |  |  |  |
| 5 | Requirements Traceability Matrix (RTM) & Biên bản phê duyệt (Sign-off)  | [RTM / Sign-off](https://docs.google.com/document/d/1lvXIaN2OhV2WOpwdTNe76Br81VKswwyh/edit?usp=sharing&ouid=102411615496158609974&rtpof=true&sd=true) (Phần D)  | Đánh giá đủ 5 tiêu chí chất lượng yêu cầu (Completeness, Consistency, Testability, Feasibility, Traceability); có bảng xác nhận phạm vi MVP và biên bản sign-off chuyển giao sang Design.  |  | Out  | In  |  |  |  |
| 6 | Software Architecture Document (SAD) \+ Database Design (ERD/3NF) \+ UI/UX Wireframe  | SAD / ERD / UIX  | Kiến trúc Modular Monolith mô tả rõ component, công nghệ; ERD chuẩn hóa 3NF; wireframe ánh xạ với Use Case tương ứng.  |  |  | Out  | In  |  |  |
| 7 | Sprint 1 Increment \+ Release Notes (đăng ký/đăng nhập, đăng sản phẩm, tìm kiếm)  | S1-INC / RN  | Đạt Definition of Done Sprint 1; chạy end-to-end luồng đăng ký/đăng nhập 3 actor, đăng sản phẩm (≤3 bước), tìm kiếm/lọc.  |  |  | Out  | In  |  |  |
| 8 | Sprint 2 Increment \+ Release Notes (giỏ hàng, pre-order, xử lý tồn kho đồng thời)  | S2-INC / RN  | Đạt DoD Sprint 2; luồng đặt hàng/pre-order/đặt cọc đúng BR; cơ chế khóa tồn kho xử lý đúng khi nhiều Buyer đặt trước cùng lúc.  |  |  |  | Out  | In  |  |
| 9 | Sprint 3 Increment \+ Release Notes (QR truy xuất, đánh giá, công cụ Admin)  | S3-INC / RN  | Đạt DoD Sprint 3; mã QR truy xuất đúng nhật ký trồng trọt; chức năng đánh giá hoạt động; Admin đủ công cụ duyệt/quản lý/xử lý khiếu nại.  |  |  |  |  | Out  | In  |
| 10 | Test Plan, Test Case & Test Summary Report (theo chuẩn IEEE 829\)  | TP / TC / TSR  | Tuân chuẩn IEEE 829: test case bao phủ đủ 31 FR và 13 NFR; ghi nhận Bug Severity (Critical/Major/Minor), trạng thái, tỉ lệ Pass/Fail theo Sprint.  |  |  |  |  |  | Out  |
| 11 | Source Code & Deployment/Handover Document  | SRC / DEP  | Tuân Naming Convention, comment đầy đủ, không dead code, pass kiểm thử tích hợp, kèm tài liệu bàn giao triển khai (deploy guide) trên hạ tầng free-tier.  |  |  | Out  | Out  | Out  | In  |
| 12 | Project Closure Report (Biên bản nghiệm thu & tổng kết dự án)  | PCR  | Tuân khung PMBOK (Project Closure): đối chiếu đầy đủ mục tiêu ban đầu (mục 1.3) với kết quả thực đạt; có xác nhận hoàn tất của toàn bộ 11 tài liệu ở Bảng 2.1; có chữ ký nghiệm thu từ PO và các bên liên quan.  |  |  |  |  |  | Out  |

Ghi chú:   
\- Out: Tài liệu/sản phẩm được tạo ra (đầu ra) trong pha đó.  
\- In: Tài liệu/sản phẩm được sử dụng (đầu vào) trong pha đó.  
\- Ký hiệu pha:  
\+ P1: Pha 1 \- Requirements  
\+ P2: Pha 2 \- Analysis  
\+ P3S1: Pha 3 \- Sprint 1  
\+ P3S2: Pha 3 \- Sprint 2  
\+ P3S3: Pha 3 \- Sprint 3  
\+ P4ST: Pha 4 \- Sprint Test

## 

## **2.2. Quy tắc đặt tên**

**Bảng 2.2. Quy tắc đặt tên**

| STT  | Đối tượng  | Quy tắc  | Ví dụ  |
| :---- | :---- | :---- | :---- |
| Quy tắc chung (mã nguồn)  |  |  |  |
| 1 | Thư mục, file mã nguồn  | kebab-case  | order-service, product-list  |
| 2 | Class, interface, type, component  | PascalCase  | OrderService, ProductCard  |
| 3 | Biến, hàm/method, tham số  | camelCase; biến boolean bắt đầu bằng is/has/can  | totalPrice, isPreOrder  |
| 4 | Hằng số  | UPPER\_SNAKE\_CASE  | MAX\_ORDER\_QUANTITY  |
| Giao diện web  |  |  |  |
| 5 | CSS class  | kebab-case  | product-card, btn-primary  |
| 6 | Thuộc tính id / data-testid của phần tử giao diện  | tiền tố kiểu phần tử \+ PascalCase (bảng tiền tố ở các dòng 7-18)  | btnSubmitOrder  |
| 7 | Label  | lbl  | lblProductName  |
| 8 | Textbox / input  | txt  | txtSearchKeyword  |
| 9 | Button  | btn  | btnAddProduct  |
| 10 | Checkbox  | chk  | chkAcceptTerms  |
| 11 | Radio button  | rdb  | rdbPaymentCod  |
| 12 | Dropdown / select  | cmb  | cmbCategory  |
| 13 | Table  | tbl  | tblOrderList  |
| 14 | Form  | frm  | frmLogin  |
| 15 | Panel / section  | pnl  | pnlOrderSummary  |
| 16 | Link  | lnk  | lnkTraceOrigin  |
| 17 | Image  | img  | imgProductPhoto  |
| 18 | Modal / dialog  | mdl  | mdlConfirmDelete  |
| Cơ sở dữ liệu (quan hệ)  |  |  |  |
| 19 | Database  | snake\_case  | farm\_direct  |
| 20 | Table  | snake\_case, số nhiều, không tiền tố  | users, order\_items  |
| 21 | Column  | snake\_case  | harvest\_date, created\_at  |
| 22 | Primary key  | cột id; constraint pk\_ \+ tên bảng  | pk\_orders  |
| 23 | Foreign key  | cột tên bảng số ít \+ \_id; constraint fk\_ \+ bảng con \+ bảng cha  | farmer\_id; fk\_products\_users  |
| 24 | Index / unique  | idx\_ hoặc uq\_ \+ bảng \+ cột  | idx\_products\_name, uq\_users\_email  |
| 25 | View  | vw\_ \+ snake\_case  | vw\_order\_summary  |
| 26 | Stored procedure / function  | động từ \+ snake\_case, không dùng tiền tố sp\_  | reserve\_stock  |
| API  |  |  |  |
| 27 | Endpoint  | kebab-case, danh từ số nhiều, có phiên bản; hành động thể hiện bằng phương thức HTTP  | GET /api/v1/products  |
| 28 | Trường JSON  | camelCase  | harvestDate  |
| Git và quản lý dự án  |  |  |  |
| 29 | Nhánh chính  | main (bản ổn định), develop (tích hợp)  | main, develop  |
| 30 | Nhánh tính năng  | loại/PBI-mã-mô-tả-ngắn; loại gồm feature, bugfix, hotfix, docs, test  | feature/PBI-05-pre-order  |
| 31 | Commit message  | loại(phạm vi): mô tả ngắn; loại gồm feat, fix, docs, test, refactor, chore  | feat(order): add pre-order API  |
| 32 | Mã PBI, test case, lỗi  | PBI-nn; TC-mã module-nn; BUG-nn  | PBI-05, TC-ORD-03, BUG-12  |
| 33 | File tài liệu  | Loại tài liệu\_Nội dung\_vMajor.Minor  | SRS\_v1.0.docx, TestReport\_Sprint2\_v1.0.xlsx  |

## 

## **2.3. Xác định mục tiêu chất lượng của dự án**

**Bảng 2.3. Mục tiêu chất lượng của dự án**

| STT | Tên độ đo | Ý nghĩa | Công thức/Cách đo |
| :---- | :---- | :---- | :---- |
| Chất lượng sản phẩm  |  |  |  |
| 1 | Mức độ hoàn thành chức năng  | Mức đáp ứng các yêu cầu đã cam kết trong Product Backlog  | Số PBI đạt DoD / Tổng số PBI × 100%. Mục tiêu: 100% PBI ưu tiên cao; ≥ 80% tổng số PBI  |
| 2 | Tỷ lệ test case đạt  | Mức đúng đắn của hệ thống theo các kịch bản kiểm thử  | Số test case Pass / Số test case đã thực thi × 100%. Mục tiêu: ≥ 95% toàn hệ thống; 100% với luồng chính (đăng sản phẩm, đặt hàng, pre-order, truy xuất QR)  |
| 3 | Mật độ lỗi  | Lượng lỗi tồn tại trong mã nguồn  | Số lỗi phát hiện / KLOC (đếm dòng mã bằng công cụ như cloc). Mục tiêu: ≤ 10 lỗi/KLOC  |
| 4 | Số lỗi nghiêm trọng tồn đọng  | Mức sẵn sàng để bàn giao và demo  | Số lỗi mức Critical/High chưa đóng tại thời điểm chốt bản build. Mục tiêu: 0  |
| 5 | Độ bao phủ mã của unit test  | Phần mã nguồn được kiểm thử tự động  | Số dòng mã được unit test thực thi / Tổng số dòng mã × 100% (công cụ coverage của framework). Mục tiêu: ≥ 60% toàn hệ thống; ≥ 80% với logic tồn kho và đặt hàng  |
| 6 | Tính toàn vẹn tồn kho khi đặt hàng đồng thời  | Không bán vượt số lượng khi nhiều người đặt trước cùng một lô (rủi ro race condition, mục 1.8.2)  | Số đơn được xác nhận vượt tồn kho khi gửi N yêu cầu đồng thời (N ≥ 20\) cho cùng một lô hàng. Mục tiêu: 0 đơn vượt tồn kho trong mọi lần chạy  |
| 7 | Thời gian phản hồi  | Trải nghiệm sử dụng ở mức chấp nhận được  | Phân vị 95 (P95) thời gian phản hồi của trang danh sách, tìm kiếm và đặt hàng với 20 người dùng đồng thời, sau khi hệ thống đã khởi động (đo bằng JMeter hoặc k6). Mục tiêu: P95 ≤ 3 giây  |
| 8 | Độ dễ sử dụng  | Đáp ứng mục tiêu dễ dùng với nông dân ít rành công nghệ (mục 1.3)  | (a) Số bước tối đa để đăng một sản phẩm; (b) Số người dùng thử hoàn thành việc đăng sản phẩm không cần hướng dẫn / Tổng số người thử × 100% (tối thiểu 5 người). Mục tiêu: (a) ≤ 3 bước; (b) ≥ 80%  |
| 9 | Độ đúng của phân quyền  | Ngăn truy cập trái quyền giữa Farmer, Buyer, Admin  | Số kịch bản truy cập trái quyền bị chặn đúng / Tổng số kịch bản trong ma trận actor × chức năng × 100%. Mục tiêu: 100%; mật khẩu lưu dạng băm  |
| 10 | Mức tương thích  | Hiển thị và thao tác đúng trên các môi trường thông dụng  | Số cặp (trình duyệt, kích thước màn hình) hoạt động đúng / Tổng số cặp kiểm thử (Chrome, Firefox × máy tính, điện thoại \= 4 cặp). Mục tiêu: 100%  |
| Chất lượng quy trình  |  |  |  |
| 11 | Độ bao phủ yêu cầu bởi test case  | Không bỏ sót yêu cầu khi kiểm thử  | Số yêu cầu có ít nhất 1 test case / Tổng số yêu cầu × 100% (ma trận truy vết). Mục tiêu: 100%  |
| 12 | Tỷ lệ hoàn thành sprint  | Mức ổn định và dự báo được của tiến độ  | Số task hoàn thành / Số task cam kết tại Sprint Planning × 100%. Mục tiêu: ≥ 85% mỗi sprint  |
| 13 | Tỷ lệ Pull Request được review  | Kiểm soát chất lượng mã trước khi merge  | Số PR có ít nhất 1 review trước merge / Tổng số PR × 100%. Mục tiêu: 100% (khớp với DoD "Code review xong")  |
| 14 | Mức đầy đủ của hồ sơ  | Bảo đảm bàn giao đủ tài liệu theo Bảng 2.1  | Số tài liệu đạt tiêu chuẩn đánh giá / Tổng số tài liệu trong Bảng 2.1 × 100%. Mục tiêu: 100% trước tuần 8  |

## 

## **2.4. Xác định tiêu chuẩn kiểm thử được cho dự án**

### *2.4.1. Khái niệm, ý nghĩa của đặc tính kiểm thử được*

Kiểm thử được (testability) là mức độ mà một yêu cầu, thành phần hoặc hệ thống cho phép xác lập tiêu chí kiểm thử rõ ràng và thực hiện kiểm thử để xác định tiêu chí đó đã được thỏa mãn hay chưa. Đây là một đặc tính con của tính bảo trì trong ISO/IEC 25010, và tương ứng với đặc tính "kiểm chứng được" (verifiable) của một yêu cầu tốt trong ISO/IEC/IEEE 29148\. Một yêu cầu như "hệ thống chạy nhanh" hay "giao diện thân thiện" không kiểm thử được vì không ai chứng minh được nó đạt hay không đạt.  
Trong dự án này, đặc tính kiểm thử được có ba ý nghĩa. Thứ nhất, nó là điều kiện để viết test case và xác định Definition of Done của mỗi PBI, tránh tranh cãi khi nghiệm thu giữa Product Owner và Developer. Thứ hai, nó phát hiện sớm các yêu cầu mơ hồ từ giai đoạn xác định yêu cầu, khi chi phí sửa còn thấp. Thứ ba, nó giúp kiểm thử trong Sprint Test có hệ thống, giảm phụ thuộc vào kinh nghiệm cá nhân của người test, một rủi ro đã nhận diện ở mục 1.8.2.  
Nhóm áp dụng sáu tiêu chí ở mục 2.4.2 để rà soát từng yêu cầu ở mục 3.6. Yêu cầu không đạt tiêu chí nào sẽ được Product Owner viết lại hoặc tách nhỏ trước khi đưa vào Sprint.

### *2.4.2. Chi tiết hóa các tiêu chí của đặc tính kiểm thử được*

**Bảng 2.4. Tiêu chí của đặc tính kiểm thử**

| STT | Tiêu chí | Yêu cầu cụ thể đối với dự án |
| :---- | :---- | :---- |
| 1 | Rõ ràng | Mỗi yêu cầu chỉ có một cách hiểu. Chức năng viết theo mẫu user story "Là \[actor\], tôi muốn \[hành động\] để \[lợi ích\]", trong đó actor là Farmer, Buyer hoặc Admin. Không dùng từ mơ hồ ("nhanh", "dễ dùng", "phù hợp") nếu không kèm số liệu. Các thuật ngữ nghiệp vụ (pre-order, lô hàng, mã truy xuất) được định nghĩa thống nhất.  |
| 2 | Định lượng được | Yêu cầu phi chức năng có đại lượng đo và ngưỡng cụ thể, ví dụ: đăng sản phẩm tối đa 3 bước; P95 thời gian phản hồi ≤ 3 giây với 20 người dùng đồng thời; 0 đơn vượt tồn kho khi đặt hàng đồng thời. Mỗi PBI có ít nhất 2 tiêu chí chấp nhận (acceptance criteria) có thể đếm hoặc so sánh được.  |
| 3 | Xây dựng được Test Case | Mỗi yêu cầu được ánh xạ tới ít nhất 1 test case qua ma trận truy vết. Test case có mã (TC-mã module-nn), tiền điều kiện, các bước, dữ liệu vào và kết quả mong đợi. PBI ưu tiên cao có đủ kịch bản luồng chính, luồng lỗi và giá trị biên. Yêu cầu không viết được test case phải viết lại hoặc tách nhỏ.  |
| 4 | Xác định dữ liệu kiểm thử | Có bộ seed data tự tạo cho từng actor (tài khoản Farmer, Buyer, Admin; sản phẩm, lô hàng, mùa vụ, tồn kho). Dữ liệu gồm giá trị hợp lệ, không hợp lệ và biên (số lượng bằng 0, bằng tồn kho, lớn hơn tồn kho; ngày thu hoạch không hợp lệ). Dữ liệu thanh toán là dữ liệu mô phỏng. Có script khôi phục dữ liệu về trạng thái ban đầu trước mỗi đợt kiểm thử.  |
| 5 | Quan sát được kết quả | Kết quả kiểm tra được ở ít nhất một trong bốn nơi: giao diện (thông báo, trạng thái đơn hàng), phản hồi API (mã trạng thái HTTP, nội dung), bản ghi cơ sở dữ liệu (tồn kho, trạng thái đơn) và log hệ thống. Các thao tác quan trọng như đặt hàng, trừ tồn kho, duyệt tài khoản được ghi log. Người kiểm thử có quyền truy cập môi trường test, cơ sở dữ liệu và log.  |
| 6 | Có tiêu chí Pass/Fail | Mỗi test case ghi rõ kết quả mong đợi để đối chiếu: Pass khi kết quả thực tế trùng kết quả mong đợi, ngược lại là Fail. Một PBI đạt DoD khi 100% test case mức Critical/High đạt, tổng tỷ lệ test case đạt ≥ 95% và không còn lỗi Critical/High chưa đóng (khớp với Bảng 2.3).  |

# **CHƯƠNG III. XÁC ĐỊNH YÊU CẦU**

## **3.1. Vai trò của việc Xác định yêu cầu đối với dự án phần mềm**

Xác định yêu cầu (Requirements Engineering) là quá trình khám phá, phân tích, đặc tả, kiểm chứng và quản lý những gì hệ thống phải làm (yêu cầu chức năng), phải đạt đến mức nào (yêu cầu phi chức năng) và phải tuân theo quy tắc nào (quy tắc nghiệp vụ, ràng buộc). Đây là pha đầu tiên của vòng đời phần mềm nên chất lượng của nó quyết định các pha sau. Vai trò chính trong dự án gồm:  
\- Thống nhất hiểu biết: ba nhóm người dùng (người bán \- nông dân, người tiêu dùng, quản trị viên) và nhóm phát triển 7-8 người cùng hiểu một bài toán; yêu cầu là "hợp đồng" giữa Product Owner và Developer.  
\- Giảm chi phí sửa lỗi: lỗi yêu cầu phát hiện ở pha kiểm thử tốn công sửa hơn nhiều lần so với phát hiện ngay từ đầu, điều rất quan trọng với dự án chỉ có 8 tuần.  
\- Cơ sở lập kế hoạch: mỗi yêu cầu thuộc phạm vi MVP thành một Product Backlog Item, được xếp độ ưu tiên và phân vào sprint.  
\- Cơ sở kiểm thử và nghiệm thu: test case và Definition of Done được xây dựng từ yêu cầu.  
\- Kiểm soát phạm vi và truy vết: chốt phạm vi MVP để tránh thêm tính năng giữa chừng, và dùng ma trận truy vết (RTM) để biết thay đổi một yêu cầu ảnh hưởng đến phần nào.  
Các hoạt động xác định yêu cầu trong dự án  
**Bảng 3.1. Các hoạt động, kỹ thuật và sản phẩm trong quy trình phân tích yêu cầu**

| Hoạt động | Kỹ thuật áp dụng | Sản phẩm |
| :---- | :---- | :---- |
| Thu thập | Phỏng vấn ba nhóm actor, khảo sát phiếu (108 phản hồi), khảo sát hệ thống tương tự | BRD |
| Phân tích | Stakeholder Analysis, Problem-Solution Matrix, MoSCoW | SAM / PSM |
| Đặc tả | Phân loại FR/NFR; định danh yêu cầu; đặc tả theo cấu trúc IEEE 830; User Story | SRS |
| Mô hình hóa | Use Case Modeling; Business Function Decomposition (BFD) | Use Case Diagram / BFD |
| Kiểm chứng và quản lý | Requirements Review/Inspection; kiểm tra tính đầy đủ, nhất quán, kiểm thử được, khả thi, truy vết được; Traceability Analysis | RTM / Sign-off |

## 

## **3.2. Phương pháp đánh giá chất lượng của yêu cầu**

Bước Đánh giá và Xác nhận thẩm định lại bộ yêu cầu đã thu thập, phân tích và đặc tả, bảo đảm bộ yêu cầu đạt chuẩn chất lượng và sẵn sàng chuyển sang pha Thiết kế. Nhóm đánh giá nội dung của FR, BR và NFR theo năm tiêu chí ở Bảng 3.2 (các sơ đồ Use Case và BFD được trình bày ở mục 3.4 và 3.5 nên không đánh giá lại ở bước này).  
**Bảng 3.2. Năm tiêu chí đánh giá chất lượng yêu cầu**

| Tiêu chí | Nội dung cần đạt |
| :---- | :---- |
| Đầy đủ (Completeness) | Bao phủ trọn vẹn nhu cầu của các bên liên quan (Farmer, Buyer, Admin) và các kịch bản nghiệp vụ chính |
| Nhất quán, không mâu thuẫn (Consistency) | Các quy tắc nghiệp vụ (BR) và yêu cầu chức năng (FR) không xung đột nhau |
| Kiểm thử được (Testability) | NFR và điều kiện biên có chỉ số định lượng cụ thể để kiểm thử |
| Khả thi (Feasibility) | Phù hợp nguồn lực, thời gian 8 tuần, nhóm 7-8 thành viên và hạ tầng của môn học |
| Truy vết được (Traceability) | Mọi chức năng và chỉ số chất lượng truy ngược được về nhu cầu thực tế từ khảo sát và phỏng vấn |

## 

## **3.3. Danh sách yêu cầu của hệ thống**

**Bảng 3.3. Danh sách yêu cầu**

| STT | Truy vết đến [RTM / Sign-off](https://docs.google.com/document/d/1j4U6xmBOuKsqTFr0tcQAt8isskCsnTLC/edit?usp=drive_link&ouid=102411615496158609974&rtpof=true&sd=true) | Product Backlog Item (PBI) | Độ ưu tiên  | Sprint dự kiến  |
| :---- | :---- | :---- | :---- | :---- |
| 1 | FR-01 | PBI-01: Đăng ký, đăng nhập, phân quyền | High | Sprint 1 |
| 2 | FR-02a | PBI-02: Hồ sơ trang trại \- thông tin tối thiểu | High | Sprint 1 |
| 3 | FR-03 | PBI-03: Đăng bán sản phẩm (tối đa 3 bước) | High | Sprint 1 |
| 4 | FR-05 | PBI-04: Khai báo sản lượng và lịch thu hoạch | High | Sprint 1 |
| 5 | NFR-12 | PBI-05: Nén ảnh phía client và xử lý mạng yếu | High | Sprint 1 |
| 6 | NFR-01 | PBI-06: Web responsive (mobile-first) | High | Sprint 1 |
| 7 | NFR-02 | PBI-07: Giao diện tối giản (≤ 3 bước, chữ ≥ 16px) | High | Sprint 1 |
| 8 | NFR-04 | PBI-08: Bảo mật HTTPS, băm mật khẩu, phân quyền | High | Sprint 1 |
| 9 | NFR-06 | PBI-09: Khả năng mở rộng (Modular Monolith) | High | Sprint 1 |
| 10 | NFR-10 | PBI-10: Ngôn ngữ tiếng Việt và đơn vị VNĐ | High | Sprint 1 |
| 11 | FR-02b | PBI-11: Hồ sơ trang trại \- thông tin mở rộng | Medium | Sprint 1 |
| 12 | FR-04 | PBI-12: Quản lý combo sản phẩm | Medium | Sprint 1 |
| 13 | FR-06 | PBI-13: Hướng dẫn thao tác cho người bán | Medium | Sprint 1 |
| 14 | FR-10 | PBI-14: Tìm kiếm và lọc sản phẩm | Medium | Sprint 1 |
| 15 | FR-31 | PBI-15: Quên mật khẩu và xác thực số điện thoại | Medium | Sprint 1 |
| 16 | FR-16 | PBI-16: Giỏ hàng và đặt hàng | High | Sprint 2 |
| 17 | FR-17 | PBI-17: Đặt hàng trước (Pre-order) và đặt cọc | High | Sprint 2 |
| 18 | FR-19 | PBI-18: Thanh toán (COD và trực tuyến mô phỏng) | High | Sprint 2 |
| 19 | FR-20 | PBI-19: Quản lý đơn hàng và trạng thái | High | Sprint 2 |
| 20 | FR-29a | PBI-20: Thông báo trong ứng dụng | High | Sprint 2 |
| 21 | FR-30 | PBI-21: Địa chỉ giao hàng và tính phí vận chuyển | High | Sprint 2 |
| 22 | NFR-09 | PBI-22: Đúng đắn giao dịch tài chính mô phỏng | High | Sprint 2 |
| 23 | NFR-13 | PBI-23: Ghi log thao tác quản trị và giao dịch | High | Sprint 2 |
| 24 | FR-11 | PBI-24: Chat mua \- bán | Medium | Sprint 2 |
| 25 | FR-22 | PBI-25: Yêu cầu đồng kiểm | Medium | Sprint 2 |
| 26 | FR-23 | PBI-26: Kết quả đồng kiểm và báo “Đã giao” | Medium | Sprint 2 |
| 27 | FR-08 | PBI-27: Cập nhật nhật ký trồng trọt | High | Sprint 3 |
| 28 | FR-09 | PBI-28: Truy xuất nguồn gốc bằng mã QR | High | Sprint 3 |
| 29 | FR-24 | PBI-29: Gửi khiếu nại kèm ảnh/video (24h) | High | Sprint 3 |
| 30 | FR-25 | PBI-30: Phản hồi khiếu nại và SLA 24h | High | Sprint 3 |
| 31 | FR-26 | PBI-31: Admin phân xử tranh chấp và hoàn tiền | High | Sprint 3 |
| 32 | FR-27 | PBI-32: Tự động hoàn tiền khi thiếu hàng Pre-order | High | Sprint 3 |
| 33 | FR-28a | PBI-33: Admin khóa tài khoản và gỡ sản phẩm | High | Sprint 3 |
| 34 | NFR-05 | PBI-34: Lưu trữ minh chứng khiếu nại | High | Sprint 3 |
| 35 | NFR-07 | PBI-35: Toàn vẹn dữ liệu nhật ký và QR | High | Sprint 3 |
| 36 | FR-14 | PBI-36: Đánh giá và bình luận sản phẩm | Medium | Sprint 3 |
| 37 | FR-28b | PBI-37: Admin xác minh chứng nhận người bán | Medium | Sprint 3 |
| 38 | FR-15 | PBI-38: Hiển thị chỉ số uy tín người bán | Low | Sprint 3 |
| 39 | NFR-11 | PBI-39: Sao lưu dữ liệu giao dịch | Medium | Tuần 6 (buffer) |
| 40 | NFR-03 | PBI-40: Hiệu năng trang chính \< 3s | High | Sprint Test |
| 41 | NFR-08 | PBI-41: Khả dụng (uptime 99,5%) | High | Sprint Test |

## 

## **3.4. Usecase Diagram**

![][image2]  
*Hình 3.1. Usecase Diagram hệ thống*

## 

## **3.5. Sơ đồ phân rã chức năng**

![][image3]  
*Hình 3.2. BFD \- Business Flow Diagram*

## 

## **3.6. Kết quả đánh giá chất lượng các yêu cầu**

Bộ yêu cầu gồm 31 mã FR, 13 NFR và 9 quy tắc nghiệp vụ đạt cả năm tiêu chí, với kết quả và minh chứng chính ở Bảng 3.4.  
**Bảng 3.4. Kết quả đánh giá theo tiêu chí**

| Tiêu chí | Kết quả | Minh chứng |
| :---- | :---- | :---- |
| Đầy đủ | Đạt | Bao phủ cả ba actor. Các tính năng đặc thù đều được đặc tả chi tiết: Pre-order (FR-17), truy xuất QR (FR-09), khiếu nại và đổi trả trong 24h (FR-24 đến FR-27), thông báo trong ứng dụng (FR-29a), địa chỉ và phí vận chuyển (FR-30). Do không tích hợp API giao hàng thật, đồng kiểm và xác nhận giao hàng (FR-23) được tối giản bằng link xác nhận mã đơn dùng một lần |
| Nhất quán | Đạt | Quy tắc BR-01 đến BR-09 là điểm tựa của các FR: mốc 24h xử lý khiếu nại tính từ lúc đơn "Đã giao" (BR-01); đơn thường (cho COD, không cọc, hủy tự do trước khi xác nhận \- BR-02, BR-03) tách rõ với Pre-order (cọc 20-30%, có chính sách hủy cọc \- BR-02, BR-04, BR-05); tự động hoàn tiền khi người bán không giao đủ hàng do thời tiết, mất mùa (BR-05, FR-27); Pre-order chỉ áp dụng cho nông dân cá thể ở MVP (BR-08) |
| Kiểm thử được | Đạt | NFR có ngưỡng đo cụ thể: NFR-02 (≤ 3 bước, chữ ≥ 16px, nút ≥ 48×48px, 80% người trên 50 tuổi hoàn thành trong 5 phút); NFR-03 (tải trang chính \< 3s trên 4G, xác nhận đặt hàng \< 1s ở 95% yêu cầu); NFR-08 (uptime ≥ 99,5%/tháng, MTTR \< 2 giờ); NFR-09 (sai lệch đối soát bằng 0); NFR-12 (ảnh ≤ 2MB, video ≤ 25MB/30s) |
| Khả thi | Đạt | Kiến trúc Modular Monolith trên hạ tầng free-tier (Vercel, Railway, Render); thanh toán ở mức mô phỏng/sandbox (VNPay, MoMo) với seed data tự tạo; mã QR xử lý ở mức ứng dụng, không dùng blockchain |
| Truy vết được | Đạt | Ma trận truy vết (mục B.3.3) liên kết 1-1 và 1-n giữa nỗi đau thực tế từ khảo sát 108 mẫu, phỏng vấn với các mã FR và NFR |

# **CHƯƠNG IV. KIẾN TRÚC PHẦN MỀM**

## **4.1. Vai trò của kiến trúc phần mềm với quá trình phát triển dự án phần mềm**

Kiến trúc phần mềm là bộ khung thiết kế tổng thể (gồm các thành phần, trách nhiệm và cách giao tiếp). Nằm giữa pha lấy yêu cầu và pha triển khai, kiến trúc đóng vai trò cầu nối biến "yêu cầu hệ thống" thành "bản đồ thi công" cho Developer và Tester. Đây là các quyết định nền tảng, khó thay đổi nhất về sau. Với vai trò cốt lõi đối với dự án:  
\- Hiện thực hóa yêu cầu phi chức năng (NFR): Đảm bảo tốc độ tải (\<3s), uptime (99,5%), bảo mật và khả năng mở rộng được thiết kế sâu từ bên trong, thay vì đắp vá sau khi code xong.   
\- Hỗ trợ làm việc song song: Phân định rõ ranh giới module giúp 3 Dev FE và 3 Dev BE làm việc độc lập, không chồng chéo task (giảm thiểu rủi ro xung đột mã số 7).   
\- Kiểm soát rủi ro kỹ thuật sớm: Xác định trước cách giải quyết các bài toán khó (khóa tồn kho khi mua đồng thời, tự động hoàn tiền, toàn vẹn dữ liệu) trước khi bắt tay vào viết mã.   
\- Làm nền tảng kiểm thử và bảo trì: Giúp truy vết chéo từ yêu cầu đến test case dễ dàng (đạt mục tiêu bao phủ 100%), khoanh vùng sửa lỗi nhanh và hỗ trợ kiểm thử đơn vị hiệu quả.   
Với dự án này, việc chốt kiến trúc sớm đặc biệt cần thiết vì ba lý do:   
\- Thời gian gấp rút: Dự án chỉ có 8 tuần (3 sprint phát triển), hoàn toàn không có thời gian đập đi làm lại nếu sai hướng.   
\- Nhân sự đa dạng: Nhóm 8 người với kỹ năng không đồng đều cần một bộ khung chuẩn để làm việc độc lập mà vẫn tích hợp thành công.  
\- Hạ tầng hạn chế: Giới hạn tài nguyên từ các gói free-tier đòi hỏi một kiến trúc đơn giản, dễ kiểm soát nguồn lực (loại trừ ngay kiến trúc phức tạp như vi dịch vụ). 

## 

## **4.2. Một số kiến trúc phần mềm phổ biến**

Kiến trúc phần mềm đóng vai trò quan trọng trong việc tổ chức, phân chia hệ thống thành các thành phần và xác định phương thức tương tác giữa chúng. Trong thực tế phát triển phần mềm, có thể xem xét các mô hình kiến trúc theo hai khía cạnh chính:   
\- Nhóm 1 \- Kiến trúc tổ chức triển khai: Monolithic truyền thống, Modular Monolith và Microservices.   
\- Nhóm 2 \- Kiến trúc tổ chức nội bộ: Layered/N-tier, MVC và MVVM.   
**Bảng 4.1. So sánh một số kiến trúc phần mềm phổ biến**

| Kiến trúc  | Ưu điểm  | Nhược điểm  | Mức phù hợp với dự án  |
| :---- | :---- | :---- | :---- |
| Monolithic truyền thống  | Toàn bộ hệ thống là một khối duy nhất nên triển khai đơn giản, chi phí vận hành thấp. Dễ gỡ lỗi. Giao dịch cơ sở dữ liệu (ACID) thực hiện trực tiếp.  | Mã nguồn dễ rối khi lớn dần vì các chức năng phụ thuộc chằng chịt. Nhiều người cùng sửa một vùng mã dễ gây xung đột. Khó tách thành phần về sau.  | Trung bình. Dễ làm nhưng không có ranh giới module nên khó phân công cho 8 người, dễ phát sinh rủi ro xung đột code |
| Modular Monolith  | Vẫn là một đơn vị triển khai nên chi phí thấp, chạy được trên free-tier. Chia module nghiệp vụ có ranh giới rõ nên phân công theo module, giảm xung đột. Giao dịch ACID trong một CSDL phù hợp bài toán khóa tồn kho. Có thể tách module thành dịch vụ riêng về sau.  | Cần kỷ luật giữ ranh giới giữa các module, nếu không sẽ trở lại monolith rối. Vẫn phải mở rộng cả khối, không mở rộng riêng từng module.  | Cao. Cân bằng giữa tổ chức mã nguồn rõ ràng và độ phức tạp thấp.  |
| Microservices  | Mỗi dịch vụ triển khai và mở rộng độc lập. Lỗi được cô lập trong từng dịch vụ. Mỗi dịch vụ có thể dùng công nghệ riêng.  | Phức tạp: cần API gateway, giao tiếp giữa các dịch vụ, giao dịch phân tán, giám sát tập trung. Chi phí vận hành cao. Khó chạy nhiều dịch vụ trên free-tier. Đòi hỏi kinh nghiệm DevOps.  | Thấp. Vượt quá năng lực nhóm và quy mô MVP; đề tài cũng xác định không đòi hỏi kiến trúc vi dịch vụ. |
| Layered / N-tier  | Tách rõ các mối quan tâm (giao diện, xử lý nghiệp vụ, truy cập dữ liệu). Phổ biến, dễ hiểu, dễ kiểm thử từng tầng.  | Nếu chỉ chia theo tầng kỹ thuật, một tính năng mới thường phải sửa ở nhiều tầng. Tự nó không tạo ranh giới giữa các nghiệp vụ.  | Cao, khi dùng bên trong từng module. Không đủ để làm kiến trúc tổng thể một mình.  |
| MVC / MVVM  | Tách dữ liệu, giao diện và điều khiển nên dễ bảo trì và kiểm thử giao diện. Được framework web hỗ trợ sẵn.  | Là mẫu thiết kế ở mức ứng dụng/giao diện, không quyết định cách chia hệ thống thành các khối nghiệp vụ.  | Trung bình. Áp dụng ở phía client và trong từng module, không dùng làm kiến trúc tổng thể.  |

## 

## **4.3. Kiến trúc tổng thể của hệ thống**

## **![][image4]**

*Hình 4.1. Kiến trúc tổng thể của hệ thống*  
\- Ba nhóm người dùng gồm Người mua, Nông dân và Quản trị viên truy cập qua Tầng Trình bày, là ứng dụng web responsive theo hướng mobile-first với giao diện riêng cho từng vai trò. Tầng này gửi yêu cầu xuống Tầng API qua HTTPS, dữ liệu JSON theo chuẩn REST. Tầng API là điểm vào duy nhất của hệ thống, đảm nhiệm xác thực và phân quyền, kiểm tra dữ liệu đầu vào, định tuyến đến đúng module và trả lỗi thống nhất bằng mã trạng thái HTTP.  
\- Tầng Nghiệp vụ là phần cốt lõi với chín module: Tài khoản, Hồ sơ, Sản phẩm, Tìm kiếm, Đặt hàng, Quản lý đơn hàng, Khiếu nại, Quản trị và Thông báo. Hai module cốt lõi được tô nổi bật là module 3 (Sản phẩm, Nhật ký và Truy xuất bằng QR, tồn kho) và module 5 (Đặt hàng, Pre-order và Thanh toán). Khi có đơn hàng, module 5 gọi module 3 để khóa và trừ tồn kho. Các module chỉ gọi nhau qua giao diện nội bộ, không truy cập trực tiếp bảng dữ liệu của nhau, nhờ đó có thể tách thành dịch vụ riêng khi cần.  
\- Tầng Truy cập dữ liệu gồm Repository/ORM theo từng module, quản lý giao dịch, khóa tồn kho khi nhiều người đặt hàng cùng lúc và truy cập kho lưu tệp. Bên dưới là Tầng Dữ liệu, gồm CSDL quan hệ farm\_direct chuẩn hóa 3NF và kho lưu tệp chứa ảnh sản phẩm, ảnh/video minh chứng khiếu nại.  
\- Khối Thành phần dùng chung bên trái (bảo mật, ghi log thao tác quản trị và giao dịch, cấu hình môi trường dev/test/prod) phục vụ xuyên suốt nhiều tầng.  
\- Về hệ thống bên ngoài, cổng thanh toán (sandbox VNPay/MoMo, thanh toán mô phỏng) được tích hợp qua API với module 5\. Đơn vị vận chuyển không tích hợp API nên trạng thái giao hàng được cập nhật thủ công từ module 6\.

## 

## **4.4. Lựa chọn công nghệ phát triển cho hệ thống**

**Bảng 4.2. Công nghệ sử dụng**

| Thành phần  | Công nghệ  | Lý do lựa chọn  |
| :---- | :---- | :---- |
| Backend |  |  |
| Ngôn ngữ | Java 21 | Phiên bản hỗ trợ dài hạn (LTS), kiểu dữ liệu chặt, nhiều tài liệu |
| Framework | Spring Boot 4.0.8 | Cấu hình sẵn, mỗi module nghiệp vụ là một package riêng, phù hợp với Modular Monolith. |
| Frontend |  |  |
| HTML5, CSS3, JavaScript  |  | Xây dựng giao diện website, trình bày nội dung và xử lý các tương tác với người dùng.  |
| Database |  |  |
| PostgreSQL |  | Hỗ trợ ACID và khóa hàng cho bài toán tồn kho |
| Authentication |  |  |
| Spring Security \+ JWT \+ BCrypt |  | BCrypt băm mật khẩu một chiều. Sau khi đăng nhập, server cấp JWT chứa mã người dùng và vai trò (Farmer/Buyer/Admin); mỗi yêu cầu kèm token để phân quyền mà không cần lưu phiên đăng |
| Hosting/Deploy |  |  |
| Render (Docker) \+ Neon (PostgreSQL) |  | Cả hai có gói miễn phí |

## 

## **4.5. Kiến trúc phần mềm của hệ thống**

![][image5]  
*Hình 4.2. Sơ đồ phân rã các Module trong kiến trúc Modular Monolith của hệ thống*  
Giải thích sơ đồ:  
\- Presentation Layer: cung cấp giao diện cho ba nhóm người dùng Farmer, Buyer và Admin.  
\- Application Layer: gồm 9 Module nghiệp vụ, mỗi Module chịu trách nhiệm một nhóm chức năng riêng.  
\- Infrastructure Layer: cung cấp cơ sở dữ liệu, lưu trữ tệp, dịch vụ QR, ghi log và kết nối các dịch vụ bên ngoài.  
\- Các Module giao tiếp thông qua các dịch vụ nội bộ, không tổ chức thành các hệ thống Microservices độc lập.  
Lưu ý: Các đường nối giữa Module trong sơ đồ là quan hệ phụ thuộc nghiệp vụ đề xuất. Khi thiết kế chi tiết, cần quy định rõ giao diện dịch vụ nội bộ và quyền sở hữu dữ liệu của từng Module.  
**Bảng 4.3. Danh sách Module và ánh xạ yêu cầu chức năng**

| Mã Module | Tên Module | Chức năng chính | FR liên quan |
| :---- | :---- | :---- | :---- |
| M01  | Identity & Access Management  | Quản lý đăng ký, đăng nhập, đăng xuất, lựa chọn loại hình người bán, xác thực OTP, đặt lại mật khẩu và phân quyền người dùng. | FR-01 → FR-06  |
| M02  | Seller & Store Management | Quản lý hồ sơ gian hàng, thông tin chi tiết, chứng nhận gian hàng, tài liệu hướng dẫn và đồng bộ tồn kho, giá bán. | FR-07 → FR-11 |
| M03  | Product & Traceability Management  | Quản lý sản phẩm, chứng nhận chất lượng, combo, sản lượng dự kiến, lịch thu hoạch, nhật ký trồng trọt, lịch sử phiên bản và truy xuất nguồn gốc QR. | FR-12 → FR-19  |
| M04  | Discovery & Interaction Management  | Hỗ trợ tìm kiếm, lọc sản phẩm, chat trực tiếp, thương lượng giá, theo dõi gian hàng, đánh giá sản phẩm và hiển thị uy tín người bán. | FR-20 → FR-27  |
| M05  | Order & Payment Management  | Quản lý giỏ hàng, tạo đơn hàng thường, Pre-order, cấu hình tiền cọc, COD, thanh toán trực tuyến, gợi ý mua kèm, Freeship, địa chỉ và phí vận chuyển. | FR-28 → FR-38  |
| M06  | Delivery & Inspection Management | Quản lý xác nhận đơn hàng, theo dõi trạng thái, yêu cầu hủy, đồng kiểm và xác nhận đơn hàng đã giao. | FR-39 → FR-44 |
| M07  | Complaint & Dispute Management  | Tiếp nhận khiếu nại, lưu minh chứng, phản hồi khiếu nại, theo dõi SLA 24 giờ, chuyển cấp tranh chấp, phân xử, hoàn tiền và xử lý thiếu hàng Pre-order. | FR-45 → FR-51  |
| M08  | Administration Management  | Quản lý tài khoản vi phạm, khóa/mở khóa tài khoản, gỡ bỏ sản phẩm và duyệt, xác minh chứng nhận chất lượng. | FR-52 → FR-54  |
| M09  | Notification Management  | Quản lý và gửi thông báo trong ứng dụng, thông báo đẩy và SMS cho người dùng khi phát sinh sự kiện nghiệp vụ. | FR-55 → FR-57 |

## 

## **4.6. Cơ sở dữ liệu mức vật lý của hệ thống**

![][image6]   
*Hình 4.3. Sơ đồ ERD hệ thống*  
Mô tả một số schema chi tiết:  
**Bảng 4.4. Quản lý lô nông sản \- batches**

| Thuộc tính   | Kiểu dữ liệu  | Ràng buộc   | Ghi chú  |
| :---- | :---- | :---- | :---- |
| id  | BIGINT  | PK , IDENTITY  | Mã lô hàng  |
| product\_id  | BIGINT  | NOT NULL, FK  | Sản phẩm thuộc lô  |
| traceability\_code  |  UUI | NOT NULL,UNIQUE  | Mã định danh truy xuất QR  |
| harvest\_date  | DATE  | NOT NULL  | Ngày thu hoạch  |
| unit\_price  | NUMERIC(15,2)  | NOT NULL, CHECK  | Giá bán trên mỗi đơn vị  |
| total\_quantity  | NUMERIC(10,2)  | NOT NULL, CHECK  | Số lượng của lô hàng  |
| status  | VARCHAR(20)  | NOT NULL, CHECK IN (‘PRE\_ORDER’,’AVAILABLE’,’SOLD\_OUT’,’CLOSED’)  | Trạng thái lô hàng  |
| created\_at  | TIMESTAMPTZ  | NOT NULL, DEFAULT now()  | Thời điểm tạo  |

**Bảng 4.5. Quản lý đơn hàng \- orders**

| Thuộc tính   | Kiểu dữ liệu  | Ràng buộc   | Ghi chú  |
| :---- | :---- | :---- | :---- |
| id  | BIGINT  | PK , IDENTITY    | Mã đơn hàng  |
| buyer\_id  | BIGINT  | NOT NULL, FK  | Người mua  |
| address\_id  | BIGINT  | NULL, FK  | Địa chỉ giao hàng  |
| payment\_method  | VARCHAR(20)  | NOT NULL, CHECK IN (‘COD’,’ONLINE’)  | Phương thức thanh toán  |
| status  | VARCHAR(20)  | NOT NULL, DEFAULT ‘PENDING’, CHECK IN (‘PENDING’,’CONFIRMED’, ’SHIPPING’,’DELIVERED’, ’COMPLETED’,’CANCELLED’)  | Trạng thái đơn hàng  |
| shipping\_fee  | NUMERIC(15,2)  | NOT NULL, DEFAULT 0, CHECK(\>=0)  | Phí vận chuyển  |
| delivered\_at  | TIMESTAMPTZ  | NULL  | Thời điểm giao hàng    |
| created\_at  | TIMESTAMPTZ  | NOT NULL, DEFAULT now()  | Thời điểm tạo đơn  |
| updated\_at  | TIMESTAMPTZ  | NOT NULL, DEFAULT now()  | Thời điểm cập nhật  |

**Bảng 4.6. Chi tiết đơn hàng \- order\_items**

| Thuộc tính   | Kiểu dữ liệu  | Ràng buộc   | Ghi chú  |
| :---- | :---- | :---- | :---- |
| id  | BIGINT  | PK , IDENTITY    | Mã chi tiết đơn hàng  |
| order\_id  | BIGINT  | NOT NULL, FK  | Đơn hàng liên quan  |
| batch\_id  | BIGINT  | NOT NULL, FK  | Lô nông sản được chọn  |
| quantity  | NUMERIC(10,2)  | NOT NULL, CHECK(\>=0)  | Số lượng mua  |
| unit\_price  | NUMERIC(15,2)  | NOT NULL, CHECK (\>=0)  | Giá tại thời điểm mua  |

## 

## **4.7. Một số thiết kế chi tiết theo kiến trúc được chọn**

![][image7] 

*Hình 4.4. Sơ đồ tuần tự xử lý đặt hàng có khóa tồn kho*  
Bước 1: Buyer gửi yêu cầu đặt trước.  
Buyer lựa chọn sản phẩm, lô hàng và số lượng cần đặt. Hệ thống M05 tiếp nhận yêu cầu và gọi chức năng reserveStock của module M03 để kiểm tra và giữ số lượng hàng tương ứng.  
Bước 2: Hệ thống khóa dữ liệu lô hàng.  
M03 mở giao dịch với cơ sở dữ liệu và sử dụng câu lệnh SELECT ... FOR UPDATE để khóa dòng dữ liệu của lô hàng. Khi Buyer A đang xử lý, yêu cầu của Buyer B đối với cùng lô hàng phải chờ cho đến khi giao dịch của Buyer A kết thúc.  
Bước 3: Kiểm tra và cập nhật số lượng tồn kho.  
Hệ thống kiểm tra trạng thái lô hàng và số lượng còn lại. Nếu số lượng đáp ứng yêu cầu, hệ thống giảm số lượng tồn kho tương ứng. Nếu không đủ, giao dịch bị hủy và hệ thống thông báo không thể đặt hàng.  
Bước 4: Tạo đơn hàng và giải phóng khóa.  
Sau khi giữ tồn kho thành công, M05 tạo đơn hàng với trạng thái PENDING và xác nhận giao dịch bằng COMMIT. Khi giao dịch kết thúc, khóa dữ liệu được giải phóng để các Buyer khác tiếp tục thực hiện yêu cầu.  
Bước 5: Xử lý yêu cầu của Buyer tiếp theo.  
Buyer B được tiếp tục kiểm tra số lượng tồn kho mới nhất. Nếu còn đủ hàng, hệ thống giữ tồn kho và tạo đơn hàng tương tự. Ngược lại, hệ thống từ chối yêu cầu. Quy trình này giúp tránh tình trạng nhiều Buyer đặt thành công vượt quá số lượng hàng thực tế.  
![][image8]  
*Hình 4.5. Sơ đồ tuần tự sinh và xác thực mã QR truy xuất nguồn gốc*  
Bước 1: Farmer tạo thông tin lô hàng.  
Farmer nhập thông tin sản phẩm và lô hàng lên hệ thống. Module M03 tiếp nhận dữ liệu và lưu thông tin vào cơ sở dữ liệu.  
Bước 2: Hệ thống tạo mã QR.  
Sau khi lưu thông tin lô hàng thành công, hệ thống tạo mã QR định danh duy nhất cho lô hàng đó. Mã QR được liên kết với thông tin lô hàng trong cơ sở dữ liệu và hiển thị cho Farmer.  
Bước 3: Farmer cập nhật nhật ký canh tác.  
Trong quá trình sản xuất, Farmer cập nhật các thông tin về quá trình canh tác. M03 tiếp nhận và lưu nhật ký vào hệ thống để phục vụ việc truy xuất nguồn gốc sản phẩm.  
Bước 4: Buyer quét mã QR.  
Khi nhận được sản phẩm hoặc muốn kiểm tra nguồn gốc, Buyer sử dụng thiết bị để quét mã QR. Hệ thống chuyển Buyer đến đường dẫn truy xuất và gửi mã định danh đến module M03.  
Bước 5: Hệ thống xác thực mã QR.  
M03 kiểm tra mã QR trong cơ sở dữ liệu. Nếu mã hợp lệ, hệ thống lấy thông tin lô hàng và nhật ký canh tác để hiển thị cho Buyer. Nếu mã không tồn tại hoặc không hợp lệ, hệ thống thông báo lỗi. Qua đó, Buyer có thể kiểm tra nguồn gốc và quá trình sản xuất của sản phẩm.  
![][image9]  
*Hình 4.6. Sơ đồ tuần tự xử lý Pre-order và hoàn tiền khi thiếu hàng*  
Bước 1: Buyer đặt trước sản phẩm.  
Buyer lựa chọn sản phẩm thuộc lô hàng đang mở đặt trước và gửi yêu cầu đặt hàng. Module M05 tiếp nhận yêu cầu, tạo đơn đặt trước và ghi nhận số lượng sản phẩm cần mua.  
Bước 2: Buyer thanh toán tiền đặt cọc.  
Buyer thực hiện thanh toán khoản tiền đặt cọc theo quy định, từ 20–30% giá trị đơn hàng. Nếu thanh toán thất bại, hệ thống thông báo để Buyer thực hiện lại. Nếu thanh toán thành công, đơn hàng được ghi nhận để tiếp tục xử lý.  
Bước 3: Farmer cập nhật sản lượng thu hoạch thực tế.  
Sau khi thu hoạch, Farmer cập nhật số lượng sản phẩm thực tế có thể cung cấp lên hệ thống. Dữ liệu này được sử dụng để đối chiếu với tổng số lượng hàng mà Buyer đã đặt trước.  
Bước 4: Hệ thống kiểm tra khả năng đáp ứng đơn hàng.  
Hệ thống so sánh sản lượng thực tế với số lượng đã đặt. Nếu sản lượng đủ, đơn hàng tiếp tục được xử lý và chuyển sang bước giao hàng. Nếu sản lượng không đủ, hệ thống xác định những đơn hàng bị ảnh hưởng để thực hiện hoàn tiền.  
Bước 5: Tính toán và gửi yêu cầu hoàn tiền.  
Module M07 phối hợp xử lý các đơn hàng bị ảnh hưởng, xác định số tiền cần hoàn và tạo yêu cầu hoàn tiền. Sau đó, hệ thống gửi yêu cầu đến cổng thanh toán đã sử dụng.  
Bước 6: Cập nhật kết quả hoàn tiền.  
Nếu hoàn tiền thành công, hệ thống cập nhật trạng thái đơn hàng thành REFUNDED và thông báo kết quả cho Buyer. Nếu hoàn tiền thất bại, hệ thống ghi nhận lỗi, lưu yêu cầu để tiếp tục xử lý và thông báo cho Buyer về tình trạng hoàn tiền.

# **CHƯƠNG V. XÂY DỰNG VÀ KIỂM THỬ HỆ THỐNG**

**Bảng 5.1. Đối chiếu các mục của Chương V với tài liệu chuyển giao (Bảng 2.1)**

| Mục | Nội dung | Tài liệu nguồn (Bảng 2.1) | Mã tài liệu |
| :---- | :---- | :---- | :---- |
| 5.1 | Sprint 1 | Sprint 1 Increment \+ Release Notes | S1-INC / RN |
| 5.2 | Sprint 2 | Sprint 2 Increment \+ Release Notes | S2-INC / RN |
| 5.3 | Sprint 3 | Sprint 3 Increment \+ Release Notes | S3-INC / RN |
| 5.4 | Sprint Test | Test Plan, Test Case, Test Summary Report | TP / TC / TSR |
| 5.5 | Minh chứng Git, mã nguồn | Source Code & Deployment/Handover Document | SRC / DEP |
| Chương VI | Tổng kết, đối chiếu mục tiêu | Project Closure Report | PCR |

## **5.1. Sprint 1 \- Đăng ký/đăng nhập, đăng sản phẩm, tìm kiếm**

### *5.1.1. Yêu cầu của Sprint*

Sprint Goal (tuần 2, Bảng 1.4): Đăng ký/đăng nhập 3 actor; Farmer đăng sản phẩm; Buyer tìm kiếm.  
Sprint 1 gồm 15 PBI (PBI-01 đến PBI-15), trong đó 10 PBI mức High, 5 PBI mức Medium. Cột "Module" lấy theo Bảng 4.3; các PBI phi chức năng áp dụng cho toàn hệ thống.  
**Bảng 5.2. Danh sách PBI trong Sprint 1**

| STT | Product Backlog Item (PBI) | Độ ưu tiên | Module | Kết quả |
| :---- | :---- | :---- | :---- | :---- |
| 1 | PBI-01: Đăng ký, đăng nhập, phân quyền | High | M01 | Done |
| 2 | PBI-02: Hồ sơ trang trại \- thông tin tối thiểu | High | M02 | Not Done |
| 3 | PBI-03: Đăng bán sản phẩm (tối đa 3 bước) | High | M03 | \[ \] |
| 4 | PBI-04: Khai báo sản lượng và lịch thu hoạch | High | M03 | \[ \] |
| 5 | PBI-05: Nén ảnh phía client và xử lý mạng yếu | High | M03 (FE) | \[ \] |
| 6 | PBI-06: Web responsive (mobile-first) | High | Toàn hệ thống | \[ \] |
| 7 | PBI-07: Giao diện tối giản (≤ 3 bước, chữ ≥ 16px) | High | Toàn hệ thống | \[ \] |
| 8 | PBI-08: Bảo mật HTTPS, băm mật khẩu, phân quyền | High | M01 | \[ \] |
| 9 | PBI-09: Khả năng mở rộng (Modular Monolith) | High | Kiến trúc | \[ \] |
| 10 | PBI-10: Ngôn ngữ tiếng Việt và đơn vị VNĐ | High | Toàn hệ thống | \[ \] |
| 11 | PBI-11: Hồ sơ trang trại \- thông tin mở rộng | Medium | M02 | \[ \] |
| 12 | PBI-12: Quản lý combo sản phẩm | Medium | M03 | \[ \] |
| 13 | PBI-13: Hướng dẫn thao tác cho người bán | Medium | M02 | \[ \] |
| 14 | PBI-14: Tìm kiếm và lọc sản phẩm | Medium | M04 | \[ \] |
| 15 | PBI-15: Quên mật khẩu và xác thực số điện thoại | Medium | M01 | \[ \] |

### *5.1.2. Kết quả xây dựng*

1\. Tổng quan Increment  
Kết thúc Sprint 1, nhóm bàn giao Increment S1-INC\_v1.0 chạy được tại \[link deploy\]: \[x\]/15 PBI đạt Done, trong đó \[x\]/10 PBI mức High. Chi tiết danh sách PBI Done/Not Done và bảng liên kết PBI ↔ FR/NFR ↔ Use Case nằm ở tài liệu S1-INC/RN, Phần A mục 2-3.  
2\. Kết quả demo theo luồng  
Luồng kiểm chứng end-to-end gồm ba chặng:  
\- Đăng ký, đăng nhập, phân quyền (PBI-01, 08, 15): người dùng đăng ký với vai trò Farmer hoặc Buyer, đăng nhập nhận JWT; tài khoản Admin tạo từ seed data. Mật khẩu băm BCrypt.

|  |  |
| :---- | :---- |

*Hình 5.1. Màn hình đăng ký/đăng nhập.*   
Kết quả chặn truy cập trái quyền: \[x/y\] kịch bản bị chặn đúng.  
\- Farmer đăng sản phẩm (PBI-02, 03, 04, 05, 12): luồng đăng gồm 3 bước: (1) chọn danh mục và tên, (2) tải ảnh (tự nén) và nhập giá, (3) khai báo sản lượng, ngày thu hoạch rồi đăng.

|  |  |  |
| :---- | :---- | :---- |

*Hình 5.2. Ba bước đăng sản phẩm*  
Khi đăng, hệ thống tạo lô hàng kèm mã traceability\_code để dùng cho truy xuất QR ở Sprint 3\.  
\- Buyer tìm kiếm và lọc (PBI-14): tìm theo từ khóa, lọc theo danh mục, vùng, khoảng giá.

*Hình 5.3. Màn hình tìm kiếm trên điện thoại*  
3\. Đối chiếu yêu cầu phi chức năng  
**Bảng 5.3. Kết quả đo các NFR trong Sprint 1**

| PBI / NFR | Ngưỡng yêu cầu | Kết quả đo | Đạt? |
| :---- | :---- | :---- | :---- |
| PBI-03, PBI-07 / NFR-02 | ≤ 3 bước, chữ ≥ 16px, nút ≥ 48×48px | \[ \] | \[\] |
| PBI-05 / NFR-12 | Ảnh ≤ 2MB | \[ \] | \[ \] |
| PBI-06 / NFR-01 | 4 cặp (Chrome, Firefox × máy tính, điện thoại) | \[x/4\] | \[ \] |
| PBI-08 / NFR-04 | HTTPS, mật khẩu băm | \[ \] | \[ \] |
| PBI-10 / NFR-10 | Tiếng Việt, đơn vị VNĐ | \[ \] | \[ \] |

4\. Hạ tầng và mã nguồn  
Cấu trúc thư mục theo kebab-case, mỗi module nghiệp vụ là một package riêng (PBI-09); repo, nhánh main/develop và môi trường Render \+ Neon được dựng từ sprint này. Chi tiết ở tài liệu SRC/DEP, Phần A mục 1-2 và Phần B mục 2-5. Cơ sở dữ liệu có các bảng \[users, farms, products, batches, ...\] (xem Hình 4.3, Bảng 4.4).  
5\. Lỗi tồn đọng và Release Notes  
Tại thời điểm chốt sprint còn \[n\] lỗi chưa đóng (\[n\] Critical/High, \[n\] Minor), danh sách ở S1-INC/RN Phần B mục 4\. Các hạn chế đã biết: OTP ở mức mô phỏng (PBI-15); \[hạn chế khác\]. Ghi chú cho người kiểm thử ở Sprint Test: \[ví dụ: tập trung kiểm tra phân quyền và giá trị biên khi đăng sản phẩm\].

### *5.1.3. Kiểm thử theo Definition of Done*

**Bảng 5.4. Kiểm tra Definition of Done \- Sprint 1**

| STT | Hạng mục DoD | Kết quả | Ghi chú |
| :---- | :---- | :---- | :---- |
| 1 | Code review xong | \[x/y PR được review\] | \[ví dụ: 100% PR có ít nhất 1 review\] |
| 2 | Unit test pass | \[x/y test pass\] | \[độ bao phủ ...%\] |
| 3 | Test case theo yêu cầu pass | \[x/y\] | \[mã TC liên quan, ví dụ TC-AUTH-01...\] |
| 4 | Merge vào nhánh chính (Git) | \[Đạt\] | \[tag sprint-1\] |

Tỷ lệ hoàn thành sprint (độ đo 12, Bảng 2.3): \[x/y task \= z%\], mục tiêu ≥ 85%.

## **5.2. Sprint 2 \- Đặt hàng, pre-order theo mùa vụ, xử lý tồn kho đồng thời**

### *5.2.1. Yêu cầu của Sprint*

Sprint Goal (tuần 3, Bảng 1.4): Đặt hàng \+ pre-order theo mùa vụ \+ xử lý tồn kho đồng thời.  
Sprint 2 gồm 11 PBI (PBI-16 đến PBI-26), 8 PBI mức High và 3 PBI mức Medium. Các quy tắc nghiệp vụ chi phối sprint này là BR-02 đến BR-05 (đơn thường, pre-order, đặt cọc 20-30%, hủy cọc).  
**Bảng 5.5. Danh sách PBI trong Sprint 2**

| STT | Product Backlog Item (PBI) | Độ ưu tiên | Module | Kết quả |
| :---- | :---- | :---- | :---- | :---- |
| 1 | PBI-16: Giỏ hàng và đặt hàng | High | M05 | \[ \] |
| 2 | PBI-17: Đặt hàng trước (Pre-order) và đặt cọc | High | M05 | \[ \] |
| 3 | PBI-18: Thanh toán (COD và trực tuyến mô phỏng) | High | M05 | \[ \] |
| 4 | PBI-19: Quản lý đơn hàng và trạng thái | High | M06 | \[ \] |
| 5 | PBI-20: Thông báo trong ứng dụng | High | M09 | \[ \] |
| 6 | PBI-21: Địa chỉ giao hàng và tính phí vận chuyển | High | M05 | \[ \] |
| 7 | PBI-22: Đúng đắn giao dịch tài chính mô phỏng | High | M05 | \[ \] |
| 8 | PBI-23: Ghi log thao tác quản trị và giao dịch | High | Thành phần dùng chung | \[ \] |
| 9 | PBI-24: Chat mua \- bán | Medium | M04 | \[ \] |
| 10 | PBI-25: Yêu cầu đồng kiểm | Medium | M06 | \[ \] |
| 11 | PBI-26: Kết quả đồng kiểm và báo "Đã giao" | Medium | M06 | \[ \] |

### *5.2.2. Kết quả xây dựng*

1\. Tổng quan Increment  
Increment S2-INC\_v1.0 hoàn thành \[x\]/11 PBI. Danh sách PBI và bảng đối chiếu với BR-02 đến BR-05 ở S2-INC/RN, Phần A mục 2-3.  
2\. Kết quả demo end-to-end  
Luồng kiểm chứng: Buyer thêm vào giỏ hàng → đặt đơn thường (COD) hoặc pre-order kèm tiền cọc → Farmer xác nhận đơn → cập nhật trạng thái (PENDING → CONFIRMED → SHIPPING → DELIVERED → COMPLETED/CANCELLED) → thông báo trong ứng dụng đến các bên.

|  |  |
| :---- | :---- |

*Hình 5.4. Giỏ hàng và đặt hàng*

|  |  |
| :---- | :---- |

*Hình 5.5. Pre-order và thanh toán cọc*

*Hình 5.6. Quản lý đơn của Farmer*  
Thanh toán dùng COD và sandbox \[VNPay/MoMo\]; kết quả đối soát giao dịch mô phỏng: sai lệch \[0\] (NFR-09). Phí vận chuyển tính theo \[công thức\], ví dụ \[khoảng cách x km → y đồng\].  
3\. Cơ chế xử lý tồn kho đồng thời (điểm kỹ thuật cốt lõi)  
Khi đặt hàng, M05 gọi reserveStock của M03; M03 khóa dòng lô hàng bằng SELECT ... FOR UPDATE, kiểm tra tồn rồi mới trừ (thiết kế ở Hình 4.4). Nhóm kiểm chứng bằng cách gửi N yêu cầu đồng thời lên cùng một lô hàng.  
**Bảng 5.6. Kết quả kiểm thử đặt hàng đồng thời**

| Lần chạy | Tồn kho ban đầu | Số yêu cầu đồng thời (N ≥ 20\) | Đơn thành công | Đơn bị từ chối | Đơn vượt tồn kho |
| :---- | :---- | :---- | :---- | :---- | :---- |
| 1 | \[ \] | \[ \] | \[ \] | \[ \] | \[0\] |
| 2 | \[ \] | \[ \] | \[ \] | \[ \] | \[0\] |
| 3 | \[ \] | \[ \] | \[ \] | \[ \] | \[0\] |

Kết quả \[0\] đơn vượt tồn kho trong \[3\] lần chạy \[đạt / không đạt\] mục tiêu của độ đo 6 (Bảng 2.3). Mã test case: \[TC-ORD-xx\]; mô tả thiết kế khóa và kết quả đầy đủ ở S2-INC/RN, Phần A mục 4\.  
4\. Thay đổi cơ sở dữ liệu  
Bổ sung các bảng \[orders, order\_items (Bảng 4.5, 4.6), payments, addresses, notifications, audit\_logs, ...\].  
5\. Lỗi tồn đọng và ghi chú kiểm thử  
\[n\] lỗi chưa đóng (xem S2-INC/RN, Phần B mục 4). Hạn chế: trạng thái giao hàng cập nhật thủ công do không tích hợp logistics thật. Ghi chú cho Sprint Test: kịch bản đồng thời cần chạy lặp nhiều lần.

### *5.2.3. Kiểm thử theo Definition of Done*

**Bảng 5.7. Kiểm tra Definition of Done \- Sprint 2**

| STT | Hạng mục DoD | Kết quả | Ghi chú |
| :---- | :---- | :---- | :---- |
| 1 | Code review xong | \[ \] | \[ \] |
| 2 | Unit test pass | \[ \] | \[độ bao phủ logic tồn kho, đặt hàng: ...% (mục tiêu ≥ 80%)\] |
| 3 | Test case theo yêu cầu pass | \[ \] | \[ \] |
| 4 | Merge vào nhánh chính (Git) | \[ \] | \[tag sprint-2\] |

Tỷ lệ hoàn thành sprint: \[x/y \= z%\].

## **5.3. Sprint 3 \- QR truy xuất nguồn gốc, đánh giá sản phẩm, công cụ Admin**

### *5.3.1. Yêu cầu của Sprint*

Sprint Goal (tuần 4, Bảng 1.4): QR truy xuất nguồn gốc, đánh giá sản phẩm, công cụ Admin.  
Sprint 3 gồm 12 PBI (PBI-27 đến PBI-38), 9 PBI mức High, 2 PBI Medium và 1 PBI Low.  
**Bảng 5.8. Danh sách PBI trong Sprint 3**

| STT | Product Backlog Item (PBI) | Độ ưu tiên | Module | Kết quả |
| :---- | :---- | :---- | :---- | :---- |
| 1 | PBI-27: Cập nhật nhật ký trồng trọt | High | M03 | \[ \] |
| 2 | PBI-28: Truy xuất nguồn gốc bằng mã QR | High | M03 | \[ \] |
| 3 | PBI-29: Gửi khiếu nại kèm ảnh/video (24h) | High | M07 | \[ \] |
| 4 | PBI-30: Phản hồi khiếu nại và SLA 24h | High | M07 | \[ \] |
| 5 | PBI-31: Admin phân xử tranh chấp và hoàn tiền | High | M07 | \[ \] |
| 6 | PBI-32: Tự động hoàn tiền khi thiếu hàng Pre-order | High | M07 | \[ \] |
| 7 | PBI-33: Admin khóa tài khoản và gỡ sản phẩm | High | M08 | \[ \] |
| 8 | PBI-34: Lưu trữ minh chứng khiếu nại | High | M07 | \[ \] |
| 9 | PBI-35: Toàn vẹn dữ liệu nhật ký và QR | High | M03 | \[ \] |
| 10 | PBI-36: Đánh giá và bình luận sản phẩm | Medium | M04 | \[ \] |
| 11 | PBI-37: Admin xác minh chứng nhận người bán | Medium | M08 | \[ \] |
| 12 | PBI-38: Hiển thị chỉ số uy tín người bán | Low | M04 | \[ \] |

### *5.3.2. Kết quả xây dựng*

1\. Tổng quan Increment  
Increment S3-INC\_v1.0 hoàn thành \[x\]/12 PBI. Danh sách PBI ở S3-INC/RN, Phần A mục 2\.  
2\. Truy xuất nguồn gốc bằng mã QR (PBI-27, 28, 35\)  
Mỗi lô hàng có traceability\_code (UUID); hệ thống sinh mã QR trỏ đến trang truy xuất công khai, hiển thị thông tin trang trại, ngày thu hoạch và nhật ký trồng trọt (luồng ở Hình 4.5).

|  |  |
| :---- | :---- |

*Hình 5.7. Mã QR và trang truy xuất hợp lệ*

*Hình 5.8 \- mã không tồn tại, báo lỗi*  
Toàn vẹn dữ liệu (NFR-07): \[cách đảm bảo, ví dụ nhật ký chỉ thêm mới, không sửa/xóa mục đã lưu\], kết quả kiểm thử \[ \]. Chi tiết ở S3-INC/RN, Phần A mục 3\.  
3\. Khiếu nại, tranh chấp và hoàn tiền (PBI-29, 30, 31, 32, 34\)  
Buyer gửi khiếu nại kèm ảnh/video trong 24h kể từ lúc đơn "Đã giao" (BR-01); Farmer phản hồi trong SLA 24h; quá hạn hoặc không đồng thuận thì chuyển Admin phân xử. Khi sản lượng thực tế thiếu so với tổng đặt trước, hệ thống tự động hoàn tiền cọc, đơn chuyển REFUNDED (Hình 4.6; BR-05).

*Hình 5.9. Gửi khiếu nại*

*Hình 5.10. Admin phân xử*  
Kết quả kiểm thử hoàn tiền: \[ví dụ: đặt 100kg, thu hoạch 80kg → x đơn bị hoàn, tổng y đồng\]; trường hợp cổng thanh toán lỗi: \[hệ thống ghi nhận và giữ yêu cầu để xử lý lại\].  
4\. Công cụ Admin (PBI-33, 37\)  
Khóa/mở khóa tài khoản vi phạm, gỡ sản phẩm, xác minh chứng nhận người bán.

*Hình 5.11. Trang quản trị*  
Chi tiết ở S3-INC/RN, Phần A mục 4\.  
5\. Đánh giá và uy tín người bán (PBI-36, 38\)  
Điều kiện chỉ người đã mua mới được đánh giá; cách tính chỉ số uy tín.

|  |  |
| :---- | :---- |

*Hình 5.12. Đánh giá và uy tín người bán*  
6\. Thay đổi cơ sở dữ liệu  
Bổ sung các bảng \[farming\_logs, complaints, complaint\_evidences, refunds, reviews, ...\].  
7\. Lỗi tồn đọng và ghi chú kiểm thử  
\[n\] lỗi chưa đóng, trong đó \[n\] lỗi Critical/Major chuyển sang tuần Buffer (S3-INC/RN, Phần B mục 3). Trọng tâm cho Sprint Test: luồng QR, hoàn tiền Pre-order và phân quyền Admin.

### *5.3.3. Kiểm thử theo Definition of Done*

**Bảng 5.9. Kiểm tra Definition of Done \- Sprint 3**

| STT | Hạng mục DoD | Kết quả | Ghi chú |
| :---- | :---- | :---- | :---- |
| 1 | Code review xong | \[ \] | \[ \] |
| 2 | Unit test pass | \[ \] | \[ \] |
| 3 | Test case theo yêu cầu pass | \[ \] | \[ \] |
| 4 | Merge vào nhánh chính (Git) | \[ \] | \[tag sprint-3\] |

Tỷ lệ hoàn thành sprint: \[x/y \= z%\].

## **5.4. Sprint Test \- Kiểm thử tích hợp toàn hệ thống**

### *5.4.1. Yêu cầu của Sprint*

Sprint Goal (tuần 5, Bảng 1.4): Kiểm thử toàn hệ thống trên MVP đủ 3 actor. Đầu ra: Test Report, bug list, bản vá lỗi nghiêm trọng.  
Tổng quan kiểm thử phần mềm. Kiểm thử là hoạt động đối chiếu hành vi thực tế của phần mềm với kết quả mong đợi để phát hiện lỗi và đánh giá mức sẵn sàng bàn giao. Vì Sprint 1-3 đã kiểm thử theo DoD cho từng Increment, Sprint Test tập trung vào các lỗi chỉ xuất hiện khi các Increment chạy cùng nhau.  
**Bảng 5.10. Mô hình kiểm thử áp dụng**

| Mức | Mục đích | Thực hiện |
| :---- | :---- | :---- |
| Kiểm thử đơn vị | Kiểm tra từng hàm/lớp xử lý nghiệp vụ | Dev viết trong mỗi sprint \[JUnit, đo coverage bằng JaCoCo\] |
| Kiểm thử tích hợp | Kiểm tra các module phối hợp đúng (ví dụ M05 gọi M03 trừ tồn kho) | Dev test chéo module \[Postman/ API test\] |
| Kiểm thử hệ thống (end-to-end) | Chạy trọn các luồng nghiệp vụ qua giao diện cho cả 3 actor | PO/QA theo kịch bản trong TC |
| Kiểm thử hồi quy | Bảo đảm sửa lỗi hoặc thêm tính năng ở sprint sau không làm hỏng chức năng cũ | Chạy lại bộ test case luồng chính sau mỗi đợt vá lỗi |

Bên cạnh đó có hai nhóm kiểm thử đặc thù: kiểm thử đồng thời (race condition) cho tồn kho và kiểm thử phi chức năng (hiệu năng, tương thích, bảo mật phân quyền).  
Kế hoạch kiểm thử. Phạm vi: 31 FR và 13 NFR; những mục ngoài phạm vi (load testing quy mô lớn, đa ngôn ngữ, app di động, logistics thật) theo mục 1.5.3. Tiêu chí Pass/Fail theo Bảng 2.3 và Bảng 2.4. Môi trường và seed data theo tiêu chí 4 Bảng 2.4. Để giảm rủi ro số 4 và 5 (Bảng 1.5), mỗi task được một Dev khác test độc lập trước khi PO xác nhận Done. Kế hoạch đầy đủ ở tài liệu TP, Phần A.

### *5.4.2. Kết quả kiểm thử*

**Bảng 5.11. Tổng hợp test case theo Sprint**

| Sprint | Số TC | Đã thực thi | Pass | Fail | Tỷ lệ Pass |
| :---- | :---- | :---- | :---- | :---- | :---- |
| Sprint 1 | \[ \] | \[ \] | \[ \] | \[ \] | \[ \]% |
| Sprint 2 | \[ \] | \[ \] | \[ \] | \[ \] | \[ \]% |
| Sprint 3 | \[ \] | \[ \] | \[ \] | \[ \] | \[ \]% |
| Tích hợp toàn hệ thống | \[ \] | \[ \] | \[ \] | \[ \] | \[ \]% |
| Tổng | \[ \] | \[ \] | \[ \] | \[ \] | \[ \]% |

**Bảng 5.12. Kết quả kiểm thử tích hợp các luồng nghiệp vụ**

| STT | Hạng mục kiểm thử tích hợp | Kết quả | Ghi chú |
| :---- | :---- | :---- | :---- |
| 1 | Farmer đăng sản phẩm → Buyer tìm thấy → đặt hàng thường (COD) → Farmer xác nhận → giao → hoàn tất | \[Pass/Fail\] | \[mã TC\] |
| 2 | Pre-order \+ đặt cọc → thu hoạch đủ → giao hàng | \[ \] | \[ \] |
| 3 | Pre-order \+ đặt cọc → thiếu sản lượng → tự động hoàn tiền (REFUNDED) | \[ \] | \[ \] |
| 4 | Đặt hàng đồng thời N ≥ 20 yêu cầu cùng một lô, không vượt tồn kho | \[ \] | \[ \] |
| 5 | Quét QR của lô hàng sau khi cập nhật nhật ký → hiển thị đúng | \[ \] | \[ \] |
| 6 | Giao hàng → khiếu nại trong 24h → Farmer phản hồi → Admin phân xử, hoàn tiền | \[ \] | \[ \] |
| 7 | Admin khóa tài khoản Farmer → sản phẩm bị gỡ, Buyer không đặt được | \[ \] | \[ \] |
| 8 | Ma trận phân quyền actor × chức năng | \[x/y chặn đúng\] | \[ \] |
| 9 | Hồi quy luồng chính sau khi vá lỗi tuần 5-6 | \[ \] | \[ \] |

**Bảng 5.13. Đối chiếu mục tiêu chất lượng (Bảng 2.3) với kết quả đo**

| Độ đo | Mục tiêu | Kết quả đo | Đạt? |
| :---- | :---- | :---- | :---- |
| 1\. Mức hoàn thành chức năng | 100% PBI High; ≥ 80% tổng | \[ \] | \[ \] |
| 2\. Tỷ lệ test case đạt | ≥ 95%; 100% luồng chính | \[ \] | \[ \] |
| 3\. Mật độ lỗi | ≤ 10 lỗi/KLOC | \[ \] | \[ \] |
| 4\. Lỗi Critical/High tồn đọng | 0 | \[ \] | \[ \] |
| 5\. Độ bao phủ unit test | ≥ 60%; ≥ 80% tồn kho và đặt hàng | \[ \] | \[ \] |
| 6\. Toàn vẹn tồn kho khi đồng thời | 0 đơn vượt tồn | \[ \] | \[ \] |
| 7\. Thời gian phản hồi | P95 ≤ 3 giây, 20 người dùng | \[ \] | \[ \] |
| 8\. Độ dễ sử dụng | ≤ 3 bước; ≥ 80% (≥ 5 người thử) | \[ \] | \[ \] |
| 9\. Độ đúng phân quyền | 100% | \[ \] | \[ \] |
| 10\. Mức tương thích | 4/4 cặp | \[ \] | \[ \] |
| 11\. Bao phủ yêu cầu bởi test case | 100% | \[ \] | \[ \] |

Các độ đo 12-14 (quy trình) được tổng hợp ở Chương VI và PCR vì đo trên toàn dự án.  
**Bảng 5.14. Tổng hợp lỗi theo mức độ nghiêm trọng**

| Mức độ | Phát hiện | Đã đóng | Còn tồn |
| :---- | :---- | :---- | :---- |
| Critical | \[ \] | \[ \] | \[ \] |
| Major | \[ \] | \[ \] | \[ \] |
| Minor | \[ \] | \[ \] | \[ \] |

Một số lỗi tiêu biểu: \[BUG-xx: mô tả ngắn, nguyên nhân, cách khắc phục\]. Danh sách đầy đủ ở TSR, Phần C mục 6\.  
Nhận xét. \[Đánh giá ngắn: các độ đo đạt, các độ đo chưa đạt và lý do; luồng nào rủi ro nhất.\] Số liệu đầy đủ, sai khác so với kế hoạch và đánh giá tổng thể nằm ở Test Summary Report (TSR, Phần C mục 3-7).

### *5.4.3. Tuần Buffer \- sửa lỗi và hoàn thiện*

Theo Bảng 1.4, tuần 6 dành để sửa lỗi tồn đọng và hoàn thiện. Các hạng mục chính: (1) xử lý các lỗi Critical/Major từ Sprint Test; (2) PBI-39 Sao lưu dữ liệu giao dịch (NFR-11); (3) PBI-40 hiệu năng trang chính \< 3s và PBI-41 uptime 99,5%; (4) tối ưu luồng đăng sản phẩm ≤ 3 bước. Kết quả: \[ví dụ: x/y lỗi đã đóng, build ổn định tag v1.0.0\]. Quy trình sao lưu và khôi phục ở DEP, Phần B mục 6\.

## **5.5. Minh chứng GIT**

Thông tin repository. \[link repo\]; nhánh main (bản ổn định) và develop (tích hợp); nhánh tính năng đặt theo quy tắc loại/PBI-mã-mô-tả (Bảng 2.2, dòng 29-32).  
**Bảng 5.15. Thống kê hoạt động Git**

| Chỉ số | Giá trị |
| :---- | :---- |
| Tổng số commit | \[ \] |
| Số nhánh tính năng đã tạo / đã merge | \[ \] / \[ \] |
| Số Pull Request đã merge | \[ \] |
| Tỷ lệ PR có review trước khi merge (độ đo 13, mục tiêu 100%) | \[ \]% |
| Số tag phát hành | \[sprint-1, sprint-2, sprint-3, v1.0.0\] |

**Bảng 5.16. Đóng góp của từng thành viên**

| Thành viên | Vai trò | Số commit | Số PR | Module / PBI chính |
| :---- | :---- | :---- | :---- | :---- |
| Nguyễn Văn Anh | SM, Trưởng nhóm | \[ \] | \[ \] | \[ \] |
| Đồng Thị Hoàng Anh | PO, BA, QA | \[ \] | \[ \] | \[ \] |
| Nguyễn Thành Đạt | Dev FE | \[ \] | \[ \] | \[ \] |
| Phí Công Kiệt | Dev BE | \[ \] | \[ \] | \[ \] |
| Nguyễn Thành Nam | Dev FE | \[ \] | \[ \] | \[ \] |
| Nguyễn Thế Quyền | Dev BE | \[ \] | \[ \] | \[ \] |
| Hà Văn Thắng | Dev FE | \[ \] | \[ \] | \[ \] |
| Nguyễn Đình Mai Vũ | Dev BE | \[ \] | \[ \] | \[ \] |

Minh chứng hình ảnh.

*Hình 5.13. Đồ thị nhánh (network graph)*

*Hình 5.14. Danh sách commit tiêu biểu theo quy ước feat/fix/docs*

*Hình 5.15. Một Pull Request có review và comment*

*Hình 5.16. Bảng Trello*  
Lịch sử commit và nhánh tiêu biểu được trình bày thêm ở SRC, Phần A mục 4; hướng dẫn build/chạy local ở SRC, Phần A mục 5; hướng dẫn triển khai, biến môi trường và tài khoản demo ở DEP.

# **CHƯƠNG VI. KẾT LUẬN**

## **6.1. Đánh giá chất lượng phần mềm**

xxx

## **6.2. Ưu điểm & Nhược điểm của đề tài**

xxx

## **6.3. Hướng phát triển**

xxx

**TÀI LIỆU THAM KHẢO**  
xxx