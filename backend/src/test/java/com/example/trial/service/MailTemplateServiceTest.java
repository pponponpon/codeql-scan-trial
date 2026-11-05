package com.example.trial.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MailTemplateServiceTest {

    private final MailTemplateService service = new MailTemplateService("noreply@example.test");

    @Test
    void escapesEveryVariable() {
        String html = service.buildWelcomeMail("<b>x</b>", "\"q\"@example.test", "a & b");
        assertFalse(html.contains("<b>x</b>"));
        assertTrue(html.contains("&lt;b&gt;x&lt;/b&gt;"));
        assertTrue(html.contains("&quot;q&quot;"));
        assertTrue(html.contains("a &amp; b"));
    }
}
