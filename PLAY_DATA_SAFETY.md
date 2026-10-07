# Google Play Data Safety — Preparation Notes

These are implementation notes, not a substitute for the final Play Console questionnaire. The final declarations must match the exact production backend, SDKs, and hosting configuration.

## Current Android client

Permissions:
- INTERNET

No location, camera, microphone, contacts, SMS, call-log, or storage permissions are declared.

## Data that may be stored locally
- Buyer account username and authentication token after login.\n- User-generated prompts and generated prompt history.
- Saved prompts.
- App preferences such as language and backend URL.

## Advertising and payments

- No AdMob advertising is included.
- No Google Play Billing or in-app paid subscription is included.

## Data transmitted off-device
Only when the user configures a backend and explicitly requests remote generation:
- The prompt text entered for that generation request.

The backend may forward the prompt to the AI provider selected by the server configuration.

## Important production requirement
If analytics, crash-reporting SDKs, advertising SDKs, authentication, cloud databases, or additional third-party services are added later, re-audit the Data Safety form and privacy policy before release.

## Likely Play Console questions
Prepare accurate answers for:
- Whether user data is collected.
- Whether prompt content is collected/transmitted.
- Whether it is optional or required.
- Purpose: app functionality / AI generation.
- Whether data is shared with service providers.
- Whether data is encrypted in transit.
- Retention/deletion behavior.

Do not submit a "no data collected" declaration if a production backend transmits prompt content off-device.
