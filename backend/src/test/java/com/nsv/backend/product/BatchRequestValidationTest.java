package com.nsv.backend.product;

import com.nsv.backend.product.dto.BatchCreateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BatchRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final LocalDate date = LocalDate.of(2026, 12, 1);

    private Set<ConstraintViolation<BatchCreateRequest>> validate(Long productId, String qty, String price,
            LocalDate d) {
        return validator.validate(new BatchCreateRequest(productId,
                qty == null ? null : new BigDecimal(qty), price == null ? null : new BigDecimal(price), d));
    }

    private static java.util.List<String> fields(Set<ConstraintViolation<BatchCreateRequest>> v) {
        return v.stream().map(x -> x.getPropertyPath().toString()).toList();
    }

    @Test
    void validRequest_hasNoViolations() {
        assertThat(validate(1L, "10.5", "15000", date)).isEmpty();
    }

    @Test
    void missingProduct_isInvalid() {
        assertThat(fields(validate(null, "10", "15000", date))).containsExactly("productId");
    }

    @Test
    void zeroQuantity_isInvalid() {
        assertThat(fields(validate(1L, "0", "15000", date))).containsExactly("totalQuantity");
    }

    @Test
    void negativeQuantity_isInvalid() {
        assertThat(fields(validate(1L, "-5", "15000", date))).containsExactly("totalQuantity");
    }

    @Test
    void quantityWithTooManyDecimals_isInvalid() {
        assertThat(fields(validate(1L, "1.234", "15000", date))).containsExactly("totalQuantity");
    }

    @Test
    void zeroPrice_isInvalid() {
        assertThat(fields(validate(1L, "10", "0", date))).containsExactly("unitPrice");
    }

    @Test
    void missingQuantityPriceAndDate_areAllReported() {
        assertThat(fields(validate(1L, null, null, null)))
                .containsExactlyInAnyOrder("totalQuantity", "unitPrice", "harvestDate");
    }
}
