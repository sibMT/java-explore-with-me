FROM eclipse-temurin:21-jre
ARG JAR=target/ewm-stats-service-*.jar
WORKDIR /app
COPY ${JAR} app.jar
EXPOSE 9090
ENTRYPOINT ["java","-jar","/app/app.jar"]