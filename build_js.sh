#!/bin/bash

# Get the directory that this script is in
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo -e "Running production JS builds"
(cd $SCRIPT_DIR/django_airavata/apps/api && yarn && yarn run build) || exit 1
(cd $SCRIPT_DIR/django_airavata/static/common && yarn && yarn run build) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/auth && yarn && yarn run build) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/admin && yarn && yarn run build) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/groups && yarn && yarn run build) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/workspace/django-airavata-workspace-plugin-api && yarn && yarn run build) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/workspace && yarn && yarn run build) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/dataparsers && yarn && yarn run build) || exit 1

echo -e "All builds finished successfully!"

exit 0
