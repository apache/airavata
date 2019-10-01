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

# This script will generate/regenerate the thrift stubs for Airavata Services: Profile Service.

show_usage() {
	echo -e "Usage: $0 [--native-thrift] [Component to generate stubs]"
	echo ""
	echo "options:"
	echo -e "\tps Generate/Update Profile Service Stubs"
	echo -e "\tall Generate/Update all stubs (Profile Service)."
	echo -e "\t-h[elp] Print the usage options of this script"
	echo -e "\t--native-thrift Use natively installed thrift instead of Docker image"
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

REQUIRED_THRIFT_VERSION='0.10.0'
THRIFT_DOCKER_IMAGE='thrift'
THRIFT_NATIVE="false"
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PARENT_DIR=`dirname "$SCRIPT_DIR"`

setup() {
    if [[ $THRIFT_NATIVE == "true" ]]; then
        if hash thrift &> /dev/null; then
          THRIFT_EXEC=$(which thrift)
        else
          THRIFT_EXEC=/usr/local/bin/thrift
        fi
        BASEDIR="$PARENT_DIR"
    else
        THRIFT_EXEC="docker run --rm -v $PARENT_DIR:/data $THRIFT_DOCKER_IMAGE:$REQUIRED_THRIFT_VERSION thrift"
        BASEDIR="/data"
    fi

    VERSION=$($THRIFT_EXEC -version 2>/dev/null | grep -F "${REQUIRED_THRIFT_VERSION}" |  wc -l)
    if [ "$VERSION" -ne 1 ] ; then
        echo -e "ERROR:\t Apache Thrift version ${REQUIRED_THRIFT_VERSION} is required."
        echo -e "It is either not installed or is not in the path"
        exit 1
    fi

    # Global Constants used across the script
    BASE_TARGET_DIR="$SCRIPT_DIR/target"

    PROFILE_SERVICE_THRIFT_FILE="${BASEDIR}/service-cpis/profile-service/profile-service-cpi.thrift"
    PROFILE_SERVICE_SRC_DIR='../../airavata-services/profile-service/profile-service-stubs/src/main/java'

    BASE_API_SRC_DIR='../../airavata-api/airavata-base-api/src/main/java'

    # Initialize the thrift arguments.
    #  Since most of the Airavata API and Data Models have includes, use recursive option by default.
    #  Generate all the files in target directory
    THRIFT_ARGS="-r -o ${BASEDIR}/service-cpis/target"
    # Ensure the required target directories exists, if not create.
    mkdir -p ${BASE_TARGET_DIR}
}

# The Funcation fail prints error messages on failure and quits the script.
fail() {
    echo $@
    exit 1
}

# The funcation add_license_header adds the ASF V2 license header to all java files within the specified generated
#   directory. The funcation also adds suppress all warnings annotation to all public classes and enum's
#  To Call:
#   add_license_header $generated_code_directory
add_license_header() {

    # Fetch the generated code directory passed as the argument
    GENERATED_CODE_DIR=$1

    # For each java file within the generated directory, add the ASF V2 LICENSE header
    for f in $(find ${GENERATED_CODE_DIR} -name '*.java'); do
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
}

# The function compares every generated java file with the one in specified existing source location. If the comparison
#   shows a difference, then it replaces with the newly generated file (with added license header).
#  To Call:
#   copy_changed_files $generated_code_directory $existing_source_directory
copy_changed_files() {

    # Read all the function arguments
    GENERATED_CODE_DIR=$1
    WORKSPACE_SRC_DIR=$2
    IGNORE_GENERATED_CODE_PATH=$3

    echo "Generated sources are in ${GENERATED_CODE_DIR}"
    echo "Destination workspace is in ${WORKSPACE_SRC_DIR}"

    # Check if the newly generated files exist in the targetted workspace, if not copy. Only changed files will be synced.
    #  the extra slash to GENERATED_CODE_DIR is needed to ensure the parent directory itself is not copied.
    if [ -z "$3" ]
        then
            rsync -auv ${GENERATED_CODE_DIR}/ ${WORKSPACE_SRC_DIR}
        else
            echo "Ignoring generated path ${IGNORE_GENERATED_CODE_PATH}"
            rsync -auv --exclude ${IGNORE_GENERATED_CODE_PATH} ${GENERATED_CODE_DIR}/ ${WORKSPACE_SRC_DIR}
    fi
}

# The function generates the thrift stubs and copies to the specified directory.
#  To Call:
#   generate_thrift_stubs $component_thrift_file $component_src_dir
generate_thrift_stubs() {

    COMPONENT_THRIFT_FILE=$1
    COMPONENT_SRC_DIR=$2

    #Java generation directory
    JAVA_GEN_DIR=${BASE_TARGET_DIR}/gen-java

    BASE_API_DIR=org/apache/airavata/base

    # As a precaution remove and previously generated files if exists
    rm -rf ${JAVA_GEN_DIR}

    # Using thrify Java generator, generate the java classes based on components CPI.
    $THRIFT_EXEC ${THRIFT_ARGS} --gen java:generated_annotations=undated ${COMPONENT_THRIFT_FILE} || fail unable to generate java thrift classes

    # Remove generated model classes, airavata api thrift file will generate those.
    echo "Remove generated model classes ${JAVA_GEN_DIR}/org/airavata/apache/model"
    rm -rf ${JAVA_GEN_DIR}/org/apache/airavata/model

    # For the generated java classes add the ASF V2 License header
    add_license_header $JAVA_GEN_DIR

    # Compare the newly generated classes with existing java generated skelton/stub sources and replace the changed ones.
    copy_changed_files ${JAVA_GEN_DIR} ${COMPONENT_SRC_DIR} ${BASE_API_DIR}

    # This will copy the base API java stubs to airavata-base-api module
    mkdir -p ${BASE_API_SRC_DIR}/org/apache/airavata/base
    copy_changed_files ${JAVA_GEN_DIR}/org/apache/airavata/base ${BASE_API_SRC_DIR}/org/apache/airavata/base

    echo "Successfully generated new sources, compared against exiting code and replaced the changed files"

}

for arg in "$@"
do
    case "$arg" in
    all)    echo "Generate all (profile service) Stubs"
            setup
            generate_thrift_stubs ${PROFILE_SERVICE_THRIFT_FILE} ${PROFILE_SERVICE_SRC_DIR}
            ;;
    ps)   echo "Generating Profile Service Stubs"
            setup
            generate_thrift_stubs ${PROFILE_SERVICE_THRIFT_FILE} ${PROFILE_SERVICE_SRC_DIR}
            ;;
    --native-thrift)
            THRIFT_NATIVE="true"
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
