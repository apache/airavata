
# Get the directory that this script is in
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo -e "Testing JS"
(cd $SCRIPT_DIR/django_airavata/apps/api && yarn && yarn run test) || exit 1
(cd $SCRIPT_DIR/django_airavata/apps/workspace && yarn && yarn run test) || exit 1

echo -e "All testing finished successfully!"

exit 0
