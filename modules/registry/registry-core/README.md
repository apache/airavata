# Schema migration scripts

When you add or update Entity classes, you need to update the SQL schema
scripts, both the scripts that define the entire schema for brand new databases
(referred to from here on as the _database script_) and the script to migrate an
older schema to the new schema (referred to from here on as the _migration
script_). These instructions show you how to create these scripts.

Before using these steps, be sure to review the **Known Issues** section later
on. Notably, any removals from the schema will have to be manually managed.

1. First, start by adding or updating the Entity class, for example, by adding a
   new field.

   Additional notes:

   - To have OpenJPA generate FOREIGN KEY schema statements, you need to
     annotate the foreign key reference with
     `@org.apache.openjpa.persistence.jdbc.ForeignKey`.
   - To have OpenJPA generate INDEX schema statements, you need to annotate the
     indexed columns with `@org.apache.openjpa.persistence.jdbc.Index`.

2. If you added an Entity class, make sure to add an entry for it in
   `persistence.xml`. You'll need to also add the same entry to
   `../../ide-integration/src/main/resources/META-INF/persistence.xml`.
3. Next, you need to update the Derby database script. To do this, run
   ```
   mvn clean process-classes exec:exec@generate-schema-derby
   ```
   This will generate a database script for each database in `target/`. For
   example, for appcatalog, it will generate a file called
   `target/app_catalog-schema.sql`. Copy the contents of the database script
   that are relevant to the Entity class changes that you made into the
   corresponding database script in `src/main/resources/`. For example, if you
   changed an app catalog Entity then you would copy the `CREATE TABLE`, etc.
   statements related to that Entity from the `target/app_catalog-schema.sql`
   script to `src/main/resources/appcatalog-derby.sql`, replace any existing
   `CREATE TABLE`, etc. statements for the table. Note that the generate
   database script may have several statements related to your Entity class
   changes throughout it, for example, the `CREATE TABLE` statements tend to
   come first and the `FOREIGN KEY` statements come later.
4. Next, you'll update the MariaDB (or MySQL) database script. To do this, run

   ```
   mvn clean process-classes docker-compose:up@mysql-up \
      exec:exec@generate-schema-mysql exec:exec@generate-migrations-mysql \
      docker-compose:down@mysql-down
   ```

   This will generate a database and a migration script for each database in
   `target/`. For example, for appcatalog, it will generate a file called
   `target/app_catalog-schema.sql` (the database script) and a file called
   `target/app_catalog-migration.sql` (the migration script). Like the previous
   step, you'll copy the contents of the database script (the one ending in
   `-schema.sql`) that are relevant to the Entity class changes you made and add
   them to the corresponding database script in `src/main/resources/`.

   Some additional notes:

   - Make sure to add `DEFAULT CHARSET=latin1` to all MySQL `CREATE TABLE`
     statements.
   - It is recommended that you name all constraints and indexes. This makes it
     easier to change or drop them in the future. The naming convention is to
     use a prefix of `FK_` or `UNIQ_` or `IDX_` that indicates the type of
     constraint, then the table name, then the names of the columns or related
     entity or some short description of the constraint. For example, if you
     create a FOREIGN KEY on table CHILD of column PARENT_ID you would name it
     `FK_CHILD_PARENT_ID`.

5. Next, you'll create a MariaDB migration script. The migration script should
   have been created in the previous step, but if necessary you can run

   ```
   mvn clean process-classes docker-compose:up@mysql-up exec:exec@generate-migrations-mysql docker-compose:down@mysql-down
   ```

   Copy the contents of the migration script that are relevant to the Entity
   classes changes that you made into the respective migration schema scripts in
   `../release-migration-scripts/next/DeltaScripts/`. All statements should have
   `IF NOT EXISTS` (if adding) or `IF EXISTS` (if dropping) added so that they
   can be reapplied and only change the schema when it hasn't already been
   updated. For example:

   - `CREATE TABLE IF NOT EXISTS ...`
   - `DROP TABLE IF EXISTS ...`
   - `ALTER TABLE <table name> ADD COLUMN IF NOT EXISTS <column definition>`
   - `ALTER TABLE <table name> DROP COLUMN IF EXISTS <column name>`
   - `ALTER TABLE <table name> ADD CONSTRAINT <foreign key name> FOREIGN KEY IF NOT EXISTS <foreign key definition>`
   - `ALTER TABLE <table name> DROP FOREIGN KEY IF EXISTS <foreign key name>`
   - `ALTER TABLE <table name> ADD KEY IF NOT EXISTS <key definition>`
   - `ALTER TABLE <table name> DROP KEY IF EXISTS <index name>`

   See also the **additional notes** under the previous step.

6. Next, you'll also copy this MariaDB migration script to the corresponding
   `0*-migrations.sql` file in ide-integration. Here it should also have
   `IF NOT EXISTS`/`IF EXISTS` added. Essentially you just need to take the
   lines you added in `release-migration-scripts` and add them to the
   corresponding `0*-migrations.sql` script.

## Known Issues

- can automatically create schema migrations that add columns/tables, but not
  ones that remove them. **Creating schema migrations that drop columns/tables
  will have to be done manually.**
- **AIRAVATA-3386: Migration script generation for MariaDB includes also the
  full database schema. The generated migration script will have the ALTER
  commands first but then be followed by the CREATE statements for all of the
  tables, etc. for the database.**
- schema generation generates a PRIMARY KEY for VIEWs that are mapped to an
  Entity, for example ExperimentSummary.
- when unique constraints are added to an entity, the generated migration
  scripts do not include an ALTER TABLE statement to add it. However, the
  database script does include the unique constraint so it can be copied from
  that into an `ALTER TABLE ADD UNIQUE ...` statement in the migation script.
- when the definition of a column is chaged, the generated migrations do not
  include an ALTER STATEMENT to update it. However, the database script does
  reflect the updated column definition so it can be copied from that into an
  `ALTER TABLE MODIFY COLUMN col_name ...`.
