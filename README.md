# PromptForge AI
Bilingual Android app for creating and improving prompts across a large AI-tool catalog.

## Android / Google Play
- Package: `com.promptforge.ai`
- Target SDK: 36
- Compile SDK: 36
- Publishing format: Android App Bundle (AAB)
- Google Play Billing subscriptions: `premium_monthly`, `premium_yearly`
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

Then enter the HTTPS backend URL in the app Settings. Keep all provider API keys on the server.

## Premium
Create Google Play subscriptions:
- `premium_monthly`
- `premium_yearly`

Do not put provider API keys, keystores, passwords, or `.env` files in the repository.
