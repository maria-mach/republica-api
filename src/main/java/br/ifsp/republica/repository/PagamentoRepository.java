package br.ifsp.republica.repository;
import br.ifsp.republica.model.Pagamento;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface PagamentoRepository extends JpaRepository<Pagamento,Long> {
 List<Pagamento> findByRepublicaIdOrderByDataAscIdAsc(Long id);
 Optional<Pagamento> findByIdAndRepublicaId(Long id,Long republicaId);
}
