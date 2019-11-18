FROM openjdk:8

COPY target/security-system.jar /usr/security-system.jar
COPY maven/ /usr/maven/
WORKDIR /usr

ENTRYPOINT ["top", "-b"]