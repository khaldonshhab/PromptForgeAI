# Windows verification checklist for PromptForgeAI

## 1. Prerequisites

Install and confirm these tools are available in PowerShell:

- Node.js 20+
- Java 17
- Android SDK 36
- Gradle 9.x
- Git
- Android Debug Bridge (`adb`)

Check them with:

```powershell
node -v
java -version
gradle -v
adb version
sdkmanager --version
```

This project is intentionally local-only. It does not use Render, Firebase, or any paid backend.

## 2. Backend env setup

Create a real `.env` file in the backend folder using the template in [backend/.env.example](backend/.env.example).

```env
PORT=8787
AI_PROVIDER=gemini
AI_API_URL=https://generativelanguage.googleapis.com/v1beta
AI_API_MODE=gemini
AI_API_KEY=PASTE_GEMINI_API_KEY_HERE
AI_MODEL=gemini-2.5-flash
PF_AUTH_SECRET=REPLACE_WITH_A_LONG_RANDOM_SECRET
PF_ADMIN_USER=admin
PF_ADMIN_PASSWORD=YourStrongPassword123
PF_ADMIN_PASSWORD_HASH=REPLACE_WITH_HASH_FROM_NODE_SCRIPTS_HASH_PASSWORD_MJS
PF_DATA_DIR=C:/PromptForgeAI/backend/data
MAX_BODY_BYTES=1200000
RATE_LIMIT_PER_MINUTE=20
```

Generate the admin password hash:

```powershell
cd C:\PromptForgeAI
node .\scripts\hash-password.mjs "YourStrongPassword123"
```

Copy the resulting `scrypt$...` value into `PF_ADMIN_PASSWORD_HASH`.

## 3. Local backend startup

```powershell
cd C:\PromptForgeAI\backend
npm install
node server.mjs
```

Health check:

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8787/health
```

Expected result: `{ "ok": true, "configured": true }`.

## 4. Real backend prompt generation check

```powershell
$body = @{
  idea = "أنشئ صورة سينمائية لمسلسل عربي في مدينة دمشق ليلاً، إضاءة دافئة، طابع درامي، شرفة قديمة، شخصيات واقعية"
  platform = "Gemini"
  task = "Image prompt"
  language = "ar"
} | ConvertTo-Json -Compress

Invoke-RestMethod -Method Post -Uri http://localhost:8787/v1/prompt -ContentType "application/json" -Body $body
```

Expected result:

- HTTP 200
- Response includes a `prompt` field
- The prompt is professional English, not a literal translation

## 5. Android local testing setup

### Emulator

Use the emulator's host bridge value:

```text
http://10.0.2.2:8787
```

### Physical device on the same Wi‑Fi network

Use your PC's LAN IP, for example:

```text
http://192.168.1.25:8787
```

### Best local device method (recommended)

When the device or emulator is connected via ADB, use a reverse port so Android can reach the local backend:

```powershell
adb reverse tcp:8787 tcp:8787
```

Then the app should use:

```text
http://localhost:8787
```

This is the most reliable local setup for a real device or emulator.

## 6. Android build

From the repo root:

```powershell
cd C:\PromptForgeAI
gradle :app:assembleDebug
```

If you want to install it directly to a connected device or emulator:

```powershell
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
```

## 7. App runtime checklist

On the device or emulator:

1. Launch the app.
2. Open Settings.
3. Set the backend URL to the local value you are using:
   - emulator: `http://10.0.2.2:8787`
   - adb reverse: `http://localhost:8787`
   - physical device: `http://<your-pc-ip>:8787`
4. Save the setting.
5. Sign in with a valid user account.
6. Generate a prompt using Arabic input.
7. Confirm the output is a useful English prompt.
8. Switch target platforms and verify the prompt structure changes.
9. Verify admin login and normal-user restrictions.
10. Validate logout and re-login persistence.

## 8. Admin verification

```powershell
$body = @{
  username = "admin"
  password = "YourStrongPassword123"
} | ConvertTo-Json -Compress

Invoke-RestMethod -Method Post -Uri http://localhost:8787/admin/login -ContentType "application/json" -Body $body
```

Expected result: a 200 response with a token.

## 9. Final pass criteria

The app is ready for local-only testing when all of these are true:

- Backend health returns `ok: true`
- Gemini generation returns a non-empty English prompt
- Arabic input produces a usable English prompt
- User login works
- Admin login works
- Android debug build completes successfully
- The app points to a local backend, not Render or any paid service
- No secrets are checked into source control
