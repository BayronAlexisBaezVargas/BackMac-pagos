package backmac.pagos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTO {
    private String usuarioId;
    private Long pedidoId;
    private String tipo;
    private String mensaje;
}
