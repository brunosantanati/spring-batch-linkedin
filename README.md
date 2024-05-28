# [LinkedIn Learning Spring Batch](https://www.linkedin.com/learning/spring-spring-batch)  

## Links

[Spring Batch Page](https://spring.io/projects/spring-batch)  
[Spring Batch Doc](https://docs.spring.io/spring-batch/reference/index.html)  
[MySQL Docker Image](https://hub.docker.com/_/mysql)  

## Commands

```
Create Docker network:
docker network create my-bridge-network

Start a MySQL instance:
docker run --name some-mysql --network my-bridge-network -e MYSQL_ROOT_PASSWORD=batch -d mysql:latest

List networks:
docker network ls

Show containers and its networks:
docker ps --format '{{ .ID }} {{ .Names }} {{ json .Networks }}'

Inspect a network and shows the containers associated with it:
docker network inspect my-bridge-network

See logs:
docker logs some-mysql

Get the ip address of a container:
docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' some-mysql

Connect to MySQL (using IP):
docker run -it --network my-bridge-network --rm mysql mysql -h172.21.0.2 -uroot -p

SQL queries used:
create database batch_repo;
select * from BATCH_JOB_EXECUTION\G
select * from BATCH_JOB_INSTANCE\G
select * from BATCH_STEP_EXECUTION\G
```
