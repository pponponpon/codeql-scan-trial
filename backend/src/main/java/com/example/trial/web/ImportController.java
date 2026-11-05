package com.example.trial.web;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    // VULN-12: unsafe deserialization via Jackson polymorphic default typing (uses the old jackson-databind 2.9.10.8, DEPBAIT-01).
    @SuppressWarnings("deprecation")
    private final ObjectMapper legacyMapper = new ObjectMapper().enableDefaultTyping();

    // VULN-05: unsafe deserialization - ObjectInputStream reads arbitrary serialized objects from the request body.
    @PostMapping(consumes = "application/octet-stream")
    public String importObject(HttpServletRequest request) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(request.getInputStream())) {
            Object payload = ois.readObject();
            return "imported " + payload.getClass().getName();
        }
    }

    @PostMapping(value = "/json", consumes = "application/json")
    public String importJson(@RequestBody String json) throws IOException {
        Object payload = legacyMapper.readValue(json, Object.class);
        return "imported " + payload.getClass().getName();
    }

    // VULN-12b (round 2 variant): mapper created and default typing enabled locally, right before readValue.
    @SuppressWarnings("deprecation")
    @PostMapping(value = "/json-local", consumes = "application/json")
    public String importJsonLocal(@RequestBody String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        Object payload = mapper.readValue(json, Object.class);
        return "imported " + payload.getClass().getName();
    }
}
