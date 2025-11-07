# Interview Notes — Wallet Management System

Use these points when explaining the project in interviews.

## High-level idea
A simple wallet system where users have a wallet (balance) and can create transactions (credit/debit). Data is persisted in MongoDB Atlas. Security is handled using JWT for our APIs and optional OAuth2 login (Google) for social sign-in.

## Components
- **Models**: User, Wallet, Transaction.
- **Repositories**: Spring Data MongoDB repositories for each model.
- **Services**: Business logic (e.g., atomic balance update + transaction creation).
- **Controllers**: REST APIs for authentication, wallets and transactions.
- **Security**:
  - JWT issued on successful login.
  - JwtAuthFilter populates SecurityContext.
  - OAuth2 login enabled for external identity providers.
  - Resource server config protects APIs.
- **Swagger**: Auto-generated API docs at `/swagger-ui.html`.

## Important design choices
- **MongoDB**: document store suits wallets/transactions; transactions collection can grow large — use proper indexes on walletId and timestamp.
- **Consistency**: For wallet balance updates we use optimistic approach (read-modify-write) — in production use MongoDB transactions or server-side atomic increment (`$inc`) operations to avoid race conditions.
- **JWT**: stateless auth — good for scalability; store refresh tokens if longer sessions required.
- **OAuth2**: used for convenience; treat provider identity as mapping to local `User` record.

## Scalability & Security
- **Scaling**: stateless app behind load balancer; MongoDB Atlas for managed scaling; use separate collections/partitions for archival.
- **Security**: store JWT secret in Vault/Env; enforce HTTPS; set short JWT expiry and use refresh tokens; rate-limit sensitive endpoints.

## Common interview questions & sample answers
- **Q: How do you prevent double-spend on wallet?**
  - Use DB-level atomic operations (Mongo `$inc` with conditional checks) or multi-document transactions. Also use idempotency keys for retry-safe APIs.
- **Q: How to design for high throughput?**
  - Scale horizontally, use connection pooling, shard MongoDB by userId, use async processing and event-driven architecture for non-blocking writes.
- **Q: Why JWT and OAuth together?**
  - OAuth is for federated identity (login via Google). JWT is our token format for session management and stateless authentication for API calls.
- **Q: How would you test security?**
  - Unit tests for token creation/validation, integration tests for secured endpoints, pen-test for auth flows, and verify OAuth redirect flows.
