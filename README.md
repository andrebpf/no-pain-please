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

```
users/{uid}/workouts/{workoutId}
  performedAt: timestamp
  exercise: string
  notes: string
  sets: [{ weightKg: number, repetitions: number }]
  createdAt: timestamp
```

The server, not Firestore client rules, is responsible for all workout persistence in this design. Keep Firestore rules closed to direct client access unless you deliberately add a client-side use case.
