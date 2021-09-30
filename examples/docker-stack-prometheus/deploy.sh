#!/bin/sh
stack_name="process"
mvn package -Dmaven.test.skip=true -f "../monitored-app/pom.xml"
mkdir -p "target" && cp ../monitored-app/target/*.jar ./target
docker build --pull --rm -f Dockerfile -t camundamonitoring .
docker stack deploy $stack_name --compose-file docker-compose.yml
docker stack services -q $stack_name \
  |while read service; do
    docker service update --force $service
  done
