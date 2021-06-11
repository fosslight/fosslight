#Copyright (C) Sameer1046
#Build the image from source
FROM gradle:4.7.0-jdk8-alpine AS build

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle build --no-daemon

#Create the containerized app
FROM openjdk:8-jre-alpine

LABEL maintainer="FOSSLight <fosslight-dev@lge.com>"

COPY --from=build /home/gradle/src/build/libs/*.war /app/FOSSLight.war

WORKDIR /app

CMD ["java" , "-jar", "FOSSLight.war", "--root.dir=/data/fosslight", "--server.port=8180"]
