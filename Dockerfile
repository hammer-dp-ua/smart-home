FROM openjdk:8

COPY target/security-system.jar /usr/security-system.jar
COPY log4j2.xml /usr/log4j2.xml
COPY config/application.properties /usr/application.properties

EXPOSE 8080

RUN apt-get update && apt-get install -y --no-install-recommends \
   vim \
   && rm -rf /var/lib/apt/lists/*

ENV ENTRYPOINT_SCRIPT=/usr/entrypoint.sh
COPY entrypoint.sh $ENTRYPOINT_SCRIPT
ENTRYPOINT exec $ENTRYPOINT_SCRIPT