
# Get the directory that this script is in
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo -e "Linting JS"
(cd $SCRIPT_DIR/django_airavata/apps/api && npm install && npm run lint) || exit 1
(cd $SCRIPT_DIR/django_airavata/static/common && npm install && npm run lint) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/admin && npm install && npm run lint) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/groups && npm install && npm run lint) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/workspace/django-airavata-workspace-plugin-api && npm install && npm run lint) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/workspace && npm install && npm run lint) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/dataparsers && npm install && npm run lint) || exit 1

echo -e "All linting finished successfully!"

exit 0
