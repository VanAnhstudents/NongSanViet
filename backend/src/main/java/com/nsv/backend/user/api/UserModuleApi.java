package com.nsv.backend.user.api;

import com.nsv.backend.user.api.dto.FarmSummaryDto;
import com.nsv.backend.user.api.dto.ShippingAddressSnapshotDto;

import java.util.Optional;

public interface UserModuleApi {
    Optional<FarmSummaryDto> findFarmByFarmerId(Long farmerId);
    ShippingAddressSnapshotDto getOwnedShippingAddressSnapshot(Long buyerId, Long addressId);
    String getUserFullName(Long userId);
}
