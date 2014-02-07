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

# This script will regenerate the thrift code for Airavata Server Skeltons, Client Stubs and Data Model java beans.
#
# Credit: This script was created referring to Apache Accumulo project and tuned to Airavata Needs.

# ========================================================================================================================
REQUIRED_THRIFT_VERSION='0.9'
BASE_OUTPUT_PACKAGE='org.apache.airavata'
THRIFT_IDL_DIR='thrift-interface-descriptions'
PACKAGES_TO_GENERATE=(gc master tabletserver security client.impl data)
BUILD_DIR='target'
FINAL_DIR='datamodel/src/main/java'
# ========================================================================================================================

fail() {
  echo $@
  exit 1
}

# Test to see if we have thrift installed
VERSION=$(thrift -version 2>/dev/null | grep -F "${REQUIRED_THRIFT_VERSION}" |  wc -l)
if [ "$VERSION" -ne 1 ] ; then
  # Nope: bail
  echo "****************************************************"
  echo "*** thrift is not available"
  echo "***   expecting 'thrift -version' to return ${REQUIRED_THRIFT_VERSION}"
  echo "*** generated code will not be updated"
  fail "****************************************************"
fi

# Initialize the thrift arguements.
#  Since most of the Airavata API and Data Models have includes, use recursive option by defualt.
#  Generate all the files in target directory
THRIFT_ARGS="-r -o $BUILD_DIR"

# Ensure output directories are created
mkdir -p $BUILD_DIR


# Generate the Airavata Data Model. Use Java Beans to generate to take advantage of the bean style
#   with members being private and setters returning voids.

# First remove and previously generated files
rm -rf $BUILD_DIR/gen-javabean

# the airavataDataModel.thrift includes rest of data models.
thrift ${THRIFT_ARGS} --gen java:beans $THRIFT_IDL_DIR/airavataDataModel.thrift || fail unable to generate java bean thrift classes


# For all generated thrift code, suppress all warnings and add the LICENSE header
find $BUILD_DIR/gen-javabean -name '*.java' -print0 | xargs -0 sed -i.orig -e 's/public class /@SuppressWarnings("all") public class /'
#find $BUILD_DIR/gen-javabean -name '*.java' -print0 | xargs -0 sed -i.orig -e 's/public enum /@SuppressWarnings("all") public enum /'
for f in $(find $BUILD_DIR/gen-javabean -name '*.java'); do
  cat - $f >${f}-with-license <<EOF
/*
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
done

# For every generated java file, compare it with the version-controlled one, and copy the ones that have changed into place
  SDIR="${BUILD_DIR}/gen-javabean/org/apache/airavata/model/experiment"
  DDIR="${FINAL_DIR}/org/apache/airavata/model/experiment"
  mkdir -p "$DDIR"
  for f in "$SDIR"/*.java; do
    DEST="$DDIR/`basename $f`"
    if ! cmp -s "${f}-with-license" "${DEST}" ; then
      echo cp -f "${f}-with-license" "${DEST}"
      cp -f "${f}-with-license" "${DEST}" || fail unable to copy files to java workspace
    fi
  done