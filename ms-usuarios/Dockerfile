# 1. Usamos una imagen base ligera (Alpine) pero con Java 25 para compatibilidad
FROM eclipse-temurin:25-jdk-alpine

# 2. Volumen temporal para Tomcat (mejora el rendimiento de escritura de logs/temp)
VOLUME /tmp

# 3. Argumento para localizar el JAR generado
ARG JAR_FILE=target/*.jar

# 4. Copiamos el JAR al contenedor con un nombre estándar
COPY ${JAR_FILE} app.jar

# 5. Comando de arranque optimizado
# -Djava.security.egd... ayuda a que arranque más rápido en entornos Linux/Cloud
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]
