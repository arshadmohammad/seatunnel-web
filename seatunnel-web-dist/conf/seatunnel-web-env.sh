#!/bin/sh

# Default directories
STACK_ROOT=${STACK_ROOT:-"/usr/bigtop"}
LOGDIR=${LOGDIR:-"/var/log/seatunnel-web"}
CONFDIR=${CONFDIR:-"/etc/seatunnel-web/conf"}
SEATUNNEL_WEB_HOME=${SEATUNNEL_WEB_HOME:-"$STACK_ROOT/current/seatunnel-web"}

# JVM options
SEATUNNEL_WEB_JAVA_OPTS="-Xms2g -Xmx4g -Xmn1g -XX:+PrintGCDetails"
SEATUNNEL_WEB_JAVA_OPTS="$SEATUNNEL_WEB_JAVA_OPTS -Xlog:gc:${LOGDIR}/gc.log"
SEATUNNEL_WEB_JAVA_OPTS="$SEATUNNEL_WEB_JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOGDIR}/dump.hprof"

#JDK17_OPTS="--add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.security.jgss/sun.security.krb5=ALL-UNNAMED"
#SEATUNNEL_WEB_JAVA_OPTS="$SEATUNNEL_WEB_JAVA_OPTS $JDK17_OPTS"

#DEBUG_OPTS="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=*:8476,suspend=n"
#DEBUG_OPTS="$DEBUG_OPTS -verbose:class"
#SEATUNNEL_WEB_JAVA_OPTS="$SEATUNNEL_WEB_JAVA_OPTS $DEBUG_OPTS"

export SEATUNNEL_WEB_JAVA_OPTS
