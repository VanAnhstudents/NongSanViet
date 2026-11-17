package com.nsv.backend.auth.service;

import com.nsv.backend.auth.dto.MenuItem;
import com.nsv.backend.user.entity.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Danh sách menu/chức năng theo vai trò. Frontend dựa vào đây để chỉ hiển thị mục phù hợp. */
@Component
public class MenuProvider {

    private static final Map<Role, List<MenuItem>> MENUS = Map.of(
            Role.FARMER, List.of(
                    new MenuItem("farm-profile", "Hồ sơ trang trại"),
                    new MenuItem("products", "Sản phẩm của tôi"),
                    new MenuItem("harvest-schedule", "Mùa vụ & thu hoạch"),
                    new MenuItem("farmer-orders", "Đơn hàng cần xử lý")),
            Role.BUYER, List.of(
                    new MenuItem("shop", "Chọn mua nông sản"),
                    new MenuItem("cart", "Giỏ hàng"),
                    new MenuItem("buyer-orders", "Đơn hàng của tôi"),
                    new MenuItem("trace", "Truy xuất nguồn gốc")),
            Role.ADMIN, List.of(
                    new MenuItem("accounts", "Quản lý tài khoản"),
                    new MenuItem("product-moderation", "Kiểm duyệt sản phẩm"),
                    new MenuItem("complaints", "Xử lý khiếu nại"),
                    new MenuItem("reports", "Báo cáo tổng quan")));

    public List<MenuItem> menusFor(Role role) {
        return MENUS.getOrDefault(role, List.of());
    }
}
