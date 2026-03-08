# llm-study-01-post

## Security Notes

### Login protection hardening guidance
- Add IP and account-based rate limiting at API gateway or Spring filter layer for `POST /api/auth/login`.
- Add temporary account lockout after repeated failed attempts (e.g., exponential backoff).
- Keep JWT secret in a secure secrets manager and rotate it regularly.
