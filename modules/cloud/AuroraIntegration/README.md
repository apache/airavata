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
auroraCreate -o create -r 1024 -n batik -c 2.0 -d 1000 -i gouravr/dacapo:tag9
```
To kill:
```
auroraCreate -o kill -k batik```
```
