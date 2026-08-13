-- SCPData.IsFile was carried over from the Java model as a required non-blank
-- string ("true"/"false"). Tightened to a real bool.
--
-- A direct MODIFY COLUMN to boolean fails outright under strict SQL mode: MariaDB
-- will not coerce the text 'true' to an integer ("Truncated incorrect INTEGER value:
-- 'true'"), confirmed against real data before writing this migration. The existing
-- values are normalised to '1'/'0' first; anything not recognised as true falls back
-- to false, matching the Go bool zero value the application already treats as the
-- default.

UPDATE `scp_data` SET `is_file` = '1' WHERE LOWER(TRIM(`is_file`)) IN ('true', '1', 'yes');
UPDATE `scp_data` SET `is_file` = '0' WHERE `is_file` <> '1';

ALTER TABLE `scp_data` MODIFY COLUMN `is_file` boolean NOT NULL;
