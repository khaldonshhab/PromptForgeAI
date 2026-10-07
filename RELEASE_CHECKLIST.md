# PromptForge AI — Google Play Release Checklist

## Code / Android
- [x] Package name: com.promptforge.ai
- [x] targetSdk 36
- [x] compileSdk 36
- [x] Android App Bundle workflow
- [x] Google Play Billing integration
- [x] Privacy policy accessible from inside the app
- [x] Only INTERNET and Google Play Billing permissions are declared
- [ ] Final production backend deployed over HTTPS
- [ ] Final public privacy-policy URL selected
- [ ] Final 512×512 Play icon prepared
- [ ] Feature graphic prepared
- [ ] Phone screenshots captured from the release candidate
- [ ] Final release AAB signed with the Google Play upload key

## Google Play Console
1. Create the app with package name `com.promptforge.ai`.
2. Use Google Play App Signing.
3. Upload the signed AAB to Internal testing first.
4. Complete App content, Content rating, Target audience, Data safety, and privacy policy.
5. Configure subscriptions:
   - `premium_monthly`
   - `premium_yearly`
6. Verify subscription offers and prices.
7. Test billing purchase and restore.
8. If this is a new personal developer account created after 13 November 2023, run a closed test with at least 12 opted-in testers continuously for 14 days before applying for production access.
9. Complete the production-access questionnaire.
10. Submit production for review.

## GitHub Actions release secrets
Store these as GitHub Actions secrets:
- `ANDROID_KEYSTORE_B64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Never commit the keystore, passwords, provider API keys, or `.env` files.

## AI backend
Deploy `backend/` to HTTPS and set:
- `AI_API_URL`
- `AI_API_MODE`
- `AI_API_KEY`
- `AI_MODEL`

Then configure the HTTPS endpoint in the app under Settings → Backend URL.

## Final QA
Test the exact release candidate on a real Android device:
- Arabic RTL layout.
- English layout.
- Prompt creation.
- Prompt improvement.
- Local engine.
- Remote backend.
- Platform selection.
- Copy/share.
- Saved/history.
- Subscription purchase/restore.
- Reinstall/update behavior.

## Release rule
Do not upload the debug APK to Google Play. Publishing should use the signed AAB.
