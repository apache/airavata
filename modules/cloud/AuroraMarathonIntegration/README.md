#AURORA MARATHON INTEGRATION

#Requirements:
1. Apache Aurora
2. Apache Mesos
3. Marathon

#Installation:
##Please follow the link to set up Apache Aurora and Apache Mesos
1. http://mesos.apache.org/gettingstarted/ OR https://open.mesosphere.com/getting-started/install/
2. http://aurora.apache.org/documentation/latest/operations/installation/

#Usage:

```Options:
-o = create/kill
-n = name of the job
-r = amount of RAM
-c = CPU count
-d = disk space
-k = name of the task to be killed
-i = executable/image
```
#Example:
To create and launch:
```
java -jar AuroraMarathonIntegration-0.17-SNAPSHOT.jar -o create -r 1024 -n batik -c 2.0 -d 1000 -i gouravr/dacapo:tag9
```
To kill:
```
java -jar AuroraMarathonIntegration-0.17-SNAPSHOT.jar -o kill -n batik
```

To update:
```
java -jar AuroraMarathonIntegration-0.17-SNAPSHOT.jar -o update -n batik
```

To retrieve update information:
```
java -jar AuroraMarathonIntegration-0.17-SNAPSHOT.jar -o update-info -n batik
```

To pause an update:
```
java -jar AuroraMarathonIntegration-0.17-SNAPSHOT.jar -o update-pause -n batik
```

To list the update progress:
```
java -jar AuroraMarathonIntegration-0.17-SNAPSHOT.jar -o update-list -n batik
```

To abort an update:
```
java -jar AuroraMarathonIntegration-0.17-SNAPSHOT.jar -o update-abort -n batik
```

To resume an update:
```
java -jar AuroraMarathonIntegration-0.17-SNAPSHOT.jar -o update-resume -n batik
```

To restart a job:
```
java -jar AuroraMarathonIntegration-0.17-SNAPSHOT.jar -o restart -n batik
```
