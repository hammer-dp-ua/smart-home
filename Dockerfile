# docker build -t super-home .
# docker stop super-home-container
# docker rm super-home-container
# docker exec -it super-home-container bash
# docker run --network=super_home_nw --restart always --detach --publish 80:8080 --mount type=volume,source=super_home_data_volume,destination=/data --name super-home-container super-home
# docker run --network=super_home_nw --rm -it --publish 80:8080 --mount type=volume,source=super_home_data_volume,destination=/data --name super-home-container super-home

# Backup
# docker run --rm -it --entrypoint tar --mount type=volume,source=super_home_data_volume,destination=/data --mount type=bind,source=z:\docker_backup,destination=/backup --name super-home-container-tmp super-home cvf /backup/super-home_backup.tar /data
# Restore
# docker run --rm -it --entrypoint bash --mount type=volume,source=super_home_data_volume,destination=/data --mount type=bind,source=/home/servak/SecurityVideoUploader/docker_backup,destination=/backup --name super-home-container-tmp super-home -c "cd /data && tar xvf /backup/backup.tar --strip 1"

# Backup 2. "--mount type=bind" is always moounted from "root" user
# docker run --rm -it --entrypoint bash --mount type=volume,source=super_home_data_volume,destination=/data --mount type=bind,source=/home/servak/SecurityVideoUploader/docker_backup,destination=/backup --name super-home-container-tmp super-home -c "cd /data && tar cvf /backup/backup.tar ."
# Restore
# docker run --rm -it --entrypoint bash --mount type=volume,source=super_home_data_volume,destination=/data --mount type=bind,source=/home/servak/SecurityVideoUploader/docker_backup,destination=/backup --name super-home-container-tmp super-home -c "cd /data && tar xvf /backup/backup.tar"

FROM openjdk:8

RUN unlink /etc/localtime
RUN ln -s /usr/share/zoneinfo/Europe/Kiev /etc/localtime

COPY super-home.jar /usr/super-home.jar
COPY log4j2.xml /usr/log4j2.xml
COPY config/application.properties /usr/application.properties

EXPOSE 8080

RUN apt-get update && apt-get install -y --no-install-recommends \
   vim \
   && rm -rf /var/lib/apt/lists/*

ENV ENTRYPOINT_SCRIPT=/usr/entrypoint.sh
COPY entrypoint.sh $ENTRYPOINT_SCRIPT
RUN chmod 755 $ENTRYPOINT_SCRIPT
ENTRYPOINT exec $ENTRYPOINT_SCRIPT