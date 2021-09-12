# docker build -t postgres-image .
# docker stop postgres-container || docker start postgres-container
# docker run --network=super_home_nw --restart always --detach --name postgres-container --publish 5432:5432 --mount type=volume,source=postgres_data_volume,destination=/var/lib/postgresql/data -v "/home/servak/PostgreSQL/my-postgres.conf":/etc/postgresql/postgresql.conf postgres-image -c config_file=/etc/postgresql/postgresql.conf
# docker exec -it --user postgres postgres-container bash

# Backup
# docker run --rm -it --entrypoint tar --mount type=volume,source=postgres_data_volume,destination=/var/lib/postgresql/data --mount type=bind,source=/home/servak/PostgreSQL/docker_backup,destination=/backup --name postgres-container-tmp postgres-image cvf /backup/postgres-image_backup.tar /var/lib/postgresql/data
# Restore backup
# docker run --rm -it --entrypoint bash --mount type=volume,source=postgres_data_volume,destination=/var/lib/postgresql/data --mount type=bind,source=/home/servak/PostgreSQL/docker_backup,destination=/backup --name postgres-container-tmp postgres-image -c "cd /var/lib/postgresql/data && rm -rf * && tar xvf /backup/backup.tar --strip 1"
# docker run --rm -it --entrypoint bash --mount type=volume,source=postgres_data_volume,destination=/var/lib/postgresql/data --name postgres-container-tmp postgres-image -c "mv /var/lib/postgresql/data/lib/postgresql/data/* /var/lib/postgresql/data/"

# Backup 2. "--mount type=bind" is always moounted from "root" user
# docker run --rm -it --user=root --entrypoint bash --mount type=volume,source=postgres_data_volume,destination=/var/lib/postgresql/data --mount type=bind,source=/home/servak/PostgreSQL/docker_backup,destination=/backup --name postgres-container-tmp postgres-image -c "cd /var/lib/postgresql/data && tar cvf /backup/backup_ $(date '+%Y_%m_%d__%H_%M_%S').tar ."
# docker run --rm -it --entrypoint bash --mount type=volume,source=postgres_data_volume,destination=/var/lib/postgresql/data --mount type=bind,source=/home/servak/PostgreSQL/docker_backup,destination=/backup --name postgres-container-tmp postgres-image -c "cd /var/lib/postgresql/data && rm -rf * && tar xvf /backup/backup.tar"

FROM postgres:12.1

RUN unlink /etc/localtime
RUN ln -s /usr/share/zoneinfo/Europe/Kiev /etc/localtime

ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=Qwerty123
