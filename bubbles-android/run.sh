#!/bin/bash
echo "copy"
cp -fr ../bubbles/resources/public/js/ www/
echo "build run"
cordova run android
