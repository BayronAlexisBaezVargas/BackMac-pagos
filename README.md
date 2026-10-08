# Documentación Técnica: Microservicio de Pagos (ms-pagos)

## Descripción General
El microservicio `ms-pagos` es responsable de procesar los pagos de las órdenes creadas en `ms-pedidos`.

## Arquitectura y Tecnologías
- **Lenguaje:** Java 21
- **Framework:** Spring Boot
- **Persistencia:** PostgreSQL
- **Integraciones:** Comunica síncronamente con `ms-pedidos` y `ms-notificaciones` usando OpenFeign.

## Cambios Recientes
### Versión 1.2.0
- Se integró `NotificacionClient` para emitir notificaciones al usuario automáticamente después de un pago (aprobado/rechazado).
- Se configuró el contenedor Docker para despliegues.
