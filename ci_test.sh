#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

if [[ -n "${SONAR_TOKEN}" ]]; then
  SONAR_GRADLE_TASK="sonar"
else
  echo "INFO: Skipping sonar analysis as SONAR_TOKEN is not set"
fi

./gradlew  \
  clean \
  ${SONAR_GRADLE_TASK} \
  build \
  --info
