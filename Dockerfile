# Multi-stage build
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copiar archivos de configuración
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd

# Descargar dependencias (cacheable)
RUN ./mvnw dependency:go-offline -B

# Copiar código y compilar
COPY src src
RUN ./mvnw clean package -DskipTests -B

# Imagen final
FROM eclipse-temurin:21-jre-alpine

# Instalar dependencias necesarias
RUN apk add --no-cache ca-certificates tzdata curl

# Crear usuario no root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copiar JAR
COPY --from=builder /app/target/*.jar app.jar

# Cambiar owner
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8085

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
