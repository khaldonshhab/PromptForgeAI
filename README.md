# PromptForge AI
Bilingual Android app for creating and improving prompts across a large AI-tool catalog.

## Android / Google Play
- Package: `com.promptforge.ai`
- Target SDK: 36
- Compile SDK: 36
- Publishing format: Android App Bundle (AAB)
- Free app: no Google Play Billing and no in-app advertising.
- Privacy policy: [PRIVACY.md](PRIVACY.md)
- Google Play preparation: [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md)

Google Play requires new apps submitted from August 31, 2026 to target Android 16 / API 36 or higher. This project is already configured for API 36.

## Build
Use JDK 17 and Android SDK 36. The GitHub Actions workflows install the required SDK/build tools and Gradle.

## GitHub Actions
- Build APK: produces a debug APK for device testing.
- Build AAB: produces an unsigned release AAB for build verification.
- Release AAB: requires a Google Play upload keystore supplied through GitHub Actions secrets and produces a signed AAB suitable for Play Console upload.

## Real AI
Deploy the `backend/` folder to HTTPS and configure:
- `AI_API_URL`
- `AI_API_MODE`
- `AI_API_KEY`
- `AI_MODEL`

Owner admin/user authentication environment variables:
- `PF_AUTH_SECRET` — long random secret used to sign login sessions.
- `PF_DATA_DIR` — persistent directory for `users.json`. The admin dashboard manages free user accounts.

Then enter the HTTPS backend URL in the app Settings. Keep all provider API keys on the server.

## Admin dashboard
Open `/admin` on the deployed backend. Configure `PF_ADMIN_USER`, `PF_ADMIN_PASSWORD_HASH`, and `PF_AUTH_SECRET`. The dashboard can create, disable, enable, reset, and delete free user accounts.

Do not put provider API keys, keystores, passwords, or `.env` files in the repository.

