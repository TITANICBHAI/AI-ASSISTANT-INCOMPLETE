#!/bin/bash

echo "========================================"
echo "COMPREHENSIVE ANDROID APP INSPECTION"
echo "========================================"
echo ""

cd app

echo "1. CHECKING MISSING LAYOUTS FOR ACTIVITIES"
echo "-------------------------------------------"
MISSING_LAYOUTS=0
for activity_file in $(find src/main/java/com/aiassistant -name "*Activity.java" | grep -v "test"); do
    activity_name=$(basename "$activity_file" .java)
    layout_name=$(echo "$activity_name" | sed 's/\([A-Z]\)/_\L\1/g' | sed 's/^_//' | sed 's/_activity$//')
    layout_file="src/main/res/layout/activity_${layout_name}.xml"
    
    # Check if activity uses setContentView
    if grep -q "setContentView" "$activity_file" 2>/dev/null; then
        # Extract the layout reference
        layout_ref=$(grep "setContentView" "$activity_file" | grep -oP 'R\.layout\.\K\w+' | head -1)
        if [ ! -z "$layout_ref" ]; then
            if [ ! -f "src/main/res/layout/${layout_ref}.xml" ]; then
                echo "❌ MISSING: src/main/res/layout/${layout_ref}.xml for $(basename $activity_file)"
                MISSING_LAYOUTS=$((MISSING_LAYOUTS + 1))
            fi
        fi
    fi
done
echo "Total missing layouts: $MISSING_LAYOUTS"
echo ""

echo "2. CHECKING MISSING LAYOUTS FOR FRAGMENTS"
echo "-------------------------------------------"
MISSING_FRAG_LAYOUTS=0
for fragment_file in $(find src/main/java/com/aiassistant -name "*Fragment.java" | grep -v "test"); do
    # Check if fragment inflates a layout
    if grep -q "inflate" "$fragment_file" 2>/dev/null; then
        layout_ref=$(grep "inflate.*R\.layout\." "$fragment_file" | grep -oP 'R\.layout\.\K\w+' | head -1)
        if [ ! -z "$layout_ref" ]; then
            if [ ! -f "src/main/res/layout/${layout_ref}.xml" ]; then
                echo "❌ MISSING: src/main/res/layout/${layout_ref}.xml for $(basename $fragment_file)"
                MISSING_FRAG_LAYOUTS=$((MISSING_FRAG_LAYOUTS + 1))
            fi
        fi
    fi
done
echo "Total missing fragment layouts: $MISSING_FRAG_LAYOUTS"
echo ""

echo "3. CHECKING SERVICES IN MANIFEST vs SOURCE"
echo "-------------------------------------------"
MISSING_SERVICES=0
while IFS= read -r service_name; do
    service_file="src/main/java/com/aiassistant/${service_name//.//}.java"
    if [ ! -f "$service_file" ]; then
        echo "❌ MISSING: $service_file (declared in manifest)"
        MISSING_SERVICES=$((MISSING_SERVICES + 1))
    fi
done < <(grep -oP 'android:name="\.\K[^"]+' src/main/AndroidManifest.xml | grep -i service)
echo "Total missing services: $MISSING_SERVICES"
echo ""

echo "4. CHECKING RECEIVERS IN MANIFEST vs SOURCE"
echo "-------------------------------------------"
MISSING_RECEIVERS=0
while IFS= read -r receiver_name; do
    receiver_file="src/main/java/com/aiassistant/${receiver_name//.//}.java"
    if [ ! -f "$receiver_file" ]; then
        echo "❌ MISSING: $receiver_file (declared in manifest)"
        MISSING_RECEIVERS=$((MISSING_RECEIVERS + 1))
    fi
done < <(grep '<receiver' src/main/AndroidManifest.xml -A 1 | grep -oP 'android:name="\.\K[^"]+')
echo "Total missing receivers: $MISSING_RECEIVERS"
echo ""

echo "5. CHECKING FOR MISSING IMPORTS IN JAVA FILES"
echo "-------------------------------------------"
IMPORT_ERRORS=0
for java_file in $(find src/main/java/com/aiassistant -name "*.java" | head -50); do
    # Check for common missing imports (non-Android SDK)
    if grep -q "import com.aiassistant" "$java_file"; then
        while IFS= read -r import_line; do
            import_class=$(echo "$import_line" | sed 's/import //g' | sed 's/;//g' | tr -d '\r')
            # Convert to file path
            import_file=$(echo "$import_class" | sed 's/\./\//g').java
            if [[ "$import_file" == com/aiassistant/* ]]; then
                full_path="src/main/java/$import_file"
                if [ ! -f "$full_path" ]; then
                    echo "❌ $(basename $java_file): Missing import $import_class"
                    IMPORT_ERRORS=$((IMPORT_ERRORS + 1))
                fi
            fi
        done < <(grep "^import com.aiassistant" "$java_file")
    fi
done
echo "Total import errors found (sample): $IMPORT_ERRORS"
echo ""

echo "6. CHECKING DATABASE SETUP"
echo "-------------------------------------------"
if [ -f "src/main/java/com/aiassistant/data/AppDatabase.java" ]; then
    echo "✓ AppDatabase.java exists"
    # Check for common DAOs
    echo "Checking DAO registrations..."
    for dao_file in $(find src/main/java/com/aiassistant/data -name "*Dao.java"); do
        dao_name=$(basename "$dao_file" .java)
        if grep -q "$dao_name" src/main/java/com/aiassistant/data/AppDatabase.java; then
            echo "  ✓ $dao_name registered"
        else
            echo "  ⚠ $dao_name NOT registered in AppDatabase"
        fi
    done
else
    echo "❌ AppDatabase.java MISSING"
fi
echo ""

echo "7. CHECKING ACCESSIBILITY SERVICE CONFIG"
echo "-------------------------------------------"
for xml_config in accessibility_service_config.xml detection_accessibility_service_config.xml; do
    if [ -f "src/main/res/xml/$xml_config" ]; then
        echo "✓ $xml_config exists"
    else
        echo "❌ MISSING: src/main/res/xml/$xml_config"
    fi
done
echo ""

echo "8. CHECKING TFLITE MODELS"
echo "-------------------------------------------"
if [ -d "src/main/assets" ]; then
    tflite_count=$(find src/main/assets -name "*.tflite" 2>/dev/null | wc -l)
    echo "Found $tflite_count TensorFlow Lite models"
    if [ $tflite_count -eq 0 ]; then
        echo "⚠ WARNING: No .tflite models found but TFLite dependencies exist"
    fi
else
    echo "⚠ assets directory missing"
fi
echo ""

echo "9. SUMMARY"
echo "-------------------------------------------"
TOTAL_ISSUES=$((MISSING_LAYOUTS + MISSING_FRAG_LAYOUTS + MISSING_SERVICES + MISSING_RECEIVERS + IMPORT_ERRORS))
echo "Total critical issues found: $TOTAL_ISSUES"
echo ""
if [ $TOTAL_ISSUES -eq 0 ]; then
    echo "✓ No critical file/manifest issues found!"
else
    echo "❌ Found $TOTAL_ISSUES issues that need fixing"
fi
