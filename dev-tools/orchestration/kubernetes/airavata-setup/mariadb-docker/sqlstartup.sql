CREATE DATABASE IF NOT EXISTS experiment_catalog;
CREATE DATABASE IF NOT EXISTS app_catalog;
CREATE DATABASE IF NOT EXISTS replica_catalog;
CREATE DATABASE IF NOT EXISTS workflow_catalog;
CREATE DATABASE IF NOT EXISTS sharing_catalog;

grant all on experiment_catalog.* to 'airavata';
grant all on app_catalog.* to 'airavata';
grant all on replica_catalog.* to 'airavata';
grant all on workflow_catalog.* to 'airavata';
grant all on sharing_catalog.* to 'airavata';
