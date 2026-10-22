package com.college.complaints.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordUtilTest {

    private static final String LEGACY_PASS123_HASH =
            "pbkdf2:65536:N6SpgyhaAvSgBDPoIefzYQ==:qnRIwE6cA42fFU7W54oBZUsmgTUPbwmfYFpvPiOPCZ0=";

    @Test
    void hashesAndVerifiesNewPasswords() {
        String hash = PasswordUtil.hash("s3cret!");
        assertTrue(PasswordUtil.matches("s3cret!", hash));
        assertFalse(PasswordUtil.matches("wrong", hash));
    }

    @Test
    void verifiesExistingLegacyHash() {
        assertTrue(PasswordUtil.matches("pass123", LEGACY_PASS123_HASH));
        assertFalse(PasswordUtil.matches("nope", LEGACY_PASS123_HASH));
    }

    @Test
    void rejectsMalformedHashes() {
        assertFalse(PasswordUtil.matches("x", "not-a-hash"));
        assertFalse(PasswordUtil.matches("x", null));
    }
}
