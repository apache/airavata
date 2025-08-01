#! /usr/bin/env bash
#
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

# This script will generate/regenerate the thrift code for Airavata Server Skeletons, Client Stubs
#    and Data Model java beans in java, C++, PHP and Python.

show_usage() {
	echo -e "Usage: $0 [Language to generate stubs]"
	echo ""
	echo "options:"
	echo -e "\tjava Generate/Update Java Stubs"
	echo -e "\tphp Generate/Update PHP Stubs"
	echo -e "\tcpp Generate/Update C++ Stubs"
	echo -e "\tpython Generate/Update Python Stubs."
	echo -e "\tall Generate/Update all stubs (Java, PHP, C++, Python)."
	echo -e "\t-h[elp] Print the usage options of this script"
}

if [ $# -lt 1 ]
then
	show_usage
	exit 1
fi

if [[ $1 == "-h" ||$1 == "--help" ]]
then
	show_usage
	exit 0
fi

REQUIRED_THRIFT_VERSION='0.21.0'
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
AIRAVATA_DIR=`dirname "$SCRIPT_DIR"`

setup() {
    if hash thrift &> /dev/null; then
        THRIFT_EXEC=$(which thrift)
    else
        THRIFT_EXEC=/usr/local/bin/thrift
    fi
    BASEDIR="$AIRAVATA_DIR"
    THRIFTDIR="${BASEDIR}/thrift-interface-descriptions"

    VERSION=$($THRIFT_EXEC -version 2>/dev/null | grep -F "${REQUIRED_THRIFT_VERSION}" |  wc -l)
    if [ "$VERSION" -ne 1 ] ; then
        echo -e "ERROR:\t Apache Thrift version ${REQUIRED_THRIFT_VERSION} is required."
        echo -e "It is either not installed or is not in the path"
        exit 1
    fi

    # Global Constants used across the script
    AIRAVATA_API_IDL_DIR='airavata-apis'
    BASE_TARGET_DIR="$SCRIPT_DIR/target"

    # Thrift files
    CREDENTIAL_API_THRIFT_FILE="${THRIFTDIR}/service-cpis/credential-store-cpi.thrift"
    ORCHESTRATOR_THRIFT_FILE="${THRIFTDIR}/service-cpis/orchestrator-cpi.thrift"
    REGISTRY_THRIFT_FILE="${THRIFTDIR}/service-cpis/registry-api.thrift"
    SHARING_API_THRIFT_FILE="${THRIFTDIR}/service-cpis/sharing_cpi.thrift"
    PROFILE_SERVICE_THRIFT_FILE="${THRIFTDIR}/service-cpis/profile-service-cpi.thrift"

    PHP_THRIFT_FILE="${THRIFTDIR}/stubs_php.thrift"
    JAVA_THRIFT_FILE="${THRIFTDIR}/stubs_java.thrift"

    JAVA_SRC_DIR='../airavata-api/src/main/java'
    PHP_SDK_DIR='../dev-tools/airavata-php-sdk/lib'
    CPP_SDK_DIR='../dev-tools/airavata-cpp-sdk/lib/airavata/'
    PYTHON_SDK_DIR='../dev-tools/airavata-python-sdk/airavata/'


    # Initialize the thrift arguments.
    #  Since most of the Airavata API and Data Models have includes, use recursive option by default.
    #  Generate all the files in target directory
    THRIFT_ARGS="-r -o ${BASEDIR}/thrift-interface-descriptions/target"
    # Ensure the required target directories exists, if not create.
    mkdir -p ${BASE_TARGET_DIR}

    # Java stub target directories for components/services
    CS_SRC_DIR='../airavata-api/src/main/java'
    ORCHESTRATOR_SRC_DIR='../airavata-api/src/main/java'
    REGISTRY_SRC_DIR='../airavata-api/src/main/java/'
    SHARING_REGISTRY_SRC_DIR='../airavata-api/src/main/java/'
    PROFILE_SERVICE_SRC_DIR='../airavata-api/src/main/java'
}


# The Function fail prints error messages on failure and quits the script.
fail() {
    echo $@
    exit 1
}

# The function add_license_header adds the ASF V2 license header to all java files within the specified generated
#   directory. The function also adds suppress all warnings annotation to all public classes and enum's
#  To Call:
#   add_license_header $generated_code_directory
add_license_header() {

    # Fetch the generated code directory passed as the argument
    GENERATED_CODE_DIR=$1

    # For each source file within the generated directory, add the ASF V2 LICENSE header
    FILE_SUFFIXES=(.php .java .h .cpp)
    for file in "${FILE_SUFFIXES[@]}"; do
        for f in $(find ${GENERATED_CODE_DIR} -name "*$file"); do
            cat - ${f} >${f}-with-license <<EOF
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

EOF
        mv ${f}-with-license ${f}
        done
    done
}

# The function compares every generated java file with the one in specified existing source location. If the comparison
#   shows a difference, then it replaces with the newly generated file (with added license header).
#  To Call:
#   copy_changed_files $generated_code_directory $existing_source_directory
copy_changed_files() {

    # Read all the function arguments
    GENERATED_CODE_DIR=$1
    WORKSPACE_SRC_DIR=$2

    echo "Generated sources are in ${GENERATED_CODE_DIR}"
    echo "Destination workspace is in ${WORKSPACE_SRC_DIR}"

    # Check if the newly generated files exist in the targeted workspace, if not copy. Only changed files will be synced.
    #  the extra slash to GENERATED_CODE_DIR is needed to ensure the parent directory itself is not copied.
    rsync -auv ${GENERATED_CODE_DIR}/ ${WORKSPACE_SRC_DIR}
}

#######################################
# Generate/Update Airavata Data Model #
#######################################

generate_java_stubs() {

    #Java Beans generation directory
    JAVA_BEAN_GEN_DIR=${BASE_TARGET_DIR}/gen-javabean

    # As a precaution  remove and previously generated files if exists
    rm -rf ${JAVA_BEAN_GEN_DIR}

    # Generate the Airavata Data Model using thrift Java Beans generator. This will take generate the classes in bean style
    #   with members being private and setters returning voids.
    $THRIFT_EXEC ${THRIFT_ARGS} --gen java:beans,jakarta_annotations=undated ${JAVA_THRIFT_FILE} || fail unable to generate java bean thrift classes on base data model

    # For the generated java beans add the ASF V2 License header
    add_license_header $JAVA_BEAN_GEN_DIR

    # Compare the newly generated beans with existing sources and replace the changed ones.
    copy_changed_files ${JAVA_BEAN_GEN_DIR} ${JAVA_SRC_DIR}

    # Clear Bean generation directory as sharing data models are transferred to a different directory
    rm -rf ${JAVA_BEAN_GEN_DIR}

    echo "Successfully generated new java sources, compared against exiting code and replaced the changed files"

    # --- Generate Java stubs for all component/service CPIs ---
    generate_component_java_stubs ${CREDENTIAL_API_THRIFT_FILE} ${CS_SRC_DIR}
    generate_component_java_stubs ${ORCHESTRATOR_THRIFT_FILE} ${ORCHESTRATOR_SRC_DIR}
    generate_component_java_stubs ${REGISTRY_THRIFT_FILE} ${REGISTRY_SRC_DIR}
    generate_component_java_stubs ${SHARING_API_THRIFT_FILE} ${SHARING_REGISTRY_SRC_DIR}
    generate_component_java_stubs ${PROFILE_SERVICE_THRIFT_FILE} ${PROFILE_SERVICE_SRC_DIR}
    echo "Successfully generated all Java stubs (API, CPI, and service)"
}

####################################
# Generate/Update PHP Stubs #
####################################

generate_php_stubs() {

    #PHP generation directory
    PHP_GEN_DIR=${BASE_TARGET_DIR}/gen-php

    # As a precaution  remove and previously generated files if exists
    rm -rf ${PHP_GEN_DIR}

    # Using thrift Java generator, generate the PHP classes based on Airavata API. This
    #   The airavata_api.thrift includes rest of data models.
    $THRIFT_EXEC ${THRIFT_ARGS} --gen php ${PHP_THRIFT_FILE}  || fail unable to generate PHP thrift classes
    $THRIFT_EXEC ${THRIFT_ARGS} --gen php ${AIRAVATA_API_THRIFT_FILE} || fail unable to generate PHP thrift classes
    $THRIFT_EXEC ${THRIFT_ARGS} --gen php ${SHARING_API_THRIFT_FILE} || fail unable to generate PHP thrift classes
    $THRIFT_EXEC ${THRIFT_ARGS} --gen php ${PROFILE_SERVICE_THRIFT_FILE} || fail unable to generate PHP thrift classes

    # For the generated java classes add the ASF V2 License header
    ## TODO Write PHP license parser

    # Compare the newly generated classes with existing java generated skeleton/stub sources and replace the changed ones.
    #  Only copying the API related classes and avoiding copy of any data models which already exist in the data-models.
    copy_changed_files ${PHP_GEN_DIR} ${PHP_SDK_DIR}

}

####################################
# Generate/Update C++ Client Stubs #
####################################

generate_cpp_stubs() {

    #CPP generation directory
    CPP_GEN_DIR=${BASE_TARGET_DIR}/gen-cpp

    # As a precaution  remove and previously generated files if exists
    rm -rf ${CPP_GEN_DIR}

    # Using thrift Java generator, generate the java classes based on Airavata API. This
    #   The airavata_api.thrift includes rest of data models.
    $THRIFT_EXEC ${THRIFT_ARGS} --gen cpp ${AIRAVATA_API_THRIFT_FILE}  || fail unable to generate C++ thrift classes

    # For the generated CPP classes add the ASF V2 License header
    add_license_header $CPP_GEN_DIR

    # Compare the newly generated classes with existing java generated skeleton/stub sources and replace the changed ones.
    #  Only copying the API related classes and avoiding copy of any data models which already exist in the data-models.
    copy_changed_files ${CPP_GEN_DIR} ${CPP_SDK_DIR}

}

####################################
# Generate/Update C++ Client Stubs #
####################################

generate_python_stubs() {

    #Python generation directory
    PYTHON_GEN_DIR=${BASE_TARGET_DIR}/gen-py/airavata

    # As a precaution  remove and previously generated files if exists
    rm -rf ${PYTHON_GEN_DIR}

    # Using thrift Python generator, generate the python classes based on Airavata API. This
    #   The airavata_api.thrift includes rest of data models.
    $THRIFT_EXEC ${THRIFT_ARGS} --gen py:enum,type_hints ${AIRAVATA_API_THRIFT_FILE}  || fail unable to generate Python thrift classes

    $THRIFT_EXEC ${THRIFT_ARGS} --gen py:enum,type_hints ${CREDENTIAL_API_THRIFT_FILE}  || fail unable to generate Python thrift classes

    $THRIFT_EXEC ${THRIFT_ARGS} --gen py:enum,type_hints ${SHARING_API_THRIFT_FILE}  || fail unable to generate Python thrift classes

    $THRIFT_EXEC ${THRIFT_ARGS} --gen py:enum,type_hints ${PROFILE_SERVICE_THRIFT_FILE} || fail unable to generate Python thrift classes

    # For the generated CPP classes add the ASF V2 License header
    #add_license_header #PYTHON_GEN_DIR

    # Compare the newly generated classes with existing java generated skeleton/stub sources and replace the changed ones.
    #  Only copying the API related classes and avoiding copy of any data models which already exist in the data-models.
    copy_changed_files ${PYTHON_GEN_DIR} ${PYTHON_SDK_DIR}

}

# Helper to generate Java stubs for a component/service CPI
# Usage: generate_component_java_stubs <thrift_file> <target_dir>
generate_component_java_stubs() {
    COMPONENT_THRIFT_FILE=$1
    COMPONENT_SRC_DIR=$2
    #Java generation directory
    JAVA_GEN_DIR=${BASE_TARGET_DIR}/gen-java
    BASE_API_DIR=org/apache/airavata/base
    # Remove any previously generated files
    rm -rf ${JAVA_GEN_DIR}
    # Generate Java stubs
    $THRIFT_EXEC ${THRIFT_ARGS} --gen java:jakarta_annotations=undated ${COMPONENT_THRIFT_FILE} || fail unable to generate java thrift classes for ${COMPONENT_THRIFT_FILE}
    # Remove generated model classes, airavata api thrift file will generate those.
    rm -rf ${JAVA_GEN_DIR}/org/apache/airavata/model
    # Add license header
    add_license_header $JAVA_GEN_DIR
    # Copy changed files, excluding base API dir
    rsync -auv --exclude org/apache/airavata/base ${JAVA_GEN_DIR}/ ${COMPONENT_SRC_DIR}
    # Copy base API stubs
    mkdir -p ${JAVA_SRC_DIR}/org/apache/airavata/base
    copy_changed_files ${JAVA_GEN_DIR}/org/apache/airavata/base ${JAVA_SRC_DIR}/org/apache/airavata/base
}

for arg in "$@"
do
    case "$arg" in
    all)    echo "Generate all stubs (Java, PHP, C++, Python) Stubs"
            setup
            generate_java_stubs
            generate_php_stubs
            generate_cpp_stubs
            generate_python_stubs
            ;;
    java)   echo "Generating Java Stubs"
            setup
            generate_java_stubs
            ;;
    php)    echo "Generate PHP Stubs"
            setup
            generate_php_stubs
            ;;
    cpp)    echo "Generate C++ Stubs"
            setup
            generate_cpp_stubs
            ;;
    python)    echo "Generate Python Stubs"
            setup
            generate_python_stubs
            ;;
    *)      echo "Invalid or unsupported option"
    	    show_usage
	        exit 1
            ;;
    esac
done

####################
# Cleanup and Exit #
####################
# CleanUp: Delete the base target build directory
#rm -rf ${BASE_TARGET_DIR}

exit 0
