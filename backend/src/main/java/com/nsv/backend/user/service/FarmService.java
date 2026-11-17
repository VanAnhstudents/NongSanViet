package com.nsv.backend.user.service;

import com.nsv.backend.user.dto.CreateFarmRequest;
import com.nsv.backend.user.dto.FarmResponse;

public interface FarmService {
    FarmResponse createMinimalFarmProfile(Long farmerId, CreateFarmRequest request);
}
