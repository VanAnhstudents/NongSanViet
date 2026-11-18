package com.nsv.backend.user.service;

import com.nsv.backend.common.exception.ApiException;
import com.nsv.backend.common.exception.FieldErrorDetail;
import com.nsv.backend.common.util.IdentifierNormalizer;
import com.nsv.backend.user.dto.AddressRequest;
import com.nsv.backend.user.dto.AddressResponse;
import com.nsv.backend.user.dto.ProvinceResponse;
import com.nsv.backend.user.entity.Address;
import com.nsv.backend.user.entity.Province;
import com.nsv.backend.user.repository.AddressRepository;
import com.nsv.backend.user.repository.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** PBI-21: sổ địa chỉ giao hàng của Buyer (thêm / sửa / xóa / xem). */
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final ProvinceRepository provinceRepository;

    @Transactional(readOnly = true)
    public List<ProvinceResponse> listProvinces() {
        return provinceRepository.findAllByOrderByNameAsc().stream()
                .map(p -> new ProvinceResponse(p.getId(), p.getName())).toList();
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> list(Long userId) {
        return addressRepository.findAllByUser(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AddressResponse create(Long userId, AddressRequest request) {
        Province province = findProvince(request.provinceId());
        Address address = Address.builder()
                .userId(userId)
                .province(province)
                .receiverName(request.receiverName().trim())
                .receiverPhone(IdentifierNormalizer.normalizePhone(request.receiverPhone()))
                .addressLine(request.addressLine().trim())
                .build();
        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(Long userId, Long id, AddressRequest request) {
        Address address = findOwned(userId, id);
        address.setProvince(findProvince(request.provinceId()));
        address.setReceiverName(request.receiverName().trim());
        address.setReceiverPhone(IdentifierNormalizer.normalizePhone(request.receiverPhone()));
        address.setAddressLine(request.addressLine().trim());
        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Address address = findOwned(userId, id);
        try {
            addressRepository.delete(address);
            addressRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(HttpStatus.CONFLICT, "IN_USE",
                    "Địa chỉ này đã được dùng cho đơn hàng nên không thể xóa", null);
        }
    }

    private Address findOwned(Long userId, Long id) {
        return addressRepository.findByIdAndUser(id, userId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy địa chỉ của bạn"));
    }

    private Province findProvince(Long provinceId) {
        return provinceRepository.findById(provinceId)
                .orElseThrow(() -> ApiException.badRequest("Dữ liệu không hợp lệ",
                        List.of(new FieldErrorDetail("provinceId", "Tỉnh/thành phố không tồn tại"))));
    }

    private AddressResponse toResponse(Address a) {
        return new AddressResponse(a.getId(), a.getProvince().getId(), a.getProvince().getName(),
                a.getReceiverName(), a.getReceiverPhone(), a.getAddressLine());
    }
}
