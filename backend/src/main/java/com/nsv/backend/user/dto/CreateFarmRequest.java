package com.nsv.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateFarmRequest(
    @NotBlank(message = "Tên trang trại không được để trống")
    @Size(max = 150, message = "Tên trang trại tối đa 150 ký tự")
    String farmName,

    @NotNull(message = "Tỉnh/thành phố không được để trống")
    Long provinceId,

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    @Size(max = 255, message = "Địa chỉ chi tiết tối đa 255 ký tự")
    String addressLine,

    @NotNull(message = "Loại nông sản không được để trống")
    Long mainCategoryId
) {}