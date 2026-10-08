package backmac.pagos.client;

import backmac.pagos.dto.NotificacionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ms-notificaciones", url = "${servicios.notificaciones.url}")
public interface NotificacionClient {

    @PostMapping
    void enviar(@RequestBody NotificacionDTO dto,
                @RequestHeader("Authorization") String token);
}
