package com.example.trial.web;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    // VULN-03: OS command injection - request parameter spliced into a shell command line.
    @GetMapping("/ping")
    public String ping(@RequestParam("host") String host) throws IOException, InterruptedException {
        String[] cmd = {"/bin/sh", "-c", "ping -c 1 " + host};
        Process process = Runtime.getRuntime().exec(cmd);
        try (InputStream in = process.getInputStream()) {
            String output = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            process.waitFor();
            return output;
        }
    }
}
