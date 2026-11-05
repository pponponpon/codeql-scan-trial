package com.example.trial.web;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.trial.repo.UserRepository;
import com.example.trial.service.PasswordService;
import com.example.trial.support.AccessDeniedException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    // VULN-06: hard-coded credential - admin password embedded in source and used for authentication.
    private static final String ADMIN_PASSWORD = "Tr1al-Adm1n-P@ssw0rd-2026";

    private final UserRepository users;
    private final PasswordService passwords;

    public UserController(UserRepository users, PasswordService passwords) {
        this.users = users;
        this.passwords = passwords;
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam("name") String name) {
        // VULN-01 sink lives in UserRepository.findByNameUnsafe
        return users.findByNameUnsafe(name);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        return users.findById(id);
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "");
        String email = body.getOrDefault("email", "");
        String password = body.getOrDefault("password", "");
        // VULN-07 sink lives in PasswordService.hash (MD5)
        long id = users.insert(name, email, passwords.hash(password));
        return Map.of("id", id, "name", name);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "");
        String password = body.getOrDefault("password", "");
        // VULN-11: log injection - request-controlled value concatenated directly into a log line (CRLF not stripped).
        log.info("Login attempt for user: " + name);
        if ("admin".equals(name) && ADMIN_PASSWORD.equals(password)) {
            return Map.of("ok", true, "role", "admin");
        }
        // VULN-06b (round 2 variant): the password literal is compared directly in equals().
        if ("support".equals(name) && password.equals("Supp0rt-P@ss-2026")) {
            return Map.of("ok", true, "role", "support");
        }
        boolean ok = passwords.matches(password, users.findPasswordHash(name));
        if (!ok) {
            throw new AccessDeniedException(401, "bad credentials");
        }
        return Map.of("ok", true, "role", "user");
    }

    // VULN-CONTROL: missing authorization check (IDOR / broken object-level authorization).
    // The caller is identified by X-User-Id, but nothing verifies that the caller owns {id};
    // any logged-in user can change any other user's e-mail. This is a logic flaw that
    // rule-based static analysis is NOT expected to detect.
    @PutMapping("/{id}/email")
    public ResponseEntity<Map<String, Object>> updateEmail(@PathVariable("id") long id,
                                                           @RequestHeader(value = "X-User-Id", required = false) String callerId,
                                                           @RequestBody Map<String, String> body) {
        if (callerId == null || callerId.isBlank()) {
            throw new AccessDeniedException(401, "login required");
        }
        String email = body.getOrDefault("email", "");
        users.updateEmail(id, email);
        return ResponseEntity.ok(Map.of("id", id, "email", email));
    }
}
