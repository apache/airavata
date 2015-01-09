#! /usr/bin/env bash

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# This script will regenerate the thrift code for Airavata Server Skeletons, Client Stubs and Data Model java beans.
#
# Credit: This script was created referring to Apache Accumulo project and tuned to Airavata Needs.

# Global Constants used across the script
REQUIRED_THRIFT_VERSION='0.9.1'
THRIFT_IDL_DIR='thrift-interface-descriptions'
BASE_TARGET_DIR='target'
DATAMODEL_SRC_DIR='airavata-data-models/src/main/java'
JAVA_API_SDK_DIR='airavata-api-stubs/src/main/java'
CPP_SDK_DIR='airavata-client-sdks/airavata-cpp-sdk/src/main/resources/lib/airavata/'
PHP_SDK_DIR='airavata-client-sdks/airavata-php-sdk/src/main/resources/lib'

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

    # For all generated thrift code, add the suppress all warnings annotation
    #  NOTE: In order to save the original file as a backup, use sed -i.orig in place of sed -i ''
    find ${GENERATED_CODE_DIR} -name '*.java' -print0 | xargs -0 sed -i '' -e 's/public class /@SuppressWarnings("all") public class /'
    find ${GENERATED_CODE_DIR} -name '*.java' -print0 | xargs -0 sed -i '' -e 's/public enum /@SuppressWarnings("all") public enum /'

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

# Generation of thrift files will require installing Apache Thrift. Please add thrift to your path.
#  Verify is thrift is installed, is in the path is at a specified version.
VERSION=$(thrift -version 2>/dev/null | grep -F "${REQUIRED_THRIFT_VERSION}" |  wc -l)
if [ "$VERSION" -ne 1 ] ; then
    echo "****************************************************"
    echo "*** thrift is not installed or is not in the path"
    echo "***   expecting 'thrift -version' to return ${REQUIRED_THRIFT_VERSION}"
    echo "*** generated code will not be updated"
    fail "****************************************************"
fi

# Initialize the thrift arguments.
#  Since most of the Airavata API and Data Models have includes, use recursive option by default.
#  Generate all the files in target directory
THRIFT_ARGS="-r -o ${BASE_TARGET_DIR}"
# Ensure the required target directories exists, if not create.
mkdir -p ${BASE_TARGET_DIR}

#######################################
# Generate/Update Airavata Data Model #
#######################################

#Java Beans generation directory
JAVA_BEAN_GEN_DIR=${BASE_TARGET_DIR}/gen-javabean

# As a precaution  remove and previously generated files if exists
rm -rf ${JAVA_BEAN_GEN_DIR}

# Generate the Airavata Data Model using thrift Java Beans generator. This will take generate the classes in bean style
#   with members being private and setters returning voids.
#   The airavataDataModel.thrift includes rest of data models.
thrift ${THRIFT_ARGS} --gen java:beans ${THRIFT_IDL_DIR}/airavataDataModel.thrift || fail unable to generate java bean thrift classes on base data model

thrift ${THRIFT_ARGS} --gen java:beans ${THRIFT_IDL_DIR}/appCatalogModels.thrift || fail unable to generate java bean thrift classes on app catalog data models

thrift ${THRIFT_ARGS} --gen java:beans ${THRIFT_IDL_DIR}/workflowDataModel.thrift || fail unable to generate java bean thrift classes on app workflow data models

# For the generated java beans add the ASF V2 License header
add_license_header $JAVA_BEAN_GEN_DIR

# Compare the newly generated beans with existing sources and replace the changed ones.
copy_changed_files ${JAVA_BEAN_GEN_DIR} ${DATAMODEL_SRC_DIR}

###############################################################################
# Generate/Update source used by Airavata Server Skeletons & Java Client Stubs #
#  JAVA server and client both use generated api-boilerplate-code             #
###############################################################################

#Java generation directory
JAVA_GEN_DIR=${BASE_TARGET_DIR}/gen-java

# As a precaution  remove and previously generated files if exists
rm -rf ${JAVA_GEN_DIR}

# Using thrift Java generator, generate the java classes based on Airavata API. This
#   The airavataAPI.thrift includes rest of data models.
thrift ${THRIFT_ARGS} --gen java ${THRIFT_IDL_DIR}/airavataAPI.thrift || fail unable to generate java thrift classes on AiravataAPI

#thrift ${THRIFT_ARGS} --gen java ${THRIFT_IDL_DIR}/workflowAPI.thrift || fail unable to generate java thrift classes on WorkflowAPI

# For the generated java classes add the ASF V2 License header
add_license_header $JAVA_GEN_DIR

# Compare the newly generated classes with existing java generated skeleton/stub sources and replace the changed ones.
#  Only copying the API related classes and avoiding copy of any data models which already exist in the data-models.
copy_changed_files ${JAVA_GEN_DIR}/org/apache/airavata/api ${JAVA_API_SDK_DIR}/org/apache/airavata/api

####################################
# Generate/Update C++ Client Stubs #
####################################

#CPP generation directory
CPP_GEN_DIR=${BASE_TARGET_DIR}/gen-cpp

# As a precaution  remove and previously generated files if exists
rm -rf ${CPP_GEN_DIR}

# Using thrift Java generator, generate the java classes based on Airavata API. This
#   The airavataAPI.thrift includes rest of data models.
thrift ${THRIFT_ARGS} --gen cpp ${THRIFT_IDL_DIR}/airavataAPI.thrift || fail unable to generate C++ thrift classes

#thrift ${THRIFT_ARGS} --gen cpp ${THRIFT_IDL_DIR}/workflowAPI.thrift || fail unable to generate C++ thrift classes for WorkflowAPI
# For the generated CPP classes add the ASF V2 License header
add_license_header $CPP_GEN_DIR

# Compare the newly generated classes with existing java generated skeleton/stub sources and replace the changed ones.
#  Only copying the API related classes and avoiding copy of any data models which already exist in the data-models.
copy_changed_files ${CPP_GEN_DIR} ${CPP_SDK_DIR}

####################################
# Generate/Update PHP Stubs #
####################################

#PHP generation directory
PHP_GEN_DIR=${BASE_TARGET_DIR}/gen-php

# As a precaution  remove and previously generated files if exists
rm -rf ${PHP_GEN_DIR}

# Using thrift Java generator, generate the java classes based on Airavata API. This
#   The airavataAPI.thrift includes rest of data models.
thrift ${THRIFT_ARGS} --gen php:autoload ${THRIFT_IDL_DIR}/airavataAPI.thrift || fail unable to generate PHP thrift classes

#thrift ${THRIFT_ARGS} --gen php:autoload ${THRIFT_IDL_DIR}/workflowAPI.thrift || fail unable to generate PHP thrift classes for WorkflowAPI
# For the generated java classes add the ASF V2 License header
## TODO Write PHP license parser

# Compare the newly generated classes with existing java generated skeleton/stub sources and replace the changed ones.
#  Only copying the API related classes and avoiding copy of any data models which already exist in the data-models.
copy_changed_files ${PHP_GEN_DIR} ${PHP_SDK_DIR}

####################
# Cleanup and Exit #
####################
# CleanUp: Delete the base target build directory
#rm -rf ${BASE_TARGET_DIR}

echo "Successfully generated new sources, compared against exiting code and replaced the changed files"
exit 0
