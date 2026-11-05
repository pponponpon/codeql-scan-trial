package com.example.trial.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proxy")
public class ProxyController {

    // VULN-08: SSRF - server fetches whatever URL the client supplies (including internal / metadata addresses).
    @GetMapping
    public String fetch(@RequestParam("url") String url) throws IOException {
        URL target = URI.create(url).toURL();
        try (InputStream in = target.openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
