# PromptForge AI Backend

The backend keeps the AI provider key off the Android device.

## Local
cp .env.example .env
node server.mjs

Health:
GET /health

Generation:
POST /v1/prompt
JSON: {"idea":"...","platform":"ChatGPT","task":"General","language":"en"}

Before public launch, deploy behind HTTPS and add authentication, durable rate limiting, logging, monitoring and server-side entitlement controls.
