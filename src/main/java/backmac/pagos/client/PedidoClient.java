package backmac.pagos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ms-pedidos", url = "${servicios.pedidos.url}")
public interface PedidoClient {

    @PatchMapping("/{id}/estado")
    void actualizarEstado(@PathVariable Long id,
                          @RequestParam String estado,
                          @RequestHeader("Authorization") String token);
}
