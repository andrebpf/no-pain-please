# Repository Guidelines

## Project Structure & Module Organization

`frontend/` contains the Angular 19 standalone PWA. Application code lives in `frontend/src/app`, assets in `frontend/src/assets`, and Firebase settings in `frontend/src/environments`. `backend/` is a Java 21 Spring Boot API under `com.nopainplease.api`; configuration, authentication, and workout features have separate packages. Tests mirror production packages under `backend/src/test/java`. Root files configure Firebase Hosting, Firestore rules, and emulators. `scripts/` contains local seed and integration utilities.

## Build, Test, and Development Commands

- `cd frontend && npm install`: install frontend dependencies.
- `cd frontend && npm start`: serve Angular at `http://localhost:4200`.
- `cd frontend && npm run build`: produce the production PWA and catch template or TypeScript errors.
- `cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local`: run the API against local Firebase emulators.
- `cd backend && mvn test`: run Spring Boot/JUnit tests.
- `firebase emulators:start --only auth,firestore --project no-pain-please-local`: start local Auth and Firestore.
- `node scripts/verify-local-training.mjs`: verify plan reads and daily persistence with an isolated account.

## Coding Style & Naming Conventions

Use two-space indentation for TypeScript, HTML, CSS, and JSON and four spaces for Java. Use standalone Angular components, signals for local state, `inject()` for dependencies, and built-in `@if`/`@for` control flow. Name Angular files in kebab case (`training-days.service.ts`) and Java types in PascalCase. Keep controllers thin and validate request records with Jakarta Validation. Run `npm run build` and `git diff --check` before submitting. No formatter or linter is configured.

## Testing Guidelines

Backend tests use JUnit 5 and Spring Boot Test. Name classes `*Test.java` and test observable behavior, validation boundaries, authentication, and persistence semantics. Add frontend tests only for meaningful interaction or state logic; use Playwright for responsive and end-to-end flows. Never write integration fixtures into a real user account.

## Commit & Pull Request Guidelines

Write short, imperative commit subjects. Existing history uses both plain subjects and Conventional Commit prefixes; prefer forms such as `feat: add workout history` or `fix: preserve daily load`. Pull requests should explain the user-visible behavior, list validation commands, link related issues, and include desktop/mobile screenshots for UI changes. Call out Firestore shape, security-rule, or environment changes explicitly.

## Security & Configuration

Never commit service-account keys, `.env*`, or real Firebase credentials. API access must remain scoped to `users/{uid}`, and direct Firestore client access should remain closed unless deliberately designed and reviewed.
