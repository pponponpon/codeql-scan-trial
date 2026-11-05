package com.example.trial.repo;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // VULN-01: SQL injection - request parameter concatenated straight into the SQL string.
    public List<Map<String, Object>> findByNameUnsafe(String name) {
        String sql = "SELECT id, name, email FROM users WHERE name = '" + name + "'";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> findByNameSafe(String name) {
        return jdbcTemplate.queryForList("SELECT id, name, email FROM users WHERE name = ?", name);
    }

    public Map<String, Object> findById(long id) {
        return jdbcTemplate.queryForMap("SELECT id, name, email FROM users WHERE id = ?", id);
    }

    public int updateEmail(long id, String email) {
        return jdbcTemplate.update("UPDATE users SET email = ? WHERE id = ?", email, id);
    }

    public long insert(String name, String email, String passwordHash) {
        jdbcTemplate.update("INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)", name, email, passwordHash);
        Long id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM users", Long.class);
        return id == null ? -1 : id;
    }

    public String findPasswordHash(String name) {
        List<String> hashes = jdbcTemplate.queryForList("SELECT password_hash FROM users WHERE name = ?", String.class, name);
        return hashes.isEmpty() ? null : hashes.get(0);
    }
}
