# node image is based on Debian and includes necessary build tools
FROM node:lts as build-stage

# build api javascript
# api must come first, then common, since the others depend on these
WORKDIR /code/django_airavata/apps/api
COPY ./django_airavata/apps/api/package.json ./django_airavata/apps/api/yarn.lock ./
RUN yarn
COPY ./django_airavata/apps/api/ .
RUN yarn run build

# build common javascript
WORKDIR /code/django_airavata/static/common
COPY ./django_airavata/static/common/package.json ./django_airavata/static/common/yarn.lock ./
RUN yarn
COPY ./django_airavata/static/common/ .
RUN yarn run build

# build dataparsers javascript
WORKDIR /code/django_airavata/apps/dataparsers
COPY ./django_airavata/apps/dataparsers/package.json ./django_airavata/apps/dataparsers/yarn.lock ./
RUN yarn
COPY ./django_airavata/apps/dataparsers/ .
RUN yarn run build

# build groups javascript
WORKDIR /code/django_airavata/apps/groups
COPY ./django_airavata/apps/groups/package.json ./django_airavata/apps/groups/yarn.lock ./
RUN yarn
COPY ./django_airavata/apps/groups/ .
RUN yarn run build

# build auth javascript
WORKDIR /code/django_airavata/apps/auth
COPY ./django_airavata/apps/auth/package.json ./django_airavata/apps/auth/yarn.lock ./
RUN yarn
COPY ./django_airavata/apps/auth/ .
RUN yarn run build

# build workspace/django-airavata-workspace-plugin-api javascript
# This one must come before workspace build
WORKDIR /code/django_airavata/apps/workspace/django-airavata-workspace-plugin-api
COPY ./django_airavata/apps/workspace/django-airavata-workspace-plugin-api/package.json ./django_airavata/apps/workspace/django-airavata-workspace-plugin-api/yarn.lock ./
RUN yarn
COPY ./django_airavata/apps/workspace/django-airavata-workspace-plugin-api/ .
RUN yarn run build

# build admin javascript
# To reuse cache best, putting the two most volatile apps, admin and workspace, last
WORKDIR /code/django_airavata/apps/admin
COPY ./django_airavata/apps/admin/package.json ./django_airavata/apps/admin/yarn.lock ./
RUN yarn
COPY ./django_airavata/apps/admin/ .
RUN yarn run build

# build workspace javascript
WORKDIR /code/django_airavata/apps/workspace
COPY ./django_airavata/apps/workspace/package.json ./django_airavata/apps/workspace/yarn.lock ./
RUN yarn
COPY ./django_airavata/apps/workspace/ .
RUN yarn run build



FROM python:3.9-slim as server-stage

ENV PYTHONUNBUFFERED 1

EXPOSE 8000

WORKDIR /code
COPY requirements.txt requirements-mysql.txt ./
COPY setup.* ./
COPY README.md .
RUN apt-get update && apt-get install -y git gcc g++ zlib1g-dev libjpeg-dev default-libmysqlclient-dev
RUN pip install --upgrade pip setuptools wheel --no-cache
RUN pip install -r requirements.txt --no-cache
RUN pip install -r requirements-mysql.txt --no-cache

# Copy in a default settings_local.py file
COPY ./django_airavata/settings_local.py.sample ./django_airavata/settings_local.py

COPY ./ .

# Copy javascript builds from build-stage
WORKDIR /code/django_airavata/apps/api/static/django_airavata_api
COPY --from=build-stage /code/django_airavata/apps/api/static/django_airavata_api .
WORKDIR /code/django_airavata/static/common/dist
COPY --from=build-stage /code/django_airavata/static/common/dist .
WORKDIR /code/django_airavata/apps/admin/static/django_airavata_admin
COPY --from=build-stage /code/django_airavata/apps/admin/static/django_airavata_admin .
WORKDIR /code/django_airavata/apps/groups/static/django_airavata_groups
COPY --from=build-stage /code/django_airavata/apps/groups/static/django_airavata_groups .
WORKDIR /code/django_airavata/apps/auth/static/django_airavata_auth
COPY --from=build-stage /code/django_airavata/apps/auth/static/django_airavata_auth .
WORKDIR /code/django_airavata/apps/workspace/static/django_airavata_workspace
COPY --from=build-stage /code/django_airavata/apps/workspace/static/django_airavata_workspace .
WORKDIR /code/django_airavata/apps/dataparsers/static/django_airavata_dataparsers
COPY --from=build-stage /code/django_airavata/apps/dataparsers/static/django_airavata_dataparsers .

WORKDIR /code

ENTRYPOINT ["/code/scripts/start-server.sh"]

