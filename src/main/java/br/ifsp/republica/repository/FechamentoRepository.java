package br.ifsp.republica.repository;

import br.ifsp.republica.model.Fechamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FechamentoRepository extends JpaRepository<Fechamento, Long> {

    boolean existsByRepublicaIdAndMes(Long republicaId, String mes);
}
