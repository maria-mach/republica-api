package br.ifsp.republica.repository;

import br.ifsp.republica.model.Despesa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {

    List<Despesa> findByRepublicaIdOrderByDataAscIdAsc(Long republicaId);

    Optional<Despesa> findByIdAndRepublicaId(Long id, Long republicaId);

    boolean existsByModeloIdAndCompetencia(Long modeloId, String competencia);
}
