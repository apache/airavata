
echo Running production JS builds

set cwd=%cd%

cd %cwd%\django_airavata\apps\api
call yarn
call yarn run build

cd %cwd%\django_airavata\static\common
call yarn
call yarn run build

cd %cwd%\django_airavata\apps\admin
call yarn
call yarn run build

cd %cwd%\django_airavata\apps\groups
call yarn
call yarn run build

cd %cwd%\django_airavata\apps\workspace\django-airavata-workspace-plugin-api
call yarn
call yarn run build

cd %cwd%\django_airavata\apps\workspace
call yarn
call yarn run build

cd %cwd%\django_airavata\apps\dataparsers
call yarn
call yarn run build

cd %cwd%

echo All builds finished successfully!