#!/bin/bash
set -e

function check_variable_set() {
  _VARIABLE_NAME=$1
	_VARIABLE_VALUE=${!_VARIABLE_NAME}
	if [[ -z ${_VARIABLE_VALUE} ]]; then
		echo "Missing env variable ${_VARIABLE_NAME}"
		exit 1
	fi
}
check_variable_set GRADLE_PUBLISH_KEY
check_variable_set GRADLE_PUBLISH_SECRET

./gradlew publishPlugins -p restdocs-api-spec-gradle-plugin
