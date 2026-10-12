# PromptForge AI Backend

The backend keeps the AI provider key off the Android device. Configure Gemini on the server; never place the API key in the APK or share it in chat, screenshots, or logs.

## Run locally

```sh
cp .env.example .env
npm test
npm start
``

Set these provider variables in the runtime environment (for example, Render → Environment):

- `AI_API_URL=https://generativelanguage.googleapis.com/v1beta`
- `AI_API_MODE=gemini`
- `AI_API_KEY` to a server-side Gemini API key
- `AI_MODEL=gemini-2.5-flash` (or another model supported by the key)

The `/health` endpoint reports whether required provider settings are present; it does not validate that the key is accepted by Google or that a prompt generation request succeeds. Use a real authenticated `POST /v1/prompt` request to verify generation.

## Authentication and database

Authentication is implemented in the backend. Configure a unique `PF_AUTH_SECRET` and the appropriate admin credentials for the environment. `PF_DB_URL` and `PF_DB_KEY` enable the remote database; without both, the service uses local storage. Keep test database credentials separate from production and do not copy production secrets into a test service.

Before public launch, review durable rate limiting, monitoring, backups, and server-side access controls.
