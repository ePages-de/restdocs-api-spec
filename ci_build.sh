#!/bin/bash
set -e

./gradlew clean build coveralls \
  --exclude-task signMavenJavaPublication \
  --exclude-task signArchives
