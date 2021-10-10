#!/bin/bash
# run required service
docker-compose down
docker-compose up -d

# wait for service in docker-compose to up
sleep 10s

# run discord bot server
java -jar ./target/MSDOBot-Jenkins-0.0.1-SNAPSHOT.jar