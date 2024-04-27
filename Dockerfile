#FROM openjdk:latest
FROM bellsoft/liberica-openjdk-debian:17

#RUN addgroup spring-boot-group && adduser --ingroup spring-boot-group spring-boot
#USER spring-boot:spring-boot-group

WORKDIR /app

COPY target/players-service-1.0.3.jar /app/players-service.jar

EXPOSE 8082 8084

ENV CSV_FILE_PATH=d:/player.csv
ENV MANAGEMENT_SERVER_PORT=8084

CMD ["java", "-DCSV_FILE_PATH=${CSV_FILE_PATH}","-jar", "players-service.jar", "--management.server.port=${MANAGEMENT_SERVER_PORT}"]

#ARG JAR_FILE
#WORKDIR /build
#
#ADD $JAR_FILE application.jar
#RUN java -Djarmode=layertools -jar application.jar extract --destination extracted
#
#FROM eclipse-temurin:21.0.2_13-jdk-jammy
#
#RUN addgroup spring-boot-group && adduser --ingroup spring-boot-group spring-boot
#USER spring-boot:spring-boot-group
#VOLUME /tmp
#WORKDIR /application
#
#COPY --from=build /build/extracted/dependencies .
#COPY --from=build /build/extracted/spring-boot-loader .
#COPY --from=build /build/extracted/snapshot-dependencies .
#COPY --from=build /build/extracted/application .
#
#ENTRYPOINT exec java ${JAVA_OPTS} org.springframework.boot.loader.launch.JarLauncher ${0} ${@}