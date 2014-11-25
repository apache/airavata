#!/bin/sh

. `dirname $0`/../bin/setenv.sh
JAVA_OPTS=""

java -classpath "$XBAYA_CLASSPATH" \
		    -Djava.endorsed.dirs="$AIRAVATA_HOME/lib/endorsed":"$JAVA_HOME/jre/lib/endorsed":"$JAVA_HOME/lib/endorsed" \
		     org.apache.airavata.client.samples.RegisterSampleData $*
