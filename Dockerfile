FROM openjdk:latest

WORKDIR /app

COPY target/players-service-1.0.0.jar /app/players-service.jar

EXPOSE 8082 8084

ENV CSV_FILE_PATH=d:/player.csv
ENV MANAGEMENT_SERVER_PORT=8084

CMD ["java", "-DCSV_FILE_PATH=${CSV_FILE_PATH}","-jar", "players-service.jar", "--management.server.port=${MANAGEMENT_SERVER_PORT}"]
