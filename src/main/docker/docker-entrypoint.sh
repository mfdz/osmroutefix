#!/bin/bash

if [ "$1" = 'osmroutefix' ]; then
	shift 
	OSM_FILE=(`ls /osmroutefix/data/*.pbf`)
		
    if [ -f /osmroutefix/data/env.sh  ]; then
		. /osmroutefix/data/env.sh
	fi

	if [ -z "${JAVA_OPTS}" ]; then
		JAVA_OPTS="$JAVA_OPTS -Xms64m -Xmx1024m -XX:MaxPermSize=256m -Djava.net.preferIPv4Stack=true"
		JAVA_OPTS="$JAVA_OPTS -server -Djava.awt.headless=true -Xconcurrentio"
		echo "Setting default JAVA_OPTS"
	fi

	RUN_ARGS=" -Dgraphhopper.datareader.file=$OSM_FILE -Dgraphhopper.graph.location=/osmroutefix/data/gh -jar osmroutefix-$osmroutefix_VERSION.jar server config.yml"

	echo "JAVA_OPTS= ${JAVA_OPTS}"
	echo "RUN_ARGS= ${RUN_ARGS}"
	
	exec java $JAVA_OPTS $RUN_ARGS "$@"	
fi

exec "$@"
