# No Pain Please

A personal workout tracker built as an Angular PWA backed by a Spring Boot API, Firebase Authentication, and Cloud Firestore.

```
Angular PWA → Firebase Hosting → Spring Boot API on Cloud Run → Cloud Firestore
                    ↓                     ↑
             Firebase Authentication ─────┘ (Firebase ID token)
```

The browser signs a user in with Firebase Authentication and includes its Firebase ID token on each API call. The API verifies that token with the Firebase Admin SDK and scopes all Firestore reads and writes to `users/{uid}`.

## Repository layout

```
frontend/                 Angular standalone PWA
backend/                  Spring Boot 4 / Java 26 REST API
firebase.json             Firebase Hosting configuration
.firebaserc.example       Firebase project alias template
```

## Prerequisites

- Node.js 20+ and npm
- Java 26 and Maven 3.9+
- Firebase CLI (`npm install -g firebase-tools`)
- Google Cloud CLI, authenticated to the target project

## Backend architecture

The workout feature follows hexagonal architecture under
`backend/src/main/java/com/nopainplease/api/workout`:

```text
adapter/in/web              REST controllers and HTTP request/response records
application/port/in         use-case interfaces called by inbound adapters
application/service         framework-free use-case implementations
application/port/out        persistence interfaces required by the application
domain/model                framework-free workout and training records
adapter/out/firestore       Firestore implementations of output ports
adapter/config              Spring bean wiring (the composition root)
```

Dependencies point inward: adapters depend on application ports and domain
types, while the domain and application services have no Spring or Firestore
dependencies. New delivery or persistence mechanisms should be added as
adapters instead of being referenced from the core.

## Configure Firebase

1. Create a Firebase project and enable **Authentication** (Google sign-in is configured by default in this starter) and **Cloud Firestore**.
2. Register a Web app in Firebase and copy its public configuration values into `frontend/src/environments/environment.ts` and `environment.production.ts`.
3. Copy `.firebaserc.example` to `.firebaserc` and set your Firebase project id.
4. For local API development, authenticate Application Default Credentials:

   ```bash
   gcloud auth application-default login
   export GOOGLE_CLOUD_PROJECT=your-project-id
   ```

   On Cloud Run, this is automatic when the service account has Firestore access.

## Run locally

Start the API:

```bash
cd backend
mvn spring-boot:run
```

Start the PWA in another terminal:

```bash
cd frontend
npm install
npm start
```

The frontend runs on `http://localhost:4200` and calls `http://localhost:8080/api` in development. Set `API_CORS_ALLOWED_ORIGIN` before starting the API if you use a different local frontend origin.

## Run with Firebase emulators

The local Angular configuration automatically connects Firebase Authentication to the Auth emulator. The API uses the Firestore emulator under Spring's `local` profile while still validating Firebase ID tokens.

In separate terminals:

```bash
# Terminal 1: emulators
cp .env.local.example .env.local
set -a; . ./.env.local; set +a
./scripts/start-local-emulators.sh

# Terminal 2: API
set -a; . ./.env.local; set +a
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 3: PWA (uses environment.local.ts in development)
cd frontend
npm start
```

Open the PWA at `http://localhost:4200`. The Firebase Emulator UI is at `http://localhost:4000`. The starter uses `no-pain-please-local` as its local project id; change it consistently in [.env.local.example](.env.local.example), [application-local.yml](backend/src/main/resources/application-local.yml), and [environment.local.ts](frontend/src/environments/environment.local.ts) if you prefer another id.

## Debug the Java API in VS Code

Open the repository root in VS Code through WSL and install the recommended
**Extension Pack for Java** in WSL. Use Java 26 and wait for the backend Maven
project to finish importing.

Start the emulators in a terminal at the repository root:

```bash
./scripts/start-local-emulators.sh
```

In **Run and Debug**, select **Java API (local Firebase emulators)** and press
**F5**. This activates the existing `local` Spring profile and supplies the
emulator environment variables. No Google Cloud credentials or `.env.local`
file are needed for this launch configuration. The API listens on port 8080;
stop any existing backend process on that port first.

Start Angular separately with `npm start` from `frontend/`, then open
http://localhost:4200. The VS Code configuration starts only Java; leave the
emulator and Angular terminals running. Use breakpoints to debug and Shift+F5
to stop Java.

## Deploy

Production is deployed manually from your computer. There is no automatic GitHub
deployment yet: this keeps a personal educational project simple and makes every
production change an intentional action.

First-time setup (only once per computer):

```bash
gcloud auth login
gcloud config set project no-pain-please
firebase login
chmod +x scripts/deploy-*.sh
```

For a normal change, publish everything with:

```bash
./scripts/deploy-production.sh --confirm-production
```

The command performs this sequence:

1. `deploy-api.sh` asks Cloud Build to compile the Java API and deploys it to
   Cloud Run in `southamerica-east1`.
2. Cloud Run runs with zero minimum instances, one maximum instance, 1 CPU and
   512 MiB. Its runtime identity is `no-pain-api@no-pain-please.iam.gserviceaccount.com`.
3. `deploy-web.sh` builds Angular and publishes Firebase Hosting plus the
   Firestore security rules.

Use one of these when only one part changed:

```bash
./scripts/deploy-api.sh --confirm-production  # Java API only
./scripts/deploy-web.sh --confirm-production  # Angular site and Firestore rules only
```

`--allow-unauthenticated` on Cloud Run is intentional: it lets browser requests
reach the API, while the application validates Firebase ID tokens for every
`/api/**` request. The Firestore rules deny browser-direct access, and the API
uses the verified token UID to reach only that user's documents.

The scripts are a deployment recipe, not a billing limit. Keep the Cloud Run
spend cap enabled in Google Cloud Billing, and check the deployment output before
sharing a new version.

### Environments

Non-secret deployment values are kept outside the pipeline in
`deploy/environments/production.conf`. This includes the Google/Firebase project,
Cloud Run region and limits, Artifact Registry image location, runtime service
account, and CORS origin. `cloudbuild.yaml` reads this file using its
`_DEPLOY_ENV` substitution.

To introduce another environment later, copy `production.conf` to, for example,
`staging.conf`, change its values, and configure a Cloud Build trigger with
`_DEPLOY_ENV=staging`. Do not put passwords, API tokens, or service-account JSON
keys in these files.

## Firestore shape

The training page reads plans from `GET /api/training-plans`. Plans are stored in
`users/{uid}/trainingPlans/{planId}` with `id`, `title`, `description`, `cardio`,
and `exercises` (`id`, `name`, `sets`, `reps`, `rest`, `group`, `video`). The
frontend contains only TypeScript interfaces, not a built-in exercise catalog.

Import the supplied A/B/C plans into the running local emulator with the UID
from the Auth Emulator UI:

```bash
node scripts/seed-local-training.mjs USER_UID
```

This emulator-only import preserves existing plans. Its JSON file is an import
fixture; subsequent plan changes are read from Firestore. The supplied video
links search YouTube because direct video URLs were not provided; replace the
`video` fields in Firestore with the desired URLs.

Daily progress is saved by `PUT /api/training-days/{YYYY-MM-DD}` into
`users/{uid}/trainingDays/{YYYY-MM-DD}`:

```text
date: "2026-09-12"
plan: "A"
attended: true
exercises: [{ id: "A-1", weightKg: 42.5, completed: true }]
```

Saving updates the same document, so checkbox changes do not create duplicate
days. Dates use America/Sao_Paulo. The next day starts with unchecked exercises,
and the next suggested plan follows the last earlier day with attendance.
Previous weights are shown as references; entering a weight alone does not
record attendance. A completed exercise automatically records attendance.
Each weight is kept with its training date, and the API exposes the chronological
series for a single exercise at `GET /api/exercise-weight-history/{exerciseId}`.
This read model includes zero weights, omits exercises without a registered
weight, and is ready for a future evolution screen.
One plan is recorded per day; select it before entering progress. The history
and date picker allow reviewing and updating earlier days.

`./scripts/start-local-emulators.sh` saves Auth and Firestore state in
`.firebase/local-data` when it stops and reloads it on the next start. The
folder is ignored by Git. Run `node scripts/seed-local-training.mjs USER_UID`
once after creating your local user; the imported A/B/C plans, along with later
training progress, are then preserved across emulator restarts.

For a ready-to-use local account with A/B/C plans and sample history, start the
emulators and run:

```bash
node scripts/seed-local-demo-user.mjs
```

In the development PWA, use **Entrar no modo demonstração**. It signs in as
`demo@no-pain-please.local` (password: `demo-local-password`) and is available
only while the local Auth Emulator is configured.

Validation, with the API on port 8080 and the emulators running:

```bash
cd frontend
npm run build
cd ../backend
mvn test
cd ..
node scripts/verify-local-training.mjs
```

The integration check creates an isolated local account, verifies plan reads
and daily persistence, and removes its own test fixtures afterward.

The original exercise-log collection remains available through `/api/workouts`:

```
users/{uid}/workouts/{workoutId}
  performedAt: timestamp
  exercise: string
  notes: string
  sets: [{ weightKg: number, repetitions: number }]
  createdAt: timestamp
```

The server, not Firestore client rules, is responsible for all workout persistence in this design. Keep Firestore rules closed to direct client access unless you deliberately add a client-side use case.
