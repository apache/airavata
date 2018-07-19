
# Running the Airavata Data Migrator

1. Copy the file `src/main/resources/airavata-server.properties.sample` to `src/main/resources/airavata-server.properties`.
2. Edit the `airavata-server.properties` file and make sure all properties are specified correctly.
3. Make sure that you have run `mvn install` in the root directory of the airavata project.
4. Run `mvn compile exec:java` in this directory. If you want to run the
   migration for just a single gateway, you can instead run
   `mvn compile exec:java -Dexec.args=gatewayId`.
