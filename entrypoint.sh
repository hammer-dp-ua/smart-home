#!/bin/sh

#cp /usr/log4j2.xml /data/log4j2.xml
#cp /usr/application.properties /data/application.properties
# Using exec a java process is being executed with PID 1
exec java -Duser.timezone=Europe/Kiev -Dlogging.config=file:/data/log4j2.xml -jar /usr/super-home.jar --spring.config.location=/data/application.properties
