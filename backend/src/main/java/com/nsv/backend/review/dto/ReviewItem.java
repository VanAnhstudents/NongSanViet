package com.nsv.backend.review.dto;

import java.time.Instant;

public record ReviewItem(Long id, int rating, String comment, String reviewerName, Instant createdAt) {
}
