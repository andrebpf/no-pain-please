#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
data_directory="$repository_root/.firebase/local-data"
project_id="${GOOGLE_CLOUD_PROJECT:-no-pain-please-local}"

cd "$repository_root"

arguments=(emulators:start --only auth,firestore --project "$project_id" --export-on-exit="$data_directory")
if [[ -d "$data_directory" ]]; then
  arguments+=(--import="$data_directory")
fi

exec firebase "${arguments[@]}"
