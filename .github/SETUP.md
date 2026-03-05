# 🔑 GitHub Actions — Setup Guide

## Required Secrets

Tambahkan secrets ini di: **GitHub Repo → Settings → Secrets and Variables → Actions**

---

### 🔐 Keystore Secrets (Wajib untuk Release)

| Secret Name | Keterangan |
|---|---|
| `KEYSTORE_BASE64` | Keystore file di-encode base64 |
| `KEYSTORE_PASSWORD` | Password keystore |
| `KEY_ALIAS` | Alias key di dalam keystore |
| `KEY_PASSWORD` | Password untuk key alias |

#### Cara membuat Keystore:
```bash
# 1. Generate keystore baru
keytool -genkey -v \
  -keystore streamtv-release.keystore \
  -alias streamtv \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

# 2. Encode ke base64
base64 -i streamtv-release.keystore | tr -d '\n'
# → Salin output ini ke secret KEYSTORE_BASE64
```

#### Set secrets via GitHub CLI:
```bash
# Install gh CLI lalu login
gh auth login

# Set semua secrets sekaligus
gh secret set KEYSTORE_BASE64 < <(base64 -i streamtv-release.keystore | tr -d '\n')
gh secret set KEYSTORE_PASSWORD --body "your_keystore_password"
gh secret set KEY_ALIAS --body "streamtv"
gh secret set KEY_PASSWORD --body "your_key_password"
```

---

### 📣 Notifikasi (Opsional)

| Secret Name | Keterangan |
|---|---|
| `SLACK_WEBHOOK_URL` | Webhook Slack untuk notifikasi release |

---

## 📋 Workflow Summary

### 1. `ci.yml` — CI (Push & PR)
Trigger: setiap push ke `main`, `develop`, `feature/**`, `fix/**`

```
Lint ──┐
       ├──► Build Debug APK
Test ──┘
```

- ✅ Android Lint dengan anotasi di PR
- ✅ Unit tests dengan laporan JUnit
- ✅ Build debug APK
- ✅ Komentar otomatis di PR dengan link APK

---

### 2. `release.yml` — Release (Tags)
Trigger: push tag `v*.*.*` atau manual via `workflow_dispatch`

```
Prepare version ──► Tests ──► Build & Sign APK+AAB ──► GitHub Release
                                     │
                             Changelog generation ──┘
```

- ✅ Otomatis extract version dari tag (`v1.2.3` → `1.2.3`)
- ✅ Generate version code dari semver
- ✅ Build APK + AAB yang sudah di-sign
- ✅ Verifikasi tanda tangan APK
- ✅ Generate changelog dari Git commit history
- ✅ Upload ke GitHub Releases dengan checksums SHA256
- ✅ Notifikasi Slack (opsional)

---

### 3. `pr-check.yml` — PR Validation
Trigger: setiap PR ke `main` atau `develop`

- ✅ Lint, compile, test berjalan paralel
- ✅ PR size checker dengan komentar otomatis
- ✅ Skip otomatis untuk Draft PR

---

### 4. `dependency-check.yml` — Security & Updates
Trigger: setiap Senin jam 9 pagi UTC

- ✅ Scan dependensi outdated
- ✅ OWASP vulnerability check

---

## 🚀 Cara Membuat Release

### Via Git Tag (Recommended):
```bash
# Release stable
git tag v1.0.0
git push origin v1.0.0

# Release candidate
git tag v1.1.0-rc1
git push origin v1.1.0-rc1

# Beta release
git tag v1.2.0-beta1
git push origin v1.2.0-beta1
```

### Via GitHub UI (Manual):
1. Buka **GitHub Repo → Actions**
2. Pilih workflow **🚀 Release**
3. Klik **Run workflow**
4. Isi tag, centang pre-release jika perlu
5. Klik **Run workflow**

---

## 📁 Struktur File

```
.github/
└── workflows/
    ├── ci.yml              # CI — Lint, Test, Build Debug
    ├── release.yml         # Release — Sign, Build, Publish
    ├── pr-check.yml        # PR validation & size check
    └── dependency-check.yml # Weekly security scan
```

---

## 🔍 Tips

- **Cache Gradle**: Semua workflow sudah menggunakan `gradle/actions/setup-gradle` yang otomatis cache build.
- **Concurrency**: CI di-cancel otomatis jika ada push baru ke branch yang sama.
- **Draft PR**: PR check otomatis di-skip untuk draft PR.
- **Artifacts**: Debug APK disimpan 7 hari, Release disimpan 90 hari.
