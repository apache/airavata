## 0.16 - 0.17 Migration Notes
 
 ### Known Issues:
  
  * Experiment catalog - _NOTIFICATION_ and _USER_ table's column ordering differs, apart from this difference all other constraints are met 
 
 ### Migration Steps:
 
  * Execute the SQL scripts present in DeltaScripts folder on top of Airavata 0.16 Release Database
  * dev_sharingcatalog and dev_grouper database schema's are not present in Airavata 0.16 Release, hence whole database has to be migrated, therefore not delta scripts are generated.
  * There is no migration script for the new profile_service database
  * Run the WSO2 IS -> Keycloak+Profile Service migration script. See MigrationManager.java in the user-profile-migration module.
  * Keycloak only allows lowercase usernames. So all usernames in the database must be lowercased. 
  Run the scripts in the keycloak-migration folder to lowercase usernames.
  * For each gateway in gateway data storage, run the replicaCatalog_lowercase_user_data_dir_example.sql
  script in the keycloak-migration-replica-catalog directory. **NOTE**: you'll need to tweak the variables
  at the start of the script for a particular deployment.
