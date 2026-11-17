package com.nsv.backend.user.service;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.user.dto.ShippingFeeResponse;
import com.nsv.backend.user.entity.Address;
import com.nsv.backend.user.entity.Farm;
import com.nsv.backend.user.repository.AddressRepository;
import com.nsv.backend.user.repository.FarmRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * PBI-21 (FR-38): phí vận chuyển cố định theo khu vực, so sánh tỉnh của trang trại với tỉnh của địa chỉ nhận.
 * Mức phí cấu hình được qua app.shipping.same-province-fee và app.shipping.other-province-fee.
 */
@Service
public class ShippingFeeService {

    private final AddressRepository addressRepository;
    private final FarmRepository farmRepository;
    private final BigDecimal sameProvinceFee;
    private final BigDecimal otherProvinceFee;

    public ShippingFeeService(AddressRepository addressRepository, FarmRepository farmRepository,
            @Value("${app.shipping.same-province-fee:20000}") BigDecimal sameProvinceFee,
            @Value("${app.shipping.other-province-fee:35000}") BigDecimal otherProvinceFee) {
        this.addressRepository = addressRepository;
        this.farmRepository = farmRepository;
        this.sameProvinceFee = sameProvinceFee;
        this.otherProvinceFee = otherProvinceFee;
    }

    @Transactional(readOnly = true)
    public ShippingFeeResponse quote(Long buyerId, Long addressId, Long farmerId) {
        Address address = addressRepository.findByIdAndUser(addressId, buyerId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy địa chỉ của bạn"));
        Farm farm = farmRepository.findByUserId(farmerId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy trang trại của người bán"));

        Long toProvinceId = address.getProvince().getId();
        boolean same = farm.getProvinceId().equals(toProvinceId);
        return new ShippingFeeResponse(addressId, farmerId, farm.getProvinceId(), toProvinceId, same,
                same ? sameProvinceFee : otherProvinceFee);
    }
}
