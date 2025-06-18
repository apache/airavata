
# Get the directory that this script is in
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo -e "Linting JS"
(cd $SCRIPT_DIR/django_airavata/apps/api && yarn && yarn run lint) || exit 1
(cd $SCRIPT_DIR/django_airavata/static/common && yarn && yarn run lint) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/auth && yarn && yarn run lint) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/admin && yarn && yarn run lint) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/groups && yarn && yarn run lint) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/workspace/django-airavata-workspace-plugin-api && yarn && yarn run lint) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/workspace && yarn && yarn run lint) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/dataparsers && yarn && yarn run lint) || exit 1

echo -e "All linting finished successfully!"

exit 0
