package br.ifsp.republica.repository;
import br.ifsp.republica.model.Republica;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface RepublicaRepository extends JpaRepository<Republica,Long> {
 Optional<Republica> findByCodigoConvite(String codigo);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from Republica r where r.id=:id")
 Optional<Republica> lockById(@Param("id") Long id);
}
