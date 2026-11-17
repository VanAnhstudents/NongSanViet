package com.nsv.backend.common;

import com.nsv.backend.common.util.IdentifierNormalizer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdentifierNormalizerTest {

    @Test
    void normalizeEmail_trimsAndLowercases() {
        assertThat(IdentifierNormalizer.normalizeEmail("  Nong.Dan@Mail.COM ")).isEqualTo("nong.dan@mail.com");
    }

    @Test
    void normalizeEmail_blankBecomesNull() {
        assertThat(IdentifierNormalizer.normalizeEmail("   ")).isNull();
        assertThat(IdentifierNormalizer.normalizeEmail(null)).isNull();
    }

    @Test
    void normalizePhone_convertsPlus84AndStripsSeparators() {
        assertThat(IdentifierNormalizer.normalizePhone("+84 912.345-678")).isEqualTo("0912345678");
        assertThat(IdentifierNormalizer.normalizePhone("0912345678")).isEqualTo("0912345678");
    }

    @Test
    void normalizePhone_blankBecomesNull() {
        assertThat(IdentifierNormalizer.normalizePhone("")).isNull();
    }
}
