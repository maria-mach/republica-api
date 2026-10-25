package br.ifsp.republica.repository;
import br.ifsp.republica.model.Recorrencia;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface RecorrenciaRepository extends JpaRepository<Recorrencia,Long> {
 List<Recorrencia> findByRepublicaIdOrderById(Long id);
 Optional<Recorrencia> findByIdAndRepublicaId(Long id,Long republicaId);
}
