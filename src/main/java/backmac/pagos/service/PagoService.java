package backmac.pagos.service;

import backmac.pagos.client.NotificacionClient;
import backmac.pagos.client.PedidoClient;
import backmac.pagos.dto.NotificacionDTO;
import backmac.pagos.dto.ProcesarPagoDTO;
import backmac.pagos.entity.EstadoPago;
import backmac.pagos.entity.Pago;
import backmac.pagos.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoClient pedidoClient;
    private final NotificacionClient notificacionClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange:pedidos360.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key-notificaciones:notificaciones.routing.key}")
    private String routingKeyNotif;

    @Transactional
    public Pago procesarPago(ProcesarPagoDTO dto, String usuarioId, String tokenHeader) {
        Pago pago = new Pago();
        pago.setPedidoId(dto.getPedidoId());
        pago.setUsuarioId(usuarioId);
        pago.setMonto(dto.getMonto());
        pago.setMetodoPago(dto.getMetodoPago());

        boolean pagoExitoso = simularPasarelaPago();

        if (pagoExitoso) {
            pago.setEstado(EstadoPago.APROBADO);
            Pago pagoGuardado = pagoRepository.save(pago);

            // 1. Notificar a ms-pedidos: cambiar estado a EN_PREPARACION
            try {
                pedidoClient.actualizarEstado(dto.getPedidoId(), "EN_PREPARACION", tokenHeader);
                log.info("Pedido {} actualizado a EN_PREPARACION", dto.getPedidoId());
            } catch (Exception e) {
                log.error("No se pudo actualizar el estado del pedido {}: {}", dto.getPedidoId(), e.getMessage());
            }

            // 2. Notificar a RabbitMQ de forma asíncrona desacoplada
            NotificacionDTO notif = new NotificacionDTO(
                usuarioId,
                dto.getPedidoId(),
                "PAGO_APROBADO",
                "Tu pago de $" + dto.getMonto() + " fue aprobado. Tu pedido #" + dto.getPedidoId() + " esta en preparacion."
            );
            try {
                rabbitTemplate.convertAndSend(exchangeName, routingKeyNotif, notif);
                log.info("[RABBITMQ-PRODUCER] Notificacion PAGO_APROBADO enviada a RabbitMQ para usuario {}", usuarioId);
            } catch (Exception e) {
                log.warn("[RABBITMQ-PRODUCER] Error enviando a RabbitMQ: {}. Usando fallback Feign...", e.getMessage());
                try {
                    notificacionClient.enviar(notif, tokenHeader);
                    log.info("Notificacion enviada por fallback Feign al usuario {}", usuarioId);
                } catch (Exception ex) {
                    log.error("Fallo envio por Feign: {}", ex.getMessage());
                }
            }

            return pagoGuardado;
        } else {
            pago.setEstado(EstadoPago.RECHAZADO);
            Pago pagoGuardado = pagoRepository.save(pago);

            // Notificar rechazo
            NotificacionDTO notif = new NotificacionDTO(
                usuarioId,
                dto.getPedidoId(),
                "PAGO_RECHAZADO",
                "Tu pago fue rechazado. Por favor intenta con otro metodo de pago."
            );
            try {
                rabbitTemplate.convertAndSend(exchangeName, routingKeyNotif, notif);
                log.info("[RABBITMQ-PRODUCER] Notificacion PAGO_RECHAZADO enviada a RabbitMQ para usuario {}", usuarioId);
            } catch (Exception e) {
                log.warn("[RABBITMQ-PRODUCER] Error enviando a RabbitMQ: {}. Usando fallback Feign...", e.getMessage());
                try {
                    notificacionClient.enviar(notif, tokenHeader);
                } catch (Exception ex) {
                    log.error("Fallo envio por Feign: {}", ex.getMessage());
                }
            }

            return pagoGuardado;
        }
    }

    private boolean simularPasarelaPago() {
        return Math.random() > 0.1;
    }

    public List<Pago> obtenerMisPagos(String usuarioId) {
        return pagoRepository.findByUsuarioIdOrderByFechaPagoDesc(usuarioId);
    }

    public Pago obtenerPorPedido(Long pedidoId) {
        return pagoRepository.findByPedidoId(pedidoId)
            .orElseThrow(() -> new RuntimeException("No se encontro pago para el pedido: " + pedidoId));
    }
}
