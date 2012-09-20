. `dirname $0`/setenv.sh
export DERBY_HOME=$AIRAVATA_HOME/standalone-server
cd $AIRAVATA_HOME/bin
./startNetworkServer $*
