This tool contains clients to airavata/tools/gsissh libraries.  To compile, you must first run "mvn clean install" in airavata/tools/gsissh. Then run "mvn clean install" in airavata/tools/gsissh-cli-tools.

To run the SSHApiClientWithMyProxyAuth tool, use the following command line:

java -classpath ./target/gsissh-cli-tools-0.14-SNAPSHOT.jar:$CP -Dmyproxy.server=myproxy.teragrid.org -Dmyproxy.username=<your.username> -Dmyproxy.password=<your.password> -Dmyproxy.cert.location=<path-to>/airavata/tools/gsissh-cli-tools/target/classes/certificates/ -Dremote.host=trestles.sdsc.xsede.org -Dremote.host.port=22 -Dremote.cmd=/bin/ls org.apache.airavata.gsi.ssh.cli.SSHApiClientWithMyProxyAuth 
