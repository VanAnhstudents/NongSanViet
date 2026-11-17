package com.nsv.backend.user;

import com.nsv.backend.user.dto.AddressRequest;
import com.nsv.backend.user.dto.AddressResponse;
import com.nsv.backend.user.entity.Address;
import com.nsv.backend.user.entity.Province;
import com.nsv.backend.user.repository.AddressRepository;
import com.nsv.backend.user.repository.ProvinceRepository;
import com.nsv.backend.user.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddressServiceTest {

    private static final long USER_ID = 10L;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ProvinceRepository provinceRepository;

    private final Province hanoi = new Province(1L, "Hà Nội");
    private final Province danang = new Province(2L, "Đà Nẵng");
    private AddressService service;

    @BeforeEach
    void setUp() {
        service = new AddressService(addressRepository, provinceRepository);
        when(provinceRepository.findById(1L)).thenReturn(Optional.of(hanoi));
        when(provinceRepository.findById(2L)).thenReturn(Optional.of(danang));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> {
            Address a = inv.getArgument(0);
            if (a.getId() == null) {
                a.setId(1L);
            }
            return a;
        });
    }

    private Address existing() {
        return Address.builder().id(1L).userId(USER_ID).province(hanoi).receiverName("Cô Lan")
                .receiverPhone("0912345678").addressLine("12 Phố Huế").build();
    }

    @Test
    @DisplayName("TC-SHIP-01: Buyer thêm địa chỉ giao hàng -> lưu thành công, SĐT +84 được chuẩn hóa")
    void tc01_addAddress() {
        AddressResponse result = service.create(USER_ID,
                new AddressRequest(1L, " Cô Lan ", "+84912345678", " 12 Phố Huế "));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.provinceName()).isEqualTo("Hà Nội");
        assertThat(result.receiverName()).isEqualTo("Cô Lan");
        assertThat(result.receiverPhone()).isEqualTo("0912345678");
        assertThat(result.addressLine()).isEqualTo("12 Phố Huế");
    }

    @Test
    @DisplayName("TC-SHIP-01: Buyer sửa địa chỉ giao hàng -> thông tin mới được lưu")
    void tc01_updateAddress() {
        when(addressRepository.findByIdAndUser(1L, USER_ID)).thenReturn(Optional.of(existing()));

        AddressResponse result = service.update(USER_ID, 1L,
                new AddressRequest(2L, "Cô Lan", "0987654321", "5 Bạch Đằng"));

        assertThat(result.provinceId()).isEqualTo(2L);
        assertThat(result.provinceName()).isEqualTo("Đà Nẵng");
        assertThat(result.receiverPhone()).isEqualTo("0987654321");
        assertThat(result.addressLine()).isEqualTo("5 Bạch Đằng");
    }

    @Test
    @DisplayName("TC-SHIP-01: Buyer xóa địa chỉ giao hàng -> địa chỉ bị xóa")
    void tc01_deleteAddress() {
        Address address = existing();
        when(addressRepository.findByIdAndUser(1L, USER_ID)).thenReturn(Optional.of(address));

        service.delete(USER_ID, 1L);

        verify(addressRepository).delete(address);
    }
}
