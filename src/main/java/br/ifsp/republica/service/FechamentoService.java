package br.ifsp.republica.service;

import br.ifsp.republica.exception.RegraException;
import br.ifsp.republica.model.Fechamento;
import br.ifsp.republica.model.Republica;
import br.ifsp.republica.repository.FechamentoRepository;
import br.ifsp.republica.util.Competencias;
import java.time.YearMonth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FechamentoService {

    private final FechamentoRepository fechamentos;
    private final ContextoRepublica contexto;

    public FechamentoService(FechamentoRepository fechamentos, ContextoRepublica contexto) {
        this.fechamentos = fechamentos;
        this.contexto = contexto;
    }

    // Fechar protege os lançamentos; não quita dívidas nem confirma pagamentos.
    @Transactional
    public void fechar(Long usuarioId, String competencia) {
        Republica republica = contexto.bloquearRepublica(usuarioId);
        contexto.exigirAdministrador(republica, usuarioId);
        YearMonth mes = Competencias.converter(competencia);

        if (mesEstaFechado(republica.getId(), mes)) {
            return;
        }

        Fechamento fechamento = new Fechamento();
        fechamento.setRepublicaId(republica.getId());
        fechamento.setMes(mes.toString());
        fechamentos.save(fechamento);
    }

    public void exigirMesAberto(Long republicaId, YearMonth mes) {
        if (mesEstaFechado(republicaId, mes)) {
            throw RegraException.invalido("Mês fechado: despesas não podem ser alteradas");
        }
    }

    private boolean mesEstaFechado(Long republicaId, YearMonth mes) {
        return fechamentos.existsByRepublicaIdAndMes(republicaId, mes.toString());
    }
}
