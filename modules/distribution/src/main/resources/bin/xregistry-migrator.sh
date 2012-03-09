#!/bin/sh
# WSO2 ESB Migration Tool

echo "\n      ** Build Script for XRegistry Migration Tool **"
echo "      ==========================================\n"

# Absolute path of this script.
SCRIPT=$(readlink -f $0)
# Full path of the directory where current script is in.
TOOL_CLASSPATH=`dirname $SCRIPT`

#---------- Add jars to classpath ------------------
for f in ../lib/*.jar
do
  TOOL_CLASSPATH="$TOOL_CLASSPATH":$f
done
#echo "classpath : $TOOL_CLASSPATH"

#---------- Running the migration client -----------
java -cp "$TOOL_CLASSPATH" org.apache.airavata.migrator.registry.XRegistryMigrationManager $1



