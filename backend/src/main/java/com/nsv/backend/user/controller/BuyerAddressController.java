package com.nsv.backend.user.controller;

import com.nsv.backend.common.security.AuthenticatedUser;
import com.nsv.backend.user.dto.AddressRequest;
import com.nsv.backend.user.dto.AddressResponse;
import com.nsv.backend.user.dto.ProvinceResponse;
import com.nsv.backend.user.dto.ShippingFeeResponse;
import com.nsv.backend.user.service.AddressService;
import com.nsv.backend.user.service.ShippingFeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** PBI-21: địa chỉ giao hàng và phí vận chuyển (đường dẫn /api/v1/buyer/** chỉ dành cho BUYER). */
@RestController
@RequestMapping("/api/v1/buyer")
@RequiredArgsConstructor
public class BuyerAddressController {

    private final AddressService addressService;
    private final ShippingFeeService shippingFeeService;

    @GetMapping("/provinces")
    public List<ProvinceResponse> provinces() {
        return addressService.listProvinces();
    }

    @GetMapping("/addresses")
    public List<AddressResponse> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return addressService.list(user.id());
    }

    @PostMapping("/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(@AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody AddressRequest request) {
        return addressService.create(user.id(), request);
    }

    @PutMapping("/addresses/{id}")
    public AddressResponse update(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        return addressService.update(user.id(), id, request);
    }

    @DeleteMapping("/addresses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        addressService.delete(user.id(), id);
    }

    @GetMapping("/shipping-fee")
    public ShippingFeeResponse shippingFee(@AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam Long addressId, @RequestParam Long farmerId) {
        return shippingFeeService.quote(user.id(), addressId, farmerId);
    }
}
