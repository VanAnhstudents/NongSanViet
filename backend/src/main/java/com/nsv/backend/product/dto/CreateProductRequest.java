package com.nsv.backend.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateProductRequest(
    // Bước 1: Thông tin cơ bản
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 150, message = "Tên sản phẩm tối đa 150 ký tự")
    String name,

    @NotNull(message = "Danh mục sản phẩm không được để trống")
    Long categoryId,

    @NotBlank(message = "Đơn vị tính không được để trống")
    @Size(max = 20, message = "Đơn vị tính tối đa 20 ký tự")
    String unit,

    String description,

    // Bước 2: Hình ảnh sản phẩm (ảnh đầu tiên tự động làm ảnh đại diện)
    @NotEmpty(message = "Cần tải lên ít nhất 1 hình ảnh sản phẩm")
    @Size(max = 10, message = "Tối đa 10 hình ảnh cho mỗi sản phẩm")
    List<@NotBlank(message = "Đường dẫn ảnh không được để trống") @Size(max = 500) String> imageUrls,

    // Bước 3: Giá bán & Số lượng mở bán
    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "0.01", message = "Giá bán phải lớn hơn 0")
    BigDecimal unitPrice,

    @NotNull(message = "Số lượng mở bán không được để trống")
    @DecimalMin(value = "0.01", message = "Số lượng mở bán phải lớn hơn 0")
    BigDecimal quantity,

    LocalDate harvestDate,

    @Pattern(
        regexp = "AVAILABLE|PRE_ORDER",
        message = "Trạng thái mở bán chỉ hỗ trợ AVAILABLE hoặc PRE_ORDER"
    )
    String saleStatus
) {}
