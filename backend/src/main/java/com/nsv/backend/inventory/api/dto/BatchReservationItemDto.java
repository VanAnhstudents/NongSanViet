package com.nsv.backend.inventory.api.dto;

import java.math.BigDecimal;

public record BatchReservationItemDto(
    Long batchId,
    BigDecimal quantity
) {}
