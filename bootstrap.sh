#!/usr/bin/env bash
set -euo pipefail

# Bootstrap script for base_project
# - default: run tests
# - --run : run tests and if successful, start the app with bootRun

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GRADLEW="$ROOT_DIR/../gradlew"

if [ ! -f "$GRADLEW" ]; then
  # fallback to workspace gradlew
  GRADLEW="$ROOT_DIR/../../gradlew"
fi

if [ ! -x "$GRADLEW" ]; then
  chmod +x "$GRADLEW" || true
fi

echo "action=bootstrap status=start dir=$ROOT_DIR"

run_tests() {
  echo "action=bootstrap status=run_tests"
  "$GRADLEW" -p "$ROOT_DIR" clean test --no-daemon
}

run_server() {
  echo "action=bootstrap status=run_server"
  "$GRADLEW" -p "$ROOT_DIR" bootRun
}

case "${1:-}" in
  --run)
    run_tests
    echo "Tests passed. Starting application..."
    run_server
    ;;
  --help|-h)
    echo "Usage: ./bootstrap.sh [--run]"
    echo "  --run  : run tests and if OK start the application (bootRun)"
    ;;
  "")
    run_tests
    ;;
  *)
    echo "Unknown option: $1"
    exit 2
    ;;
esac

echo "action=bootstrap status=finished"

