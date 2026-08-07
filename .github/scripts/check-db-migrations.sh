#!/usr/bin/env bash
# Apply migration/migration/scripts on top of the latest release schema and fail on errors.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_DATABASE="${MYSQL_DATABASE:-fosslight}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-fosslight}"

MIGRATION_HOME="$ROOT_DIR/migration/migration"
MIGRATE_BIN="$ROOT_DIR/migration/mybatis-migrations-3.3.11/bin/migrate"
SCRIPTS_DIR="$MIGRATION_HOME/scripts"

mysql_cli() {
  mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" "$@"
}

echo "==> Resolving baseline release tag"
git fetch --tags --force 2>/dev/null || true
BASELINE_TAG="$(git tag -l 'v2.*' --sort=-v:refname | head -n 1 || true)"
if [[ -z "${BASELINE_TAG}" ]]; then
  echo "No v2.* git tags found; cannot determine baseline schema." >&2
  exit 1
fi
echo "Baseline tag: ${BASELINE_TAG}"

echo "==> Validating migration script file names"
shopt -s nullglob
missing_undo=0
for script in "$SCRIPTS_DIR"/*.sql; do
  base="$(basename "$script")"
  [[ "$base" == "bootstrap.sql" ]] && continue
  if [[ ! "$base" =~ ^[0-9]{14}_.+\.sql$ ]]; then
    echo "Invalid migration file name (expected YYYYMMDDHHMMSS_description.sql): $base" >&2
    exit 1
  fi
  if ! grep -qE '^--[[:space:]]*//@UNDO' "$script"; then
    echo "Warning: missing '-- //@UNDO' section: $base"
    missing_undo=$((missing_undo + 1))
  fi
done
if [[ "$missing_undo" -gt 0 ]]; then
  echo "Note: ${missing_undo} script(s) have no //@UNDO section (allowed for legacy scripts)."
fi

echo "==> Waiting for MariaDB at ${MYSQL_HOST}:${MYSQL_PORT}"
for _ in $(seq 1 60); do
  if mysqladmin ping -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" --silent; then
    break
  fi
  sleep 2
done
mysqladmin ping -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" --silent

echo "==> Applying baseline schema from ${BASELINE_TAG}:db/initdb.d/fosslight_create.sql"
git show "${BASELINE_TAG}:db/initdb.d/fosslight_create.sql" > /tmp/fosslight_baseline_create.sql
mysql_cli < /tmp/fosslight_baseline_create.sql

echo "==> Creating CHANGELOG table"
mysql_cli <<'SQL'
CREATE TABLE IF NOT EXISTS CHANGELOG (
  ID NUMERIC(20,0) NOT NULL,
  APPLIED_AT VARCHAR(25) NOT NULL,
  DESCRIPTION VARCHAR(255) NOT NULL,
  PRIMARY KEY (ID)
);
SQL

echo "==> Marking migrations present in ${BASELINE_TAG} as already applied"
while IFS= read -r path; do
  base="$(basename "$path")"
  [[ "$base" == "bootstrap.sql" ]] && continue
  id="${base%%_*}"
  [[ "$id" =~ ^[0-9]{14}$ ]] || continue
  desc="${base#*_}"
  desc="${desc%.sql}"
  # Escape single quotes for SQL string literal
  desc_sql="${desc//\'/\'\'}"
  mysql_cli -e "INSERT IGNORE INTO CHANGELOG (ID, APPLIED_AT, DESCRIPTION) VALUES (${id}, DATE_FORMAT(UTC_TIMESTAMP(), '%Y-%m-%d %H:%i:%s'), '${desc_sql}');"
done < <(git ls-tree -r --name-only "${BASELINE_TAG}" -- migration/migration/scripts/)

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

echo "==> Migration status (before up)"
(
  cd "$MIGRATION_HOME"
  "$MIGRATE_BIN" --path=. status
)

echo "==> Running migrate up"
(
  cd "$MIGRATION_HOME"
  "$MIGRATE_BIN" --path=. up
)

echo "==> Migration status (after up)"
(
  cd "$MIGRATION_HOME"
  "$MIGRATE_BIN" --path=. status
)

echo "==> DB migration check passed"
