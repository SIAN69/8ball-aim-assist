#!/bin/sh
##############################################################################
# Gradle wrapper script
##############################################################################
APP_HOME=$(cd "$(dirname "$0")" && pwd)
JAVA_OPTS=""
DEFAULT_JVM_OPTS="-Dfile.encoding=UTF-8 -Duser.country=US -Duser.language=en -Duser.variant"
exec java $JAVA_OPTS $DEFAULT_JVM_OPTS -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
