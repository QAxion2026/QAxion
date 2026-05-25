#!/usr/bin/env sh
# Lightweight Gradle wrapper for CI/AppCircle.
# Forces Gradle 8.7 to avoid Gradle 9.x incompatibilities with this Android project.
set -e
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION="8.7"
DIST_NAME="gradle-${GRADLE_VERSION}-bin"
DIST_URL="https://services.gradle.org/distributions/${DIST_NAME}.zip"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$APP_HOME/.gradle}"
DIST_DIR="$GRADLE_USER_HOME/wrapper/dists/gradle-${GRADLE_VERSION}"
GRADLE_BIN="$DIST_DIR/${DIST_NAME}/bin/gradle"
if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$DIST_DIR"
  ZIP_FILE="$DIST_DIR/${DIST_NAME}.zip"
  echo "Downloading Gradle ${GRADLE_VERSION}..."
  if command -v curl >/dev/null 2>&1; then
    curl -L --fail --retry 3 -o "$ZIP_FILE" "$DIST_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP_FILE" "$DIST_URL"
  else
    echo "Neither curl nor wget is available to download Gradle." >&2
    exit 1
  fi
  unzip -q -o "$ZIP_FILE" -d "$DIST_DIR"
  chmod +x "$GRADLE_BIN"
fi
exec "$GRADLE_BIN" "$@"
