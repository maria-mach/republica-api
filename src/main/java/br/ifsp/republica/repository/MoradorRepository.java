package br.ifsp.republica.repository;
import br.ifsp.republica.model.Morador;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface MoradorRepository extends JpaRepository<Morador,Long> {
 Optional<Morador> findByEmail(String email);
 List<Morador> findByRepublicaIdOrderById(Long id);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select m from Morador m where m.id=:id")
 Optional<Morador> lockById(@Param("id") Long id);
}
