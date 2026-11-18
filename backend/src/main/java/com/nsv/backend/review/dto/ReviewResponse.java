package com.nsv.backend.review.dto;

import java.time.Instant;

public record ReviewResponse(Long id, Long orderItemId, int rating, String comment, Instant createdAt) {
}
