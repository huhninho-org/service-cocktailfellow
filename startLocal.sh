#!/bin/bash

set -e

./gradlew clean build

serverless offline start --stage=local
