package backmac.pagos.controller;

import backmac.pagos.dto.ProcesarPagoDTO;
import backmac.pagos.entity.Pago;
import backmac.pagos.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @PostMapping
    public ResponseEntity<Pago> procesarPago(@Valid @RequestBody ProcesarPagoDTO dto,
                                             @AuthenticationPrincipal Jwt jwt,
                                             @RequestHeader("Authorization") String tokenHeader) {
        String usuarioId = jwt.getSubject();
        Pago pago = pagoService.procesarPago(dto, usuarioId, tokenHeader);
        return ResponseEntity.ok(pago);
    }

    @GetMapping
    public ResponseEntity<List<Pago>> obtenerMisPagos(@AuthenticationPrincipal Jwt jwt) {
        String usuarioId = jwt.getSubject();
        return ResponseEntity.ok(pagoService.obtenerMisPagos(usuarioId));
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<Pago> obtenerPorPedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(pagoService.obtenerPorPedido(pedidoId));
    }
}
