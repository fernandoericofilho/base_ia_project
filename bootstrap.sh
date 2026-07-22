#!/usr/bin/env bash
set -euo pipefail

# Bootstrap script for base_project
# - default: run tests
# - --run : run tests and if successful, start the app with bootRun

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GRADLEW="$ROOT_DIR/gradlew"

if [ ! -f "$GRADLEW" ]; then
  # fallback for when this script is copied into a parent workspace layout
  GRADLEW="$ROOT_DIR/../gradlew"
fi

if [ ! -x "$GRADLEW" ]; then
  chmod +x "$GRADLEW" || true
fi

echo "action=bootstrap status=start dir=$ROOT_DIR"

setup_claude_plugins() {
  # Makes the Claude Code setup (marketplace + superpowers plugin) ready the
  # moment the project is bootstrapped, instead of a manual one-time step.
  # Safe to run repeatedly (idempotent) and silently skipped if the `claude`
  # CLI isn't installed on this machine.
  echo "action=bootstrap status=setup_claude_plugins"
  if ! command -v claude >/dev/null 2>&1; then
    echo "action=bootstrap status=skip_claude_plugins reason=claude_cli_not_found"
    return 0
  fi
  claude plugin marketplace add anthropics/claude-plugins-official >/dev/null 2>&1 || true
  claude plugin install superpowers@claude-plugins-official >/dev/null 2>&1 || true
  echo "action=bootstrap status=claude_plugins_ready"
}

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
    setup_claude_plugins
    run_tests
    echo "Tests passed. Starting application..."
    run_server
    ;;
  --help|-h)
    echo "Usage: ./bootstrap.sh [--run]"
    echo "  --run  : run tests and if OK start the application (bootRun)"
    ;;
  "")
    setup_claude_plugins
    run_tests
    ;;
  *)
    echo "Unknown option: $1"
    exit 2
    ;;
esac

echo "action=bootstrap status=finished"

