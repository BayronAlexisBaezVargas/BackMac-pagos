package backmac.pagos.repository;

import backmac.pagos.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByPedidoId(Long pedidoId);
    List<Pago> findByUsuarioIdOrderByFechaPagoDesc(String usuarioId);
}
