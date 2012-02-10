USING Notification Listener GUI.

It is designed for Airavata project. The expected message XML schema in Airavata Workflow Management schema. It can also accept String messages.
 
 1, Use ant to compile the package. 
  2, Then run
source setenv.sh
Or setenv.bat in Windows.
 to setup CLASSPATH. It looks better in Windows.

  3, Then run: 
 java wsnt.wsntviewapp.NotificationViewer 
 to start the GUI.

You need to enter Broker URL, topic and listening port. 
