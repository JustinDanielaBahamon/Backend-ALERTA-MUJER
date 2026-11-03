# secrets/

Local secrets for `docker compose`. Everything in this folder except this file is ignored by git.

- `firebase-service-account.json`: Firebase service account (Firebase console → Project settings →
  Service accounts → Generate new private key). Auth and Alert mount it at `/app/secrets`.
  After copying it, set `FIREBASE_ENABLED=true` in `.env`; without it the services start with web
  Google sign-in and push turned off. The containers do not run as root, so the file must be
  readable by others (`chmod 644`).
