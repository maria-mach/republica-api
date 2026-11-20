package br.ifsp.republica.repository;

import br.ifsp.republica.model.Republica;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepublicaRepository extends JpaRepository<Republica, Long> {

    Optional<Republica> findByCodigoConvite(String codigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Republica r where r.id = :id")
    Optional<Republica> lockById(@Param("id") Long id);
}
