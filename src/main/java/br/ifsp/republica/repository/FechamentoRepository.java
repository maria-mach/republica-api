package br.ifsp.republica.repository;
import br.ifsp.republica.model.Fechamento;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface FechamentoRepository extends JpaRepository<Fechamento,Long> {
 boolean existsByRepublicaIdAndMes(Long republicaId,String mes);
}
