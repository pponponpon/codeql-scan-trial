package com.example.trial.service;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MailTemplateService {

    private final String from;

    public MailTemplateService(@Value("${trial.mail.from}") String from) {
        this.from = from;
    }

    // BAIT-04: HTML mail body assembled with StringBuilder, but every variable is HTML-escaped first.
    // A taint-tracking tool should recognise escapeHtml4 as a sanitizer and NOT report XSS here.
    public String buildWelcomeMail(String displayName, String email, String note) {
        String safeName = StringEscapeUtils.escapeHtml4(displayName);
        String safeEmail = StringEscapeUtils.escapeHtml4(email);
        String safeNote = StringEscapeUtils.escapeHtml4(note);
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h1>Welcome, ").append(safeName).append("</h1>");
        sb.append("<p>Your login email is <b>").append(safeEmail).append("</b>.</p>");
        sb.append("<p>").append(safeNote).append("</p>");
        sb.append("<p>From: ").append(StringEscapeUtils.escapeHtml4(from)).append("</p>");
        sb.append("</body></html>");
        return sb.toString();
    }
}
