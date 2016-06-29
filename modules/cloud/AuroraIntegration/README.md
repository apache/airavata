#Requirements:
1. Apache Aurora
2. Apache Mesos

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
java -jar AuroraAdminDriver -o create -r 1024 -n batik -c 2.0 -d 1000 -i gouravr/dacapo:tag9
```
To kill:
```
java -jar AuroraAdminDriver -o kill -k batik
```

To update:
```
java -jar AuroraAdminDriver -o update -n batik
```

To retrieve update information:
```
java -jar AuroraAdminDriver -o update-info -n batik
```

To pause an update:
```
java -jar AuroraAdminDriver -o update-pause -n batik
```

To list the update progress:
```
java -jar AuroraAdminDriver -o update-list -n batik
```

To abort an update:
```
java -jar AuroraAdminDriver -o update-abort -n batik
```

To resume an update:
```
java -jar AuroraAdminDriver -o update-resume -n batik
```

To restart a job:
```
java -jar AuroraAdminDriver -o restart -n batik
```
