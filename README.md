# TempSMS Receiver

A personal Android SMS inbox for **temporary/virtual numbers that are legitimately assigned to your account by an SMS provider**.

## Scope
This project consumes messages exposed by an authorized provider API/webhook. It does not intercept SMS, expose numbers belonging to other people, or automate/bypass social-platform verification or anti-abuse controls.

## Architecture
- **Android:** Kotlin + Jetpack Compose client
- **Server:** Node.js + TypeScript HTTP API
- **Provider adapter:** normalized interface for an authorized SMS provider
- **Webhook:** signed provider callbacks
- **OTP extraction:** local/server-side pattern detection for convenience
- **CI:** GitHub Actions Android build

## Server setup
Copy `server/.env.example` to `.env` and set:
- `PORT`
- `APP_TOKEN`
- `WEBHOOK_SECRET`

Run:
```bash
cd server
npm install
npm run dev
```

Provider webhooks should POST to `/webhooks/provider` with the configured signature. The normalized payload is:
```json
{"id":"provider-message-id","number":"+10000000000","sender":"Example","text":"Your code is 123456","receivedAt":"2026-01-01T00:00:00Z"}
```

## Android
Open the `android` directory in Android Studio and run the app. Set the API base URL and token in the app's Settings screen.

No Android SMS permission is required because messages arrive from the authorized provider API.

## Security
Never commit provider API keys, webhook secrets, signing keys, or personal access tokens. Use GitHub Actions secrets and environment variables for deployment.
