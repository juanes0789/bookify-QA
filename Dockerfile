# -------------------------------------------------------------
# Etapa 1: Build de la aplicación con Gradle (Java 17)
# -------------------------------------------------------------
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copiar archivos del wrapper de Gradle
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Dar permisos de ejecución y descargar dependencias
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# Copiar el código fuente y compilar el JAR omitiendo tests
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# -------------------------------------------------------------
# Etapa 2: Imagen final de ejecución (Java 17 JRE)
# -------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el JAR generado desde la carpeta build/libs/
COPY --from=builder /app/build/libs/*.jar app.jar

# Exponer el puerto por defecto de Spring Boot
EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]