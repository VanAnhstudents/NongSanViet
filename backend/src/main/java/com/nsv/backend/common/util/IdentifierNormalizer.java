package com.nsv.backend.common.util;

import java.util.Locale;

/** Chuẩn hóa email / số điện thoại trước khi lưu và tra cứu. */
public final class IdentifierNormalizer {

    private IdentifierNormalizer() {
    }

    /** Trả về email viết thường, đã cắt khoảng trắng; null nếu rỗng. */
    public static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String t = email.trim().toLowerCase(Locale.ROOT);
        return t.isEmpty() ? null : t;
    }

    /** Bỏ khoảng trắng/dấu chấm/gạch, đổi +84 thành 0; null nếu rỗng. */
    public static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        String t = phone.replaceAll("[\\s.\\-]", "");
        if (t.isEmpty()) {
            return null;
        }
        if (t.startsWith("+84")) {
            t = "0" + t.substring(3);
        }
        return t;
    }
}
