# Documentación Técnica: Microservicio de Pagos (ms-pagos)

## Descripción General
El microservicio `ms-pagos` es responsable de procesar los pagos de las órdenes creadas en `ms-pedidos`.

## Arquitectura y Tecnologías
- **Lenguaje:** Java 21
- **Framework:** Spring Boot
- **Persistencia:** PostgreSQL
- **Integraciones:** Comunica síncronamente con `ms-pedidos` y `ms-notificaciones` usando OpenFeign.

## Cambios Recientes
### Versión 1.3.2
- Integración de Productor RabbitMQ (`RabbitTemplate`, `pedidos360.exchange`) para publicación asíncrona de eventos de pago (aprobado/rechazado).
- Incorporación de dependencia `jackson-databind` para serialización JSON automática de mensajes en RabbitMQ.
- Despliegue Cloud Native en AWS Academy con Dockerfile multi-stage y PostgreSQL embebido.

### Versión 1.2.0
- Se integró `NotificacionClient` para emitir notificaciones al usuario automáticamente después de un pago (aprobado/rechazado).
- Se configuró el contenedor Docker para despliegues.
