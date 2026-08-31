#!/usr/bin/env bash
# Initialize DB with the schema from before the first mybatis migration
# (20230322085317), then apply every script in migration/migration/scripts
# in order via `migrate up`. Fail the job if any script errors.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_DATABASE="${MYSQL_DATABASE:-fosslight}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-fosslight}"

# Schema baseline: last release before migration scripts started (v1.6.0).
# First tracked script is 20230322085317_create_changelog.sql.
BASELINE_TAG="${BASELINE_TAG:-v1.5.0}"

MIGRATION_HOME="$ROOT_DIR/migration/migration"
MIGRATE_BIN="$ROOT_DIR/migration/mybatis-migrations-3.3.11/bin/migrate"
SCRIPTS_DIR="$MIGRATION_HOME/scripts"

mysql_cli() {
  mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" "$@"
}

echo "==> Validating migration script file names"
shopt -s nullglob
missing_undo=0
script_count=0
for script in "$SCRIPTS_DIR"/*.sql; do
  base="$(basename "$script")"
  [[ "$base" == "bootstrap.sql" ]] && continue
  script_count=$((script_count + 1))
  if [[ ! "$base" =~ ^[0-9]{14}_.+\.sql$ ]]; then
    echo "Invalid migration file name (expected YYYYMMDDHHMMSS_description.sql): $base" >&2
    exit 1
  fi
  if ! grep -qE '^--[[:space:]]*//@UNDO' "$script"; then
    echo "Warning: missing '-- //@UNDO' section: $base"
    missing_undo=$((missing_undo + 1))
  fi
done
echo "Found ${script_count} migration script(s) to apply."
if [[ "$missing_undo" -gt 0 ]]; then
  echo "Note: ${missing_undo} script(s) have no //@UNDO section (allowed for legacy scripts)."
fi

echo "==> Fetching tags (need ${BASELINE_TAG})"
git fetch --tags --force 2>/dev/null || true
if ! git rev-parse -q --verify "refs/tags/${BASELINE_TAG}" >/dev/null; then
  echo "Baseline tag ${BASELINE_TAG} not found." >&2
  exit 1
fi

echo "==> Waiting for MariaDB at ${MYSQL_HOST}:${MYSQL_PORT}"
for _ in $(seq 1 60); do
  if mysqladmin ping -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" --silent; then
    break
  fi
  sleep 2
done
mysqladmin ping -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" --silent

echo "==> Resetting database ${MYSQL_DATABASE}"
mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" -e \
  "DROP DATABASE IF EXISTS \`${MYSQL_DATABASE}\`; CREATE DATABASE \`${MYSQL_DATABASE}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

echo "==> Applying pre-migration schema from ${BASELINE_TAG}:db/initdb.d/fosslight_create.sql"
git show "${BASELINE_TAG}:db/initdb.d/fosslight_create.sql" > /tmp/fosslight_baseline_create.sql
mysql_cli < /tmp/fosslight_baseline_create.sql

echo "==> Configuring migration environment for CI"
ENV_FILE="$MIGRATION_HOME/environments/development.properties"
cp "$ENV_FILE" /tmp/development.properties.ci-bak
sed -i \
  -e "s|^url=.*|url=jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}|" \
  -e "s|^username=.*|username=${MYSQL_USER}|" \
  -e "s|^password=.*|password=${MYSQL_PASSWORD}|" \
  "$ENV_FILE"

cleanup() {
  if [[ -f /tmp/development.properties.ci-bak ]]; then
    mv /tmp/development.properties.ci-bak "$ENV_FILE"
  fi
}
trap cleanup EXIT

chmod +x "$MIGRATE_BIN"

echo "==> Migration status (before up) — all scripts should be pending"
(
  cd "$MIGRATION_HOME"
  "$MIGRATE_BIN" --path=. status
)

echo "==> Running migrate up (apply every script in order)"
(
  cd "$MIGRATION_HOME"
  "$MIGRATE_BIN" --path=. up
)

echo "==> Migration status (after up) — all scripts should be applied"
(
  cd "$MIGRATION_HOME"
  "$MIGRATE_BIN" --path=. status
)

applied_count="$(mysql_cli -N -e 'SELECT COUNT(*) FROM CHANGELOG;')"
echo "CHANGELOG applied count: ${applied_count} / expected ${script_count}"
if [[ "${applied_count}" -ne "${script_count}" ]]; then
  echo "ERROR: expected ${script_count} applied migrations, found ${applied_count} in CHANGELOG." >&2
  exit 1
fi

echo "==> DB migration check passed (baseline ${BASELINE_TAG} → all scripts)"
