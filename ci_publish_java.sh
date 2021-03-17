#!/bin/bash
set -e

openssl aes-256-cbc -K $encrypted_7b7bcfd5be68_key -iv $encrypted_7b7bcfd5be68_iv \
  -in secret-keys.gpg.enc \
  -out "${SIGNING_KEYRING_FILE}" \
  -d

./gradlew publishToSonatype \
  --info \
  --exclude-task :restdocs-api-spec-gradle-plugin:publishToSonatype \
  -Dorg.gradle.project.sonatypeUsername="${SONATYPE_USERNAME}" \
  -Dorg.gradle.project.sonatypePassword="${SONATYPE_PASSWORD}" \
  -Dorg.gradle.project.signing.keyId="${SIGNING_KEY_ID}" \
  -Dorg.gradle.project.signing.password="${SIGNING_PASSWORD}" \
  -Dorg.gradle.project.signing.secretKeyRingFile="${SIGNING_KEYRING_FILE}"
