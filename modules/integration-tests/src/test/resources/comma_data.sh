#!/bin/sh
FIRST=1
for var in "$@"
do
	if [ $FIRST -eq 1 ]
	then
	    echo -n "$var"
		FIRST=0
	else
		echo -n ",$var"
	fi
done
echo
