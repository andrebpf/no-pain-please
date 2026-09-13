#!/usr/bin/env bash
set -euo pipefail

readonly REPOSITORY_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export DEPLOY_ENV="${DEPLOY_ENV:-production}"

if [[ "${1:-}" != '--confirm-production' || $# -ne 1 ]]; then
  echo "Usage: ./scripts/deploy-production.sh --confirm-production" >&2
  exit 1
fi

"$REPOSITORY_ROOT/scripts/deploy-api.sh" --confirm-production
"$REPOSITORY_ROOT/scripts/deploy-web.sh" --confirm-production

echo
echo 'Production deployment complete: https://no-pain-please.web.app'
