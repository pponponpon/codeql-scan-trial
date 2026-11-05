# codeql-scan-trial

**This repository is a deliberately vulnerable test bed.** It exists only to evaluate the
security features GitHub offers for free on public repositories (CodeQL code scanning,
secret scanning + push protection, Dependabot, Copilot Autofix).

Every vulnerability in this repo was planted on purpose and is listed in
[`ANSWER_KEY.md`](ANSWER_KEY.md). Do **not** deploy, reuse, or copy any of this code.
Several dependencies are intentionally pinned to old versions with known CVEs.

## Layout

| Path | Stack | Purpose |
|------|-------|---------|
| `backend/` | Java 21, Spring Boot 4.0.9 (Maven) | REST service with planted Java vulnerabilities and false-positive baits |
| `frontend/` | Angular 20.3.35 | Browser app with planted client-side vulnerabilities |
| `express-app/` | Node 22, Express 5.3.0 | Small server so CodeQL's JavaScript server-side rules have something to scan |
| `.github/workflows/codeql.yml` | GitHub Actions | CodeQL advanced setup (java-kotlin, javascript-typescript, actions; `security-extended`) |

## Markers in the code

* `// VULN-<n>: <type>` – a real, intentionally planted vulnerability.
* `// VULN-CONTROL` – a logic flaw (missing authorization) that rule-based tools are *not* expected to find.
* `// BAIT-<n>` – code that looks suspicious but is safe; used to measure false positives.
* `DEPBAIT-<n>` – a dependency pinned to a version with known CVEs, to exercise Dependabot.

## Build and test locally

```bash
cd backend && mvn test
cd frontend && npm ci && npx ng test --watch=false --browsers=ChromeHeadless
cd express-app && npm ci && npm test
```

The results of the evaluation are written up in [`REPORT.md`](REPORT.md).
