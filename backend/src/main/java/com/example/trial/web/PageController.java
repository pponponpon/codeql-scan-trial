package com.example.trial.web;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.trial.service.MailTemplateService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/page")
public class PageController {

    private final MailTemplateService mail;

    public PageController(MailTemplateService mail) {
        this.mail = mail;
    }

    // VULN-09: reflected XSS - request parameter written unescaped into an HTML response.
    @GetMapping("/hello")
    public void hello(@RequestParam("name") String name, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write("<html><body><h1>Hello " + name + "</h1></body></html>");
    }

    // VULN-10: open redirect - redirect target taken verbatim from the request.
    @GetMapping("/go")
    public void go(@RequestParam("to") String to, HttpServletResponse response) throws IOException {
        response.sendRedirect(to);
    }

    // BAIT-04 sink: all inputs escaped in MailTemplateService before being appended.
    @GetMapping("/welcome-mail")
    public String welcomeMail(@RequestParam("name") String name,
                              @RequestParam("email") String email,
                              @RequestParam(value = "note", defaultValue = "") String note) {
        return mail.buildWelcomeMail(name, email, note);
    }
}
