# Release checklist

## Ready in code
- Android package: com.promptforge.ai
- Arabic/English first-run language selector.
- Prompt generation and improvement.
- Extensible AI platform catalog.
- Local history and saved prompts.
- Optional remote AI backend.
- Google Play Billing integration.

## Owner setup required
1. Google Play Console app and premium_monthly / premium_yearly.
2. HTTPS backend deployment with provider credentials as server secrets.
3. Release signing keystore as GitHub Actions secrets.
4. Public HTTPS privacy policy and terms URLs.
5. Play Console Data safety/content declarations.
6. Store listing assets.

## GitHub Actions release secrets
- ANDROID_KEYSTORE_B64
- ANDROID_KEYSTORE_PASSWORD
- ANDROID_KEY_ALIAS
- ANDROID_KEY_PASSWORD

Never commit keystores, passwords, API keys, or .env files.

## AI backend
Deploy backend/ and set AI_API_URL, AI_API_MODE, AI_API_KEY, AI_MODEL. Then configure the HTTPS endpoint in the app under Settings → Backend URL.