package backmac.pagos.service;

import backmac.pagos.client.PedidoClient;
import backmac.pagos.dto.ProcesarPagoDTO;
import backmac.pagos.entity.EstadoPago;
import backmac.pagos.entity.Pago;
import backmac.pagos.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoClient pedidoClient;

    @Transactional
    public Pago procesarPago(ProcesarPagoDTO dto, String usuarioId, String tokenHeader) {
        // Crear registro de pago
        Pago pago = new Pago();
        pago.setPedidoId(dto.getPedidoId());
        pago.setUsuarioId(usuarioId);
        pago.setMonto(dto.getMonto());
        pago.setMetodoPago(dto.getMetodoPago());

        // Simulacion de pasarela de pago:
        // En produccion aqui iria la integracion con Transbank, Stripe, etc.
        boolean pagoExitoso = simularPasarelaPago(dto);

        if (pagoExitoso) {
            pago.setEstado(EstadoPago.APROBADO);
            Pago pagoGuardado = pagoRepository.save(pago);

            // Notificar a ms-pedidos para cambiar estado a EN_PREPARACION
            try {
                pedidoClient.actualizarEstado(dto.getPedidoId(), "EN_PREPARACION", tokenHeader);
                log.info("Pedido {} actualizado a EN_PREPARACION", dto.getPedidoId());
            } catch (Exception e) {
                log.error("No se pudo actualizar el estado del pedido {}: {}", dto.getPedidoId(), e.getMessage());
            }

            return pagoGuardado;
        } else {
            pago.setEstado(EstadoPago.RECHAZADO);
            return pagoRepository.save(pago);
        }
    }

    private boolean simularPasarelaPago(ProcesarPagoDTO dto) {
        // Simulacion: el 90% de los pagos son aprobados
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
