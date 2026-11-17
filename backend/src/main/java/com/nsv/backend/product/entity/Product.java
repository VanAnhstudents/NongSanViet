package com.nsv.backend.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Ánh xạ tối thiểu (chỉ đọc) của bảng products, đủ cho PBI-04.
 * Nếu PBI-03 đã có entity Product thì XÓA file này và dùng bản của PBI-03
 * (cần có id, farmerId, name, unit).
 * Giả định products.farmer_id tham chiếu users.id.
 */
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private Long id;

    @Column(name = "farmer_id", nullable = false)
    private Long farmerId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 20)
    private String unit;
}
