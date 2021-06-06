#Copyright (C) Sameer1046
FROM openjdk:8-jre-alpine

MAINTAINER sameer1046

COPY ./FOSSLight.war /app/FOSSLight.war

WORKDIR /app

CMD ["java" , "-jar", "FOSSLight.war", "--root.dir=/data/fosslight", "--server.port=8180"]
