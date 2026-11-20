package br.ifsp.republica.service;

import br.ifsp.republica.dto.Contratos.NovaDespesa;
import br.ifsp.republica.dto.Contratos.Participante;
import br.ifsp.republica.exception.RegraException;
import br.ifsp.republica.model.Despesa;
import br.ifsp.republica.model.Republica;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class RegrasDespesa {

    private final ContextoRepublica contexto;
    private final FechamentoService fechamentoService;
    private final DivisaoService divisaoService;

    public RegrasDespesa(
            ContextoRepublica contexto,
            FechamentoService fechamentoService,
            DivisaoService divisaoService) {
        this.contexto = contexto;
        this.fechamentoService = fechamentoService;
        this.divisaoService = divisaoService;
    }

    public void preencherDespesa(Despesa despesa, NovaDespesa novaDespesa, Republica republica) {
        validarParticipantes(republica, novaDespesa);
        fechamentoService.exigirMesAberto(republica.getId(), YearMonth.from(novaDespesa.data()));
        Map<Long, BigDecimal> parcelas = divisaoService.dividir(novaDespesa);

        despesa.setRepublicaId(republica.getId());
        despesa.setDescricao(novaDespesa.descricao());
        despesa.setValor(novaDespesa.valor());
        despesa.setData(novaDespesa.data());
        despesa.setCategoria(novaDespesa.categoria());
        despesa.setPagadorId(novaDespesa.pagadorId());
        despesa.setDivisao(novaDespesa.divisao());
        despesa.substituirParcelas(parcelas);
    }

    public void validarParticipantes(Republica republica, NovaDespesa novaDespesa) {
        contexto.exigirMembroDaRepublica(republica, novaDespesa.pagadorId());
        for (Participante participante : novaDespesa.participantes()) {
            contexto.exigirMembroDaRepublica(republica, participante.moradorId());
        }
    }

    public void validarAlteracaoDaDespesa(Republica republica, Long usuarioId, Despesa despesa) {
        exigirAutorOuAdministrador(republica, usuarioId, despesa);
        fechamentoService.exigirMesAberto(republica.getId(), despesa.getMesDaData());

        // Impede recriar dívidas de moradores que já saíram com saldo zero.
        contexto.exigirMembroDaRepublica(republica, despesa.getPagadorId());
        for (Long participanteId : despesa.getParcelas().keySet()) {
            contexto.exigirMembroDaRepublica(republica, participanteId);
        }
    }

    private void exigirAutorOuAdministrador(Republica republica, Long usuarioId, Despesa despesa) {
        boolean autorDoLancamento = despesa.getAutorId().equals(usuarioId);
        if (!autorDoLancamento && !republica.administradaPor(usuarioId)) {
            throw RegraException.proibido();
        }
    }
}
