#!/usr/bin/env bash
# brew install jsonschema2pojo
jsonschema2pojo --source collection-adapted-schema.json -a JACKSON2 --target . -E -S -p com.epages.restdocs.apispec.postman.model