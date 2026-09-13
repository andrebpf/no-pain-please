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
backend/                  Spring Boot 3 / Java 21 REST API
firebase.json             Firebase Hosting configuration
.firebaserc.example       Firebase project alias template
```

## Prerequisites

- Node.js 20+ and npm
- Java 21 and Maven 3.9+
- Firebase CLI (`npm install -g firebase-tools`)
- Google Cloud CLI, authenticated to the target project

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
firebase emulators:start --only auth,firestore --project "$GOOGLE_CLOUD_PROJECT"

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
**Extension Pack for Java** in WSL. Use Java 21 and wait for the backend Maven
project to finish importing.

Start the emulators in a terminal at the repository root:

```bash
firebase emulators:start --only auth,firestore --project no-pain-please-local
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

Build and deploy the API to Cloud Run, substituting your project and preferred region:

```bash
gcloud run deploy no-pain-please-api \
  --source backend \
  --project your-project-id \
  --region southamerica-east1 \
  --allow-unauthenticated \
  --set-env-vars GOOGLE_CLOUD_PROJECT=your-project-id,API_CORS_ALLOWED_ORIGIN=https://your-project-id.web.app
```

`--allow-unauthenticated` is intentional: Cloud Run permits the request through, while the application requires and validates a Firebase ID token for `/api/**` endpoints. Assign the Cloud Run runtime service account a role that can access Firestore (for example, `roles/datastore.user`).

Then put the Cloud Run URL in `frontend/src/environments/environment.production.ts` as `apiBaseUrl`, build, and deploy Hosting:

```bash
cd frontend
npm install
npm run build
cd ..
firebase deploy --only hosting
```

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
One plan is recorded per day; select it before entering progress. The history
and date picker allow reviewing and updating earlier days.

Keep emulator data across restarts by starting Firebase with
`--import=.firebase/local-data --export-on-exit=.firebase/local-data` after an
initial export (`firebase emulators:export .firebase/local-data`).

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
