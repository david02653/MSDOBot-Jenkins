#!/bin/bash

# build rasa action server image
cd rasa
sh ./buildActionServer.sh

# build msdobot-jenkins bot server
mvn clean test -Dmaven.test.failure.ignore=true
mvn install -Dmaven.test.skip=true

#cp ./target/MSDOBot-Jenkins-0.0.1-SNAPSHOT.jar .
#sudo docker stop msdobot-jenkins || true
#sudo docker rm msdobot-jenkins || true
#sudo docker build -t msdobot-jenkins:latest .