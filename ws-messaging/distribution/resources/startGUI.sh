#!/bin/sh

DISTRIBUTION_ROOT=$(dirname `pwd`) 

BROKER_CLASS_PATH=""

#broker libraries 
for f in "$DISTRIBUTION_ROOT"/client-api/broker*.jar
do
  BROKER_CLASS_PATH="$BROKER_CLASS_PATH":$f
done


#axis libraries 
for f in "$DISTRIBUTION_ROOT"/standalone-server/lib/*.jar
do
  BROKER_CLASS_PATH="$BROKER_CLASS_PATH":$f
done

echo $BROKER_CLASS_PATH


java -classpath "$BROKER_CLASS_PATH" wsmg.gui.NotificationViewer 


