# docker build -t super-home .
# docker stop super-home-container
# docker rm super-home-container
# docker exec -it super-home-container bash
# docker run --network=super_home_nw --restart always --detach --publish 80:8080 --mount type=bind,source=z:\Videos\record\0018A2_192.168.0.200_1,destination=/ram_videos --mount type=bind,source=d:\Videos,destination=/hard_drive_videos --mount type=volume,source=super_home_data_volume,destination=/data --name super-home-container super-home
# docker run --network=super_home_nw --rm -it --publish 80:8080 --mount type=bind,source=z:\Videos\record\0018A2_192.168.0.200_1,destination=/ram_videos --mount type=bind,source=d:\Videos,destination=/hard_drive_videos --mount type=volume,source=super_home_data_volume,destination=/data --name super-home-container super-home

FROM openjdk:8

RUN unlink /etc/localtime
RUN ln -s /usr/share/zoneinfo/Europe/Kiev /etc/localtime

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