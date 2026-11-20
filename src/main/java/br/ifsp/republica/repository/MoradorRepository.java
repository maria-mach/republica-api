package br.ifsp.republica.repository;

import br.ifsp.republica.model.Morador;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MoradorRepository extends JpaRepository<Morador, Long> {

    Optional<Morador> findByEmail(String email);

    List<Morador> findByRepublicaIdOrderById(Long republicaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Morador m where m.id = :id")
    Optional<Morador> lockById(@Param("id") Long id);
}
