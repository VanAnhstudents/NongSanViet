package com.nsv.backend.trace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateFarmingLogRequest(
    @NotNull(message = "Ngày thực hiện canh tác không được để trống")
    LocalDate logDate,

    @NotBlank(message = "Tên hoạt động canh tác không được để trống")
    @Size(max = 100, message = "Tên hoạt động tối đa 100 ký tự")
    String activity,

    String note,

    @Size(max = 500, message = "Đường dẫn hình ảnh tối đa 500 ký tự")
    String imageUrl
) {}
