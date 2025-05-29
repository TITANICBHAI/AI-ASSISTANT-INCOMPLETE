#!/bin/bash
# This script fixes the build.gradle file by properly handling the externalNativeBuild section

# Read the file and process it line by line
rm -f build.gradle.fixed
while IFS= read -r line
do
  # Skip lines with externalNativeBuild if we're disabling it
  if [[ $line == *"externalNativeBuild"* ]]; then
    echo "// $line" >> build.gradle.fixed
  # Skip lines with cmake if we're disabling it
  elif [[ $line == *"cmake"* ]]; then
    echo "// $line" >> build.gradle.fixed
  # Skip lines with path if we're disabling it
  elif [[ $line == *"path"* && $line == *"CMakeLists.txt"* ]]; then
    echo "// $line" >> build.gradle.fixed
  else
    echo "$line" >> build.gradle.fixed
  fi
done < build.gradle

# Replace the original with the fixed version
mv build.gradle.fixed build.gradle
