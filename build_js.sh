
# Get the directory that this script is in
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo -e "Running production JS builds"
(cd $SCRIPT_DIR/django_airavata/apps/api && npm install && npm run build) || exit 1
(cd $SCRIPT_DIR/django_airavata/static/common && npm install && npm run build) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/admin && npm install && npm run build) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/groups && npm install && npm run build) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/workspace/django-airavata-workspace-plugin-api && npm install && npm run build) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/workspace && npm install && npm run build) || exit 1

echo -e "All builds finished successfully!"

exit 0
