#!/usr/bin/env bash
set -euo pipefail

readonly REPOSITORY_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
readonly DEPLOY_ENV="${DEPLOY_ENV:-production}"
readonly ENVIRONMENT_CONFIG="$REPOSITORY_ROOT/deploy/environments/$DEPLOY_ENV.conf"

if [[ "${1:-}" != '--confirm-production' || $# -ne 1 ]]; then
  echo "Usage: ./scripts/deploy-web.sh --confirm-production" >&2
  exit 1
fi
if [[ ! -f "$ENVIRONMENT_CONFIG" ]]; then
  echo "Unknown deployment environment: $DEPLOY_ENV" >&2
  exit 1
fi
# shellcheck disable=SC1090
source "$ENVIRONMENT_CONFIG"
if ! command -v firebase >/dev/null; then
  echo 'Firebase CLI was not found. Install it and run firebase login first.' >&2
  exit 1
fi
if grep -q 'REPLACE_WITH_CLOUD_RUN_URL' "$REPOSITORY_ROOT/frontend/src/environments/environment.production.ts"; then
  echo 'The production API URL is not configured in environment.production.ts.' >&2
  exit 1
fi

echo 'Building the production PWA...'
(cd "$REPOSITORY_ROOT/frontend" && npm run build)

echo 'Publishing Firebase Hosting and Firestore rules...'
(cd "$REPOSITORY_ROOT" && firebase deploy --only hosting,firestore --project "$FIREBASE_PROJECT_ID" --non-interactive)
