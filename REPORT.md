# GitHub 免費資安功能評估報告（public repo：`pponponpon/codeql-scan-trial`）

評估日期：2026-11-05。所有漏洞皆為刻意埋入，答案卷見 [`ANSWER_KEY.md`](ANSWER_KEY.md)；原始掃描結果在 [`scan-results/`](scan-results/)。

## 1. 哪些功能在 public repo 免費、怎麼開、花多少時間

| 功能 | public repo 是否免費 | 開啟方式 | 實測耗時 |
|------|----------------------|----------|----------|
| CodeQL code scanning（advanced setup） | 是 | 自行提交 `.github/workflows/codeql.yml`（本 repo：java-kotlin / javascript-typescript / actions，`security-extended`，Java 用 `build-mode: none`，Actions 全釘 SHA） | 第一輪：push 02:42:50 → run 排入 02:42:58 → 三個 job 02:43:02 開跑 → 02:44:27 全部完成。**java-kotlin 84 秒、javascript-typescript 70 秒、actions 37 秒**（三個 job 並行，整體約 1.5 分鐘）。第二輪同樣約 2 分鐘（含排隊）。 |
| Dependabot alerts | 是 | `gh api -X PUT repos/{owner}/{repo}/vulnerability-alerts` → 204 | 開啟後 1 分鐘內 18 條警示已出現（dependency graph 對 public repo 預設開啟） |
| Dependabot security updates（自動開升版 PR） | 是 | `gh api -X PUT repos/{owner}/{repo}/automated-security-fixes` → 204 | 開啟後至本報告撰寫（約 10 分鐘）尚未產生任何 PR；GitHub 文件說明 security updates 是排程式執行，首批 PR 可能需數小時 |
| Secret scanning | 是 | `gh api -X PATCH repos/{owner}/{repo}` 帶 `security_and_analysis.secret_scanning.status=enabled` → 200 | 即時；目前 0 條（repo 內沒有真的憑證） |
| Push protection | 是 | 同上，`secret_scanning_push_protection.status=enabled` | 即時；測試步驟見第 3 節 |
| Copilot Autofix | 是 | 不需開啟；對既有警示 `POST repos/{owner}/{repo}/code-scanning/alerts/{n}/autofix` → 202 | 三條警示同時送出，**60 秒內全部 `success`** |
| Secret scanning 非供應商樣式（generic password 等）、validity checks | **否**（`secret_scanning_non_provider_patterns: disabled`、`secret_scanning_validity_checks: disabled`，需 GitHub Secret Protection 付費方案） | — | — |

所有 API 對 public repo 都適用，不需改用其他端點。注意 `GET /dependabot/alerts?state=all` 不是合法值，會回空陣列；要全部就不要帶 `state`。

開啟後的 `security_and_analysis`：

```json
{"dependabot_security_updates":{"status":"enabled"},
 "secret_scanning":{"status":"enabled"},
 "secret_scanning_non_provider_patterns":{"status":"disabled"},
 "secret_scanning_push_protection":{"status":"enabled"},
 "secret_scanning_validity_checks":{"status":"disabled"}}
```

## 2. CodeQL 對答案卷的命中（CodeQL CLI 2.27.4，`security-extended`）

### 2.1 第一輪（原始 18 條漏洞 + 1 條對照組 + 4 條誘餌）

| ID | 類型 | 結果 | 實際 query | security severity / level |
|----|------|------|------------|---------------------------|
| VULN-01 | SQL injection | ✅ 抓到 | `java/sql-injection` | high / error |
| VULN-02 | 路徑穿越 | ✅ 抓到（2 條：`new File` 與 `readAllBytes` 各一） | `java/path-injection` | high / error |
| VULN-03 | 命令注入 | ✅ 抓到 | `java/command-line-injection` | critical / error |
| VULN-04 | XXE | ✅ 抓到 | `java/xxe` | critical / error |
| VULN-05 | ObjectInputStream 反序列化 | ✅ 抓到 | `java/unsafe-deserialization` | critical / error |
| VULN-06 | 寫死密碼（static final 欄位比對） | ❌ 沒抓到 | — | — |
| VULN-07 | MD5 存密碼 | ✅ 抓到 | `java/potentially-weak-cryptographic-algorithm`（不是預期的 weak-cryptographic-algorithm / hash-without-salt） | high / warning |
| VULN-08 | SSRF | ✅ 抓到 | `java/ssrf` | critical / error |
| VULN-09 | 反射型 XSS | ✅ 抓到 | `java/xss` | high / error |
| VULN-10 | 開放重導向 | ✅ 抓到 | `java/unvalidated-url-redirection` | medium / error |
| VULN-11 | Log injection | ✅ 抓到 | `java/log-injection` | medium / error |
| VULN-12 | Jackson default typing（mapper 在欄位） | ❌ 沒抓到 | — | — |
| VULN-CONTROL | 缺授權檢查（IDOR） | ❌ 沒抓到（**符合預期**） | — | — |
| VULN-A1 | `bypassSecurityTrustHtml`（`inject()` 取得 sanitizer） | ❌ 沒抓到 | — | — |
| VULN-A2 | `innerHTML` 綁 URL 參數 | ✅ 抓到 | `js/xss` | high / error |
| VULN-A3 | `eval` 使用者字串 | ✅ 抓到 | `js/code-injection` | critical / error |
| VULN-A4 | postMessage 不檢查 origin | ✅ 抓到（2 條：origin 檢查 + 資料進 innerHTML 的 XSS） | `js/missing-origin-check`、`js/xss` | medium / warning、high / error |
| VULN-E1 | Express 命令注入 | ✅ 抓到 | `js/command-line-injection` | critical / error |
| VULN-E2 | Express 路徑穿越（`String(req.query.name \|\| 'x')` + `path.join`） | ❌ 沒抓到 | — | — |
| BAIT-01 | 401/403 handler `PrintWriter.write` 固定字串 | ✅ 無誤報 | — | — |
| BAIT-02 | `new File(@Value 路徑)` | ✅ 無誤報 | — | — |
| BAIT-03 | header 進 MDC | ✅ 無誤報 | — | — |
| BAIT-04 | StringBuilder 拼 HTML、全部 escapeHtml4 | ✅ 無誤報 | — | — |
| （未埋）| Express handler 無 rate limiting | ⚠️ 額外 2 條 | `js/missing-rate-limiting` | high / warning |
| （未埋）| workflow 檔（actions 語言） | 0 條 | — | — |

**第一輪統計**

| 指標 | 數值 |
|------|------|
| 刻意漏洞 recall | 14 / 18 = **77.8%**（Java 10/12、Angular 3/4、Express 1/2） |
| 警示總數 | 18 條（Java 11、JS 7） |
| 對應到刻意漏洞的警示 | 16 條（VULN-02、VULN-A4 各 2 條） |
| 誘餌誤報 | **0 / 4** |
| 噪音（`js/missing-rate-limiting`） | 2 條 |
| Precision（把 rate-limiting 視為噪音） | 16 / 18 = **88.9%**；若視為合理提醒則 100% |

### 2.2 第二輪：針對 4 條漏抓做變體，釐清原因

| 變體 | 改了什麼 | 結果 | 結論 |
|------|----------|------|------|
| VULN-12b | `ObjectMapper` 改在同一方法內 `new` 並 `enableDefaultTyping()` | ✅ `java/unsafe-deserialization` | 漏抓是因為 default typing 狀態不跨欄位初始化追蹤，不是沒規則 |
| VULN-A1b | `DomSanitizer` 改用建構子注入 | ✅ `js/xss` | CodeQL 的 Angular 模型只認建構子注入；Angular 14+ 的 `inject()` 寫法會整個漏掉 |
| VULN-E2b | 拿掉 `String(... \|\| default)`，直接 `path.join(dir, req.query.name)` | ✅ `js/path-injection` | `String()` 包裝讓 path-injection 的 flow label 斷掉（command injection 不受影響） |
| VULN-E2c | 保留 `String()`，改用字串拼接 | ❌ | 同上，確認是 `String()` 造成 |
| VULN-06b | 密碼字面值直接放進 `password.equals("...")` | ❌ | 兩種寫法都沒抓到：`security-extended` 對寫死密碼比對是**真正的規則缺口**（CWE-798 類 query 在此組合下沒有產出） |

第二輪後：23 條刻意漏洞抓到 17 條（73.9%），但去掉「同一漏洞的變體」後實質結論是：**12 種漏洞型態裡 11 種有規則，寫死密碼沒有；另有 3 種型態對寫法敏感**。

### 2.3 嚴重度分布（第二輪後 23 條警示）

critical 7、high 12、medium 4。`error` 級 17 條、`warning` 級 6 條（MD5、origin check、4 條 rate-limiting）。

## 3. Dependabot、Secret scanning、Push protection

### 3.1 Dependabot（`scan-results/dependabot-alerts.json`，共 19 條）

| 誘餌 | 套件 | 抓到的 CVE | 建議升到 |
|------|------|-----------|----------|
| DEPBAIT-01 | jackson-databind 2.9.10.8（Maven） | CVE-2020-36518、CVE-2022-42003、CVE-2022-42004（high）、CVE-2026-91776、CVE-2026-91777（high）、CVE-2026-19032、CVE-2026-54514、CVE-2026-54515、CVE-2026-59889、CVE-2026-77310（medium），共 10 條 | 2.18.11 |
| DEPBAIT-02 | commons-text 1.9（Maven） | CVE-2022-42889（critical） | 1.10.0 |
| DEPBAIT-03 | lodash 4.17.15（npm） | CVE-2020-8203、CVE-2021-23337、CVE-2026-4800（high）、CVE-2020-28500、CVE-2025-13465（medium），共 5 條 | 4.18.0 |
| DEPBAIT-04 | minimist 1.2.5（npm） | CVE-2021-44906（critical） | 1.2.6 |
| 非誘餌 | Angular 開發相依 `minimatch`、`glob` | CVE-2026-118764（open）、CVE-2026-118773（**auto_dismissed**，Dependabot 對 dev scope 低影響的 npm 警示自動關閉） | 11.0.1 / 14.0.2 |

4 個誘餌全部命中，而且比答案卷預期多（2026 年新 CVE）。ANSWER_KEY 預期的 CVE-2021-46877、CVE-2023-35116 沒出現（前者 GitHub advisory 已標 withdrawn，後者有爭議），屬資料庫差異非漏抓。
自動升版 PR：security updates 已啟用（`{"enabled":true,"paused":false}`），到撰寫時尚未開出 PR；需再回頭看。

注意：Maven 生態系 Dependabot 只看 `pom.xml` 直接宣告的相依，不解析傳遞相依（沒有 lockfile）；npm 則會看 `package-lock.json`，所以 Angular 的傳遞相依 `glob`/`minimatch` 才會出現。

### 3.2 Secret scanning

目前 0 條。寫死的 `ADMIN_PASSWORD` 字串沒有被報，因為免費版只比對**供應商 token 樣式**（GitHub PAT、AWS key 等），generic password 偵測屬付費的 non-provider patterns。

### 3.3 Push protection 測試步驟（由使用者自行執行）

1. GitHub 右上角頭像 → **Settings → Developer settings → Personal access tokens → Fine-grained tokens → Generate new token**。
2. Token name：`push-protection-test`；Expiration：**Custom → 明天**（1 天）；Repository access：**Public repositories (read-only)**；Permissions 區塊**全部不勾**（零權限）。按 Generate token，複製（開頭 `github_pat_`）。
3. 在本機 clone 內：
   ```bash
   echo "token=github_pat_貼上這裡" > secret-test.txt
   git add secret-test.txt && git commit -m "push protection test"
   git push origin main
   ```
4. 預期結果：push 被拒，訊息含 `GH013: Repository rule violations found`、`Push cannot contain secrets`，列出 `GitHub Personal Access Token`、檔名、commit，以及一個「bypass」網址。**不要點 bypass**。
5. 清掉本機 commit：`git reset --hard HEAD~1`。
6. 回到 Fine-grained tokens 頁面把該 token **Delete**。
7. 若想對照「已經進到歷史的 secret」：push protection 只擋新 push，既有歷史靠 secret scanning alert 事後報警。

## 4. Copilot Autofix 修補品質（`scan-results/copilot-autofix.json`）

| 警示 | 建議內容 | 評價 |
|------|----------|------|
| #15 `java/sql-injection` | `findByNameUnsafe` 改用 `?` 綁定參數，並把 `/search` 改呼叫既有的 `findByNameSafe` | **正確**，且注意到 repo 裡已有安全版本可重用；無多餘改動 |
| #12 `java/path-injection` | 以 `storageDir` 的 normalized 絕對路徑為基底，`resolve(name).normalize()` 後檢查 `startsWith(base)`，否則回 400 | **正確**，是標準修法（同時擋 `../` 與絕對路徑） |
| #3 `js/command-line-injection` | `exec` 改 `execFile('nslookup', [host])`，並對 `host` 做 hostname/IP 字元 allowlist，無效回 400 | **正確**，雙層防護（不經 shell + 輸入驗證） |

三條都是教科書修法，沒有看到幻覺或破壞功能的建議。API 只回傳文字說明（`description`），實際 diff 要到 Security → Code scanning → 該警示頁面看；此次只看不合併。

## 5. 相較本機工具（FindSecBugs / Semgrep / Trivy）

以下是根據本次結果與這三個工具的已知特性做的判斷，**本次沒有實際跑這三個工具**。

| 面向 | CodeQL（本次實測） | FindSecBugs（Java） | Semgrep（JS，社群規則） | Trivy（套件） |
|------|--------------------|---------------------|-------------------------|---------------|
| 分析方式 | 跨檔案、跨方法的資料流（taint）追蹤 | 以 bytecode 樣式比對為主，taint 範圍有限 | 單檔樣式比對，跨檔案 taint 需 Pro | SBOM 比對 CVE 資料庫 |
| 本次 4 個 BAIT | 0 誤報 | 這四種正是公司專案被誤報過的樣式（它看到 `PrintWriter.write`、`new File`、header 讀取就報） | 不適用 | 不適用 |
| 跨層追蹤（controller → repository 的 SQLi、controller → service 的 MD5） | 抓到 | SQLi 通常抓得到；跨類別 taint 容易斷 | 跨檔案多半斷 | — |
| 寫死密碼 | **沒抓到**（兩種寫法） | 會抓（HARD_CODE_PASSWORD） | 有規則會抓 | — |
| Angular `bypassSecurityTrustHtml` | 只認建構子注入 | — | 社群規則用樣式比對，兩種注入寫法都會報 | — |
| 噪音 | 2～4 條 rate-limiting | 多（以往經驗） | 中 | 低（但傳遞相依 CVE 數量大） |
| 套件 CVE | Dependabot：直接相依（Maven）＋ lockfile（npm），2026 新 CVE 也有 | — | — | 含傳遞相依與容器層，覆蓋比 Dependabot 廣 |
| 邏輯漏洞（VULN-CONTROL） | 抓不到 | 抓不到 | 抓不到 | — |

**CodeQL 多抓到的**：跨層的真正資料流（SQLi、SSRF、XXE、反序列化、log injection），且對 4 個誘餌零誤報，這是 FindSecBugs 做不到的。
**CodeQL 少抓到的**：寫死密碼（規則缺口）、寫法敏感的三種情況（`inject()`、`String()` 包裝、欄位級 ObjectMapper）。
**CodeQL 的邊界**：VULN-CONTROL 這類「少做一個檢查」的授權邏輯漏洞，沒有 source→sink 的資料流可追，任何規則式工具都抓不到，必須靠 code review、威脅建模或 DAST。

## 6. 對公司 private repo 的建議

**結論：不急著買。先把本次驗證過的做法套到本機 pipeline，等有明確痛點再評估 GitHub Code Security。**

判斷依據：

1. **價格**：private repo 要用 CodeQL / Autofix 需 GitHub Code Security（2025-04 起拆售，約 US$30／active committer／月）；secret scanning + push protection 要 GitHub Secret Protection（約 US$19／人／月）。以官方定價頁為準。以公司兩個平台的 committer 數估算年費後，再與一次 pentest 或一名資安工程師的成本比。
2. **CodeQL 的核心優勢是精準度，不是覆蓋面**：本次誤報 0、recall 78%，但它漏掉的（寫死密碼）FindSecBugs 反而抓得到。若公司痛點是 FindSecBugs 誤報太多耗工，CodeQL 值得；若痛點是「怕漏」，CodeQL 不能取代現有工具，只能疊加。
3. **本機可替代方案**：CodeQL CLI 對開源專案免費，但**對私有商業程式碼不得使用**（授權條款）；不能靠「本機跑 CodeQL」繞過付費。可行的是 FindSecBugs + Semgrep（含 taint 模式）+ Trivy，再用本次答案卷同樣方法量化三者的 precision/recall，與本報告數字比。
4. **Dependabot 沒有不可替代性**：Trivy / OWASP Dependency-Check 對 Maven 還能看傳遞相依，比 Dependabot 廣；Dependabot 的優勢只是自動開升版 PR 與 GitHub 整合。
5. **Push protection 是唯一本機工具不易替代的功能**：pre-commit hook（gitleaks）可以做類似事，但靠開發者自律；server 端擋 push 比較可靠。若只買一項，優先考慮 Secret Protection 而非 Code Security。
6. **邏輯漏洞（VULN-CONTROL）兩邊都不會抓**：買工具不能減少這類風險，這部分預算應放在 code review 規範與測試。

建議的下一步：用本 repo 的答案卷在公司 CI 跑 FindSecBugs / Semgrep / Trivy 一次，補上第 5 節缺的實測數字，再做採購決定。
