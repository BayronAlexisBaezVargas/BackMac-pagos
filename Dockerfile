# Fase 1: Construccion
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -q
COPY src src
RUN ./mvnw package -DskipTests -q

# Fase 2: Ejecucion
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache postgresql postgresql-contrib su-exec
RUN mkdir -p /run/postgresql && chown -R postgres:postgres /run/postgresql
RUN mkdir -p /var/lib/postgresql/data && chown -R postgres:postgres /var/lib/postgresql/data
USER postgres
RUN initdb -D /var/lib/postgresql/data
USER root
WORKDIR /app
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
COPY --from=builder /app/target/pagos-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8084 5436
ENTRYPOINT ["/entrypoint.sh"]
