#!/bin/bash
# Script to find unused Java files

BASE_DIR="/Users/pjayawardana3/Projects/airavata"
cd "$BASE_DIR"

# Find all Java files
find airavata-api/src/main/java modules/*/src/main/java dev-tools/*/src/main/java -name "*.java" 2>/dev/null | while read java_file; do
    # Extract package and class name
    package=$(grep "^package " "$java_file" | sed 's/package //;s/;//')
    class_name=$(basename "$java_file" .java)
    
    # Skip if no package
    if [ -z "$package" ]; then
        continue
    fi
    
    full_class="${package}.${class_name}"
    simple_class="$class_name"
    
    # Check for references
    # 1. Import statements
    import_refs=$(grep -r "import.*${full_class}" airavata-api/src modules dev-tools 2>/dev/null | grep -v "$java_file" | wc -l | tr -d ' ')
    
    # 2. Simple class name references (new ClassName, ClassName., extends ClassName, implements ClassName)
    simple_refs=$(grep -r "\b${simple_class}\b" airavata-api/src modules dev-tools 2>/dev/null | grep -v "$java_file" | grep -v "import.*${full_class}" | wc -l | tr -d ' ')
    
    # 3. String references (for reflection)
    string_refs=$(grep -r "\"${simple_class}\"" airavata-api/src modules dev-tools 2>/dev/null | grep -v "$java_file" | wc -l | tr -d ' ')
    
    total_refs=$((import_refs + simple_refs + string_refs))
    
    # Check if it's a Spring component or other protected type
    is_spring=$(grep -q "@Component\|@Service\|@Repository\|@Controller\|@Configuration" "$java_file" && echo "yes" || echo "no")
    is_interface=$(grep -q "^public interface\|^interface " "$java_file" && echo "yes" || echo "no")
    is_abstract=$(grep -q "^public abstract class\|^abstract class " "$java_file" && echo "yes" || echo "no")
    is_exception=$(echo "$class_name" | grep -q "Exception\|Error" && echo "yes" || echo "no")
    has_main=$(grep -q "public static void main" "$java_file" && echo "yes" || echo "no")
    
    echo "$java_file|$full_class|$total_refs|$is_spring|$is_interface|$is_abstract|$is_exception|$has_main"
done

