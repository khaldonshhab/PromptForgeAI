# PromptForge AI Backend

The backend keeps the AI provider key off the Android device.

## Local
cp .env.example .env
node server.mjs

Health:
GET /health

Gemini connectivity:
GET /health/ai

`/health` reports configuration only. `/health/ai` makes a bounded `generateContent` probe against the configured model and reports whether it returned text. Set `AI_MODEL` to a model enabled for the configured Gemini API key; the supplied Render service currently recommends `gemini-3.8-flash`.

Generation:
POST /v1/prompt
JSON: {"idea":"...","platform":"ChatGPT","task":"General","language":"en"}

Before public launch, deploy behind HTTPS and add authentication, durable rate limiting, logging, monitoring and server-side entitlement controls.
