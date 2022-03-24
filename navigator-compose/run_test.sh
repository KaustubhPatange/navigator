#!/bin/bash

cd navigator-compose
chmod +x gradlew
./gradlew navigator-compose:connectedAndroidTest
./gradlew navigator-compose-hilt:connectedAndroidTest