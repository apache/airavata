
# Get the directory that this script is in
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo -e "Running production JS bulids"
cd $SCRIPT_DIR/django_airavata/apps/api && npm run build || exit 1
cd $SCRIPT_DIR/django_airavata && npm run build || exit 1
cd $SCRIPT_DIR/django_airavata/apps/admin && npm run build || exit 1
cd $SCRIPT_DIR/django_airavata/apps/workspace && npm run build || exit 1

echo -e "All builds finished successfully!"

exit 0
