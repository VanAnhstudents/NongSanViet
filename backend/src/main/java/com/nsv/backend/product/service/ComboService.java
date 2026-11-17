package com.nsv.backend.product.service;

import com.nsv.backend.product.dto.ComboResponse;
import com.nsv.backend.product.dto.CreateComboRequest;

import java.util.List;

public interface ComboService {
    ComboResponse createCombo(Long farmerId, CreateComboRequest request);
    ComboResponse getComboById(Long comboId);
    List<ComboResponse> getActiveCombos();
}
