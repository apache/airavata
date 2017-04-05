## 0.16 - 0.17 Migration Notes
 
 ### Known Issues:
  
  * Experiment catalog - _NOTIFICATION_ and _USER_ table's column ordering differs, apart from this difference all other constraints are met 
 
 ### Migration Steps:
 
  * Execute the SQL scripts present in DeltaScripts folder on top of Airavata 0.16 Release Database
  * dev_sharingcatalog and dev_grouper database schema's are not present in Airavata 0.16 Release, hence whole database has to be migrated, therefore not delta scripts are generated.