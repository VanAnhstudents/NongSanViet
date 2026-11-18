package com.nsv.backend.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Ánh xạ bảng provinces (danh mục tỉnh, dữ liệu do DB cung cấp). */
@Entity
@Table(name = "provinces")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Province {

    @Id
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;
}
