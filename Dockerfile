FROM adoptopenjdk/openjdk8:jre8u252-b09-alpine

ARG VERSION

COPY HiS-${VERSION}-shaded.jar /var/his/HiS.jar

WORKDIR /var/his/

# Server port
EXPOSE 8080

ENV REDIS_ADDR localhost
ENV REDIS_PORT 6379

ENTRYPOINT [ "sh", "-c", "java", "-cp", "HiS.jar", "info.tritusk.his.MainKt" ]

CMD [ "--db-address", "$REDIS_ADDR", "--db-port", "$REDIS_PORT" ]
