-- Change CREDENTIAL column from BLOB to LONGBLOB to support larger credentials
-- BLOB has a maximum size of 65,535 bytes, which is insufficient for serialized
-- credentials, especially when encryption is enabled.
ALTER TABLE CREDENTIALS MODIFY COLUMN CREDENTIAL LONGBLOB NOT NULL;

