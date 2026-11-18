package com.nsv.backend.admin.backup;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Xuất / nạp dữ liệu giao dịch của CSDL. */
public interface TransactionDataStore {

    /**
     * Các bảng dữ liệu giao dịch, xếp bảng cha trước bảng con (đúng thứ tự nạp khi khôi phục).
     * Tập này đóng kín theo khóa ngoại (không bảng nào ngoài tập tham chiếu tới chúng), nên TRUNCATE được cả tập.
     */
    List<String> TABLES = List.of("orders", "order_addresses", "order_items", "payments",
            "complaints", "complaint_evidences", "refunds", "reviews");

    /** Xuất mọi bảng ở TABLES thành &lt;bảng&gt;.csv trong thư mục (cùng một ảnh chụp nhất quán); trả số dòng mỗi bảng. */
    Map<String, Long> exportAll(Path directory);

    /** Xóa dữ liệu hiện tại của TABLES rồi nạp lại từ thư mục sao lưu, trong một giao dịch (lỗi thì giữ nguyên dữ liệu cũ). */
    void restoreAll(Path directory);
}
