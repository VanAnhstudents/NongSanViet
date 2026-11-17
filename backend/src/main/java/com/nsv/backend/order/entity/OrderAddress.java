package com.nsv.backend.order.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", length = 15)
    private String receiverPhone;

    @Column(name = "province_name", nullable = false, length = 100)
    private String provinceName;

    @Column(name = "address_line", nullable = false, length = 255)
    private String addressLine;
}
