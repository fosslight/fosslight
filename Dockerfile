# Copyright (c) Sameer1046
# SPDX-License-Identifier: AGPL-3.0-only
# Build the image from source
FROM gradle:6.8.2-jdk8 AS build

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle build --no-daemon --exclude-task test


#Create the containerized app
FROM openjdk:8-jre-alpine

LABEL maintainer="FOSSLight <fosslight-dev@lge.com>"

COPY --from=build /home/gradle/src/build/libs/*.war /app/FOSSLight.war
COPY ./verify/verify /app/verify/verify
COPY ./db/wait-for /app/wait-for

ADD ./src/main/resources/template /app/template

RUN chmod +x /app/wait-for
RUN chmod +x /app/verify/verify
RUN apk update && apk add bash

WORKDIR /app

CMD ["java" , "-jar", "FOSSLight.war", "--root.dir=/data/fosslight", "--server.port=8180"]
