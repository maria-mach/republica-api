package br.ifsp.republica.repository;

import br.ifsp.republica.model.Recorrencia;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecorrenciaRepository extends JpaRepository<Recorrencia, Long> {

    List<Recorrencia> findByRepublicaIdOrderById(Long republicaId);

    Optional<Recorrencia> findByIdAndRepublicaId(Long id, Long republicaId);
}
