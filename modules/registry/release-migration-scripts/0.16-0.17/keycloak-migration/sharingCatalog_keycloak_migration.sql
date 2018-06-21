-- sharing catalog
--- disable foreign key checks in MySQL/MariaDB
SET FOREIGN_KEY_CHECKS=0;
update SHARING_USER set USER_ID = lower(USER_ID), USER_NAME = lower(USER_NAME);
update USER_GROUP set OWNER_ID = lower(OWNER_ID), GROUP_ID = lower(GROUP_ID);
update ENTITY set OWNER_ID = lower(OWNER_ID);
update SHARING set GROUP_ID = lower(GROUP_ID);
SET FOREIGN_KEY_CHECKS=1;