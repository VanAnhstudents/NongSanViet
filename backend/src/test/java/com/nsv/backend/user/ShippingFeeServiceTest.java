package com.nsv.backend.user;

import com.nsv.backend.user.dto.ShippingFeeResponse;
import com.nsv.backend.user.entity.Address;
import com.nsv.backend.user.entity.Farm;
import com.nsv.backend.user.entity.Province;
import com.nsv.backend.user.repository.AddressRepository;
import com.nsv.backend.user.repository.FarmRepository;
import com.nsv.backend.user.service.ShippingFeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShippingFeeServiceTest {

    private static final long BUYER_ID = 10L;
    private static final long FARMER_ID = 20L;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private FarmRepository farmRepository;

    private ShippingFeeService service;

    @BeforeEach
    void setUp() {
        service = new ShippingFeeService(addressRepository, farmRepository,
                new BigDecimal("20000"), new BigDecimal("35000"));
    }

    private void givenFarmInProvince(long provinceId) {
        Farm farm = new Farm();
        farm.setUserId(FARMER_ID);
        farm.setProvinceId(provinceId);
        when(farmRepository.findByUserId(FARMER_ID)).thenReturn(Optional.of(farm));
    }

    private void givenAddressInProvince(long provinceId) {
        Address address = Address.builder().id(1L).userId(BUYER_ID).province(new Province(provinceId, "Tỉnh"))
                .receiverName("Cô Lan").receiverPhone("0912345678").addressLine("12 Phố Huế").build();
        when(addressRepository.findByIdAndUser(1L, BUYER_ID)).thenReturn(Optional.of(address));
    }

    @Test
    @DisplayName("TC-SHIP-01: Địa chỉ cùng tỉnh với trang trại -> phí vận chuyển 20.000")
    void tc01_sameProvinceFee() {
        givenFarmInProvince(1L);
        givenAddressInProvince(1L);

        ShippingFeeResponse result = service.quote(BUYER_ID, 1L, FARMER_ID);

        assertThat(result.sameProvince()).isTrue();
        assertThat(result.shippingFee()).isEqualByComparingTo("20000");
    }

    @Test
    @DisplayName("TC-SHIP-01: Địa chỉ khác tỉnh với trang trại -> phí vận chuyển 35.000")
    void tc01_otherProvinceFee() {
        givenFarmInProvince(1L);
        givenAddressInProvince(2L);

        ShippingFeeResponse result = service.quote(BUYER_ID, 1L, FARMER_ID);

        assertThat(result.sameProvince()).isFalse();
        assertThat(result.shippingFee()).isEqualByComparingTo("35000");
    }
}
