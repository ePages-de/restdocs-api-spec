#!/bin/bash

set -e # Exit with nonzero exit code if anything fails

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
SECRET_KEYS_FILE="${SCRIPT_DIR}/secret-keys.gpg"

###############################################################################
# Parameter handling
###############################################################################

usage () {
     cat << EOF
DESCRIPTION:
The script publishes the Java libraries of this project to Sonatype or
Maven Local (default).
 
SYNOPSIS:
$0 [-s] [-h]
 
OPTIONS:
    -s   Publish to Sonatype     (Default: off)
    -h   Show this message.
    -?   Show this message.

REQUIRED ENVIRONMENT VARIABLES:
- FILE_ENCRYPTION_PASSWORD: Passphrase for decrypting the signing keys
- SIGNING_KEY_ID
- SIGNING_PASSWORD
- SONATYPE_USERNAME
- SONATYPE_PASSWORD

DEPENDENCIES:
- gpg: https://help.ubuntu.com/community/GnuPrivacyGuardHowto

EOF
}

while getopts "s h ?" option ; do
     case $option in
          s)   PUBLISH_TO_SONATYPE='true'
               ;;
          h )  usage
               exit 0;;
          ? )  usage
               exit 0;;
     esac
done
 

###############################################################################
# Env variables and dependencies
###############################################################################

function check_variable_set() {
  _VARIABLE_NAME=$1
	_VARIABLE_VALUE=${!_VARIABLE_NAME}
	if [[ -z ${_VARIABLE_VALUE} ]]; then
		echo "Missing env variable ${_VARIABLE_NAME}"
		exit 1
	fi
}
check_variable_set FILE_ENCRYPTION_PASSWORD
check_variable_set SIGNING_KEY_ID
check_variable_set SIGNING_PASSWORD
check_variable_set SONATYPE_USERNAME
check_variable_set SONATYPE_PASSWORD

if ! command -v gpg &> /dev/null; then
  echo "gpg not installed. See https://help.ubuntu.com/community/GnuPrivacyGuardHowto"
  exit 1
fi

###############################################################################
# Parameter handling
###############################################################################

# Decrypt signing key
gpg --quiet --batch --yes --decrypt --passphrase="${FILE_ENCRYPTION_PASSWORD}" \
	--output ${SECRET_KEYS_FILE} secret-keys.gpg.enc

if [[ ! -f "${SECRET_KEYS_FILE}" ]]; then
	echo "File ${SECRET_KEYS_FILE} does not exist"
	exit 1
fi

# Determine where to publish the Java archives
if [[ "${PUBLISH_TO_SONATYPE}" == "true" ]]; then
	PUBLISH_GRADLE_TASK="publishToSonatype"
else
	PUBLISH_GRADLE_TASK="publishToMavenLocal"
fi

# Publish
./gradlew ${PUBLISH_GRADLE_TASK} \
	--info \
  --exclude-task :restdocs-api-spec-gradle-plugin:publishToSonatype \
	-Dorg.gradle.project.sonatypeUsername="${SONATYPE_USERNAME}" \
	-Dorg.gradle.project.sonatypePassword="${SONATYPE_PASSWORD}" \
	-Dorg.gradle.project.signing.keyId="${SIGNING_KEY_ID}" \
	-Dorg.gradle.project.signing.password="${SIGNING_PASSWORD}" \
	-Dorg.gradle.project.signing.secretKeyRingFile="${SECRET_KEYS_FILE}"
