package com.nsv.backend.trace.dto;

import com.nsv.backend.trace.entity.FarmingLog;

import java.time.LocalDate;

public record FarmingLogResponse(
    Long id,
    LocalDate logDate,
    String activity,
    String note,
    String imageUrl
) {
    public static FarmingLogResponse from(FarmingLog log) {
        return new FarmingLogResponse(
            log.getId(),
            log.getLogDate(),
            log.getActivity(),
            log.getNote(),
            log.getImageUrl()
        );
    }
}
