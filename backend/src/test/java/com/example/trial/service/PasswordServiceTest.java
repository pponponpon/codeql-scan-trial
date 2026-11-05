package com.example.trial.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordServiceTest {

    private final PasswordService service = new PasswordService();

    @Test
    void hashIsMd5Hex() {
        assertEquals("5f4dcc3b5aa765d61d8327deb882cf99", service.hash("password"));
    }

    @Test
    void matchesComparesHashes() {
        assertTrue(service.matches("password", "5f4dcc3b5aa765d61d8327deb882cf99"));
        assertFalse(service.matches("other", "5f4dcc3b5aa765d61d8327deb882cf99"));
        assertFalse(service.matches("password", null));
    }
}
