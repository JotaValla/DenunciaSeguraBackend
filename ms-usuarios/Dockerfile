# Usa la imagen base ligera de Java 25
FROM eclipse-temurin:25-jdk-alpine
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
# Optimizacion para arranque rápido en contenedores
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]
