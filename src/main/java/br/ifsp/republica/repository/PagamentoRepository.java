package br.ifsp.republica.repository;

import br.ifsp.republica.model.Pagamento;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByRepublicaIdOrderByDataAscIdAsc(Long republicaId);

    Optional<Pagamento> findByIdAndRepublicaId(Long id, Long republicaId);
}
