#!/bin/sh
set -e

echo "Arrancando PostgreSQL en el puerto 5436..."
su-exec postgres pg_ctl start -D /var/lib/postgresql/data -w -o "-p 5436"

echo "Configurando la base de datos y usuario para Pagos..."
su-exec postgres psql -p 5436 -c "CREATE DATABASE pagos_db;" || true
su-exec postgres psql -p 5436 -c "CREATE USER springuser WITH PASSWORD 'springpassword';" || true
su-exec postgres psql -p 5436 -c "GRANT ALL PRIVILEGES ON DATABASE pagos_db TO springuser;" || true
su-exec postgres psql -p 5436 -d pagos_db -c "GRANT ALL ON SCHEMA public TO springuser;" || true

echo "Arrancando microservicio de Pagos..."
exec java -jar /app/app.jar
