#!/bin/sh
# Gradle wrapper script for Unix
exec "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" "$@" 2>/dev/null || {
  # Fallback: download gradle if wrapper jar missing
  GRADLE_VERSION="8.11"
  GRADLE_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
  echo "Gradle wrapper jar not found. Please run: gradle wrapper --gradle-version=${GRADLE_VERSION}"
  exit 1
}
