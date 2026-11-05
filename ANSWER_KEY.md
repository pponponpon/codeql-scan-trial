# Answer key – planted vulnerabilities, baits and vulnerable dependencies

All items below were planted on purpose. Line numbers refer to the files as committed in this repository.
"Expected CodeQL query" is the query ID we expect to fire with the `security-extended` suite; the actual
results are compared against this list in `REPORT.md`.

## A. Java / Spring Boot (`backend/`)

| ID | File | Line(s) | Type (CWE) | Expected CodeQL query |
|----|------|---------|------------|-----------------------|
| VULN-01 | `backend/src/main/java/com/example/trial/repo/UserRepository.java` | 20–21 (source: `web/UserController.java:43`) | SQL injection (CWE-089) – request param concatenated into `JdbcTemplate.queryForList` | `java/sql-injection` |
| VULN-02 | `backend/src/main/java/com/example/trial/web/FileController.java` | 33, 37 | Path traversal (CWE-022) – `new File(storageDir, name)` from request, no containment check | `java/path-injection` |
| VULN-03 | `backend/src/main/java/com/example/trial/web/SystemController.java` | 19–20 | OS command injection (CWE-078) – `Runtime.exec` with `/bin/sh -c "ping -c 1 " + host` | `java/command-line-injection` |
| VULN-04 | `backend/src/main/java/com/example/trial/web/XmlController.java` | 22–24 | XXE (CWE-611) – `DocumentBuilderFactory` default settings parse request body | `java/xxe` |
| VULN-05 | `backend/src/main/java/com/example/trial/web/ImportController.java` | 26–27 | Unsafe deserialization (CWE-502) – `ObjectInputStream.readObject` on request body | `java/unsafe-deserialization` |
| VULN-06 | `backend/src/main/java/com/example/trial/web/UserController.java` | 30 (used at 67) | Hard-coded credential (CWE-798) – admin password literal compared with request input | `java/hardcoded-credential-comparison` or `java/hardcoded-password-field` |
| VULN-07 | `backend/src/main/java/com/example/trial/service/PasswordService.java` | 16 | Weak hash (CWE-328 / CWE-916) – unsalted MD5 for password storage | `java/weak-cryptographic-algorithm` or `java/hash-without-salt` |
| VULN-08 | `backend/src/main/java/com/example/trial/web/ProxyController.java` | 21–22 | SSRF (CWE-918) – `URL.openStream()` on client-supplied URL | `java/ssrf` |
| VULN-09 | `backend/src/main/java/com/example/trial/web/PageController.java` | 28 | Reflected XSS (CWE-079) – request param written into HTML response | `java/xss` |
| VULN-10 | `backend/src/main/java/com/example/trial/web/PageController.java` | 34 | Open redirect (CWE-601) – `sendRedirect(to)` from request | `java/unvalidated-url-redirection` |
| VULN-11 | `backend/src/main/java/com/example/trial/web/UserController.java` | 66 | Log injection (CWE-117) – request value concatenated into `log.info` | `java/log-injection` (security-extended) |
| VULN-12 | `backend/src/main/java/com/example/trial/web/ImportController.java` | 21, 34 | Unsafe deserialization (CWE-502) – Jackson `enableDefaultTyping()` + `readValue` of request body (old jackson-databind 2.9.10.8) | `java/unsafe-deserialization` |
| VULN-CONTROL | `backend/src/main/java/com/example/trial/web/UserController.java` | 82–91 | Missing authorization / IDOR (CWE-639, CWE-862) – caller `X-User-Id` never compared with `{id}` | **none expected** – logic flaw outside rule-based static analysis |

## B. Angular (`frontend/`)

| ID | File | Line(s) | Type (CWE) | Expected CodeQL query |
|----|------|---------|------------|-----------------------|
| VULN-A1 | `frontend/src/app/app.ts` | 35, 60 | DOM XSS (CWE-079) – `DomSanitizer.bypassSecurityTrustHtml` on URL query param / form input | `js/xss` (Angular `bypassSecurityTrust*` sink) |
| VULN-A2 | `frontend/src/app/app.ts` | 41 | DOM XSS (CWE-079) – `element.innerHTML = <URL query param>` | `js/xss` |
| VULN-A3 | `frontend/src/app/app.ts` | 48 | Code injection (CWE-094) – `eval()` on URL query param | `js/code-injection` |
| VULN-A4 | `frontend/src/app/app.ts` | 23–27 | Missing origin check (CWE-020) – `message` listener trusts `event.data` without checking `event.origin`; data also goes to `innerHTML` | `js/missing-origin-check` (+ `js/xss`) |

## C. Express (`express-app/`)

| ID | File | Line(s) | Type (CWE) | Expected CodeQL query |
|----|------|---------|------------|-----------------------|
| VULN-E1 | `express-app/app.js` | 21–22 | OS command injection (CWE-078) – `exec('nslookup ' + req.query.host)` | `js/command-line-injection` |
| VULN-E2 | `express-app/app.js` | 33–35 | Path traversal (CWE-022) – `fs.readFile(path.join(PUBLIC_DIR, req.query.name))` | `js/path-injection` |

## D. False-positive baits (should **not** be reported)

These patterns were previously reported as false positives by FindSecBugs on real projects. The goal is to see
whether CodeQL's taint tracking distinguishes them from the real vulnerabilities above.

| ID | File | Line(s) | Pattern | Why it is safe |
|----|------|---------|---------|----------------|
| BAIT-01 | `backend/src/main/java/com/example/trial/support/ApiErrorHandler.java` | 22, 24 | 401/403 handler writes JSON via `PrintWriter.write` | Body is made only of fixed string literals; no request data reaches the writer |
| BAIT-02 | `backend/src/main/java/com/example/trial/web/FileController.java` | 24 | `new File(path)` | `path` comes from `@Value("${trial.storage.dir}")` configuration, not from a request |
| BAIT-03 | `backend/src/main/java/com/example/trial/support/RequestIdFilter.java` | 25–27 | Request header read and put into SLF4J `MDC` | Header is used only for log correlation; no decision, no response, no concatenated log message |
| BAIT-04 | `backend/src/main/java/com/example/trial/service/MailTemplateService.java` | 18–30 | HTML mail assembled with `StringBuilder` | Every variable passes through `StringEscapeUtils.escapeHtml4` before being appended |

## E. Vulnerable dependencies (Dependabot baits)

| ID | Manifest | Package / version | Expected advisories (non-exhaustive) |
|----|----------|-------------------|---------------------------------------|
| DEPBAIT-01 | `backend/pom.xml` | `com.fasterxml.jackson.core:jackson-databind` 2.9.10.8 | CVE-2020-36518, CVE-2021-46877, CVE-2022-42003, CVE-2022-42004, CVE-2023-35116 (and older 2.9.x gadget CVEs) |
| DEPBAIT-02 | `backend/pom.xml` | `org.apache.commons:commons-text` 1.9 | CVE-2022-42889 ("Text4Shell") |
| DEPBAIT-03 | `express-app/package.json` | `lodash` 4.17.15 | CVE-2020-8203, CVE-2020-28500, CVE-2021-23337 |
| DEPBAIT-04 | `express-app/package.json` | `minimist` 1.2.5 | CVE-2021-44906 |

Totals: 18 real vulnerabilities (12 Java + 4 Angular + 2 Express), 1 control (VULN-CONTROL), 4 baits, 4 dependency baits.
