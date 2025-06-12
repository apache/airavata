create database app_catalog;
create database experiment_catalog;
create database replica_catalog;
create database credential_store;
create database workflow_catalog;
grant all privileges on app_catalog.* to 'airavata'@'%' identified by 'airavata';
grant all privileges on experiment_catalog.* to 'airavata'@'%' identified by 'airavata';
grant all privileges on replica_catalog.* to 'airavata'@'%' identified by 'airavata';
grant all privileges on credential_store.* to 'airavata'@'%' identified by 'airavata';
grant all privileges on workflow_catalog.* to 'airavata'@'%' identified by 'airavata';

