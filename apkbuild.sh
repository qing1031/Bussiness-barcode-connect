#!/bin/sh

./environment-setup.sh
./gradlew assembleDebug

echo "" && echo 'APK built.' && echo "Run 'fig run android ./s3deploy.sh'" && echo ""