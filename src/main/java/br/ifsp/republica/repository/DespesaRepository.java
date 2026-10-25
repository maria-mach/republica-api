package br.ifsp.republica.repository;
import br.ifsp.republica.model.Despesa;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface DespesaRepository extends JpaRepository<Despesa,Long> {
 List<Despesa> findByRepublicaIdOrderByDataAscIdAsc(Long id);
 Optional<Despesa> findByIdAndRepublicaId(Long id,Long republicaId);
 boolean existsByModeloIdAndCompetencia(Long modeloId,String competencia);
}
