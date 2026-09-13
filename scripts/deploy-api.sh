#!/usr/bin/env bash
set -euo pipefail

readonly REPOSITORY_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
readonly DEPLOY_ENV="${DEPLOY_ENV:-production}"
readonly ENVIRONMENT_CONFIG="$REPOSITORY_ROOT/deploy/environments/$DEPLOY_ENV.conf"

if [[ "${1:-}" != '--confirm-production' || $# -ne 1 ]]; then
  echo "Usage: ./scripts/deploy-api.sh --confirm-production" >&2
  exit 1
fi
if [[ ! -f "$ENVIRONMENT_CONFIG" ]]; then
  echo "Unknown deployment environment: $DEPLOY_ENV" >&2
  exit 1
fi
# shellcheck disable=SC1090
source "$ENVIRONMENT_CONFIG"
if ! command -v gcloud >/dev/null; then
  echo 'Google Cloud CLI was not found. Install it and run gcloud auth login first.' >&2
  exit 1
fi
if [[ "$(gcloud config get-value project 2>/dev/null)" != "$GOOGLE_CLOUD_PROJECT" ]]; then
  echo "Select the deployment project first: gcloud config set project $GOOGLE_CLOUD_PROJECT" >&2
  exit 1
fi

if command -v mvn >/dev/null; then
  echo 'Running backend tests...'
  (cd "$REPOSITORY_ROOT/backend" && mvn test)
else
  echo 'Maven was not found; Cloud Build will still compile the API during deployment.'
fi

gcloud run deploy "$CLOUD_RUN_SERVICE" \
  --source "$REPOSITORY_ROOT/backend" \
  --project "$GOOGLE_CLOUD_PROJECT" \
  --region "$CLOUD_RUN_REGION" \
  --allow-unauthenticated \
  --service-account "$CLOUD_RUN_RUNTIME_SERVICE_ACCOUNT" \
  --min "$CLOUD_RUN_MIN_INSTANCES" \
  --max "$CLOUD_RUN_MAX_INSTANCES" \
  --cpu "$CLOUD_RUN_CPU" \
  --memory "$CLOUD_RUN_MEMORY" \
  --set-env-vars "GOOGLE_CLOUD_PROJECT=$GOOGLE_CLOUD_PROJECT,API_CORS_ALLOWED_ORIGIN=$API_CORS_ALLOWED_ORIGIN" \
  --quiet

echo
echo 'API deployed at:'
gcloud run services describe "$CLOUD_RUN_SERVICE" \
  --project "$GOOGLE_CLOUD_PROJECT" \
  --region "$CLOUD_RUN_REGION" \
  --format='value(status.url)'
