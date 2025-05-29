#!/bin/bash

echo "===== Extracting Key Java Compilation Errors ====="

# Run a clean
./gradlew clean >/dev/null 2>&1

# Create output file
LOG_FILE="compile_errors.log"
rm -f $LOG_FILE

# Run compilation with maximum info
./gradlew compileDebugJavaWithJavac --stacktrace > $LOG_FILE 2>&1

# Extract the key errors
echo "=== SYMBOL NOT FOUND ERRORS ==="
grep -n -B 1 -A 2 "cannot find symbol" $LOG_FILE | head -n 20
echo ""

echo "=== CLASS NOT FOUND ERRORS ==="
grep -n -B 1 -A 2 "class.*not found" $LOG_FILE | head -n 20
echo ""

echo "=== METHOD NOT FOUND ERRORS ==="
grep -n -B 1 -A 2 "method.*not found" $LOG_FILE | head -n 20
echo ""

echo "=== TYPE MISMATCH ERRORS ==="
grep -n -B 1 -A 2 "incompatible types" $LOG_FILE | head -n 20
echo ""

echo "=== DUPLICATE CLASS ERRORS ==="
grep -n -B 1 -A 2 "duplicate class" $LOG_FILE | head -n 20
echo ""

echo "=== PACKAGE NOT EXISTS ERRORS ==="
grep -n -B 1 -A 2 "package.*does not exist" $LOG_FILE | head -n 20
echo ""

echo "Complete error log saved to $LOG_FILE"
