## 0.17 - 0.18 Migration Notes
 
### Known Issues:
  
* None
 
### Migration Steps:
 
* Execute the SQL scripts present in DeltaScripts folder on top of Airavata 0.17 Release Database
* There is no migration script for the new profile_service database
* Run the WSO2 IS -> Keycloak+Profile Service migration script. See MigrationManager.java in the user-profile-migration module.
* Keycloak only allows lowercase usernames. So all usernames in the database must be lowercased. 
  Run the scripts in the keycloak-migration folder to lowercase usernames.
* For each gateway in gateway data storage, run the replicaCatalog_lowercase_user_data_dir_example.sql
  script in the keycloak-migration-replica-catalog directory. **NOTE**: you'll need to tweak the variables
  at the start of the script for a particular deployment.

