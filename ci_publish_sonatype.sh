#!/bin/bash
set -e

./gradlew publishToSonatype \
	--info \
	-Dorg.gradle.project.sonatypeUsername="${SONATYPE_USERNAME}" \
	-Dorg.gradle.project.sonatypePassword="${SONATYPE_PASSWORD}" \
 	-Dorg.gradle.project.signing.keyId="${SIGNING_KEY_ID}" \
	-Dorg.gradle.project.signing.password="${SIGNING_PASSWORD}" \
	-Dorg.gradle.project.signing.secretKeyRingFile="${SIGNING_KEYRING_FILE}"


deploy:
- provider: script
  script: "./gradlew binTrayUpload"
  on:
    tags: true
  skip_cleanup: true
- provider: script
  script: "./gradlew publishPlugins -p restdocs-api-spec-gradle-plugin"
  on:
    tags: true
  skip_cleanup: true
