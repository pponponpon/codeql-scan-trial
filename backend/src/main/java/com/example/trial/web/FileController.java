package com.example.trial.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final File storageDir;

    public FileController(@Value("${trial.storage.dir}") String storagePath) {
        // BAIT-02: new File(path) where path comes from application configuration (@Value), not from a request.
        this.storageDir = new File(storagePath);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
    }

    // VULN-02: path traversal - file name from the request is joined to the base dir without normalisation / containment check.
    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam("name") String name) throws IOException {
        File target = new File(storageDir, name);
        if (!target.exists()) {
            return ResponseEntity.notFound().build();
        }
        byte[] bytes = Files.readAllBytes(target.toPath());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"download.bin\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    @GetMapping("/info")
    public String info() {
        return "storage dir: " + storageDir.getName();
    }
}
