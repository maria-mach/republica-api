package br.ifsp.republica.service;

import br.ifsp.republica.dto.Contratos.NovaDespesa;
import br.ifsp.republica.dto.Contratos.NovaRecorrencia;
import br.ifsp.republica.dto.Contratos.Participante;
import br.ifsp.republica.exception.RegraException;
import br.ifsp.republica.model.Despesa;
import br.ifsp.republica.model.Recorrencia;
import br.ifsp.republica.model.Republica;
import br.ifsp.republica.repository.DespesaRepository;
import br.ifsp.republica.repository.RecorrenciaRepository;
import br.ifsp.republica.util.Competencias;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RecorrenciaService {

    private final RecorrenciaRepository recorrencias;
    private final DespesaRepository despesas;
    private final ContextoRepublica contexto;
    private final RegrasDespesa regrasDespesa;
    private final FechamentoService fechamentoService;
    private final DivisaoService divisaoService;

    public RecorrenciaService(
            RecorrenciaRepository recorrencias,
            DespesaRepository despesas,
            ContextoRepublica contexto,
            RegrasDespesa regrasDespesa,
            FechamentoService fechamentoService,
            DivisaoService divisaoService) {
        this.recorrencias = recorrencias;
        this.despesas = despesas;
        this.contexto = contexto;
        this.regrasDespesa = regrasDespesa;
        this.fechamentoService = fechamentoService;
        this.divisaoService = divisaoService;
    }

    // O modelo não gera gastos: ele é reutilizado pelo método gerar para um mês solicitado.
    @Transactional
    public Recorrencia criarModelo(Long usuarioId, NovaRecorrencia novaRecorrencia) {
        Republica republica = contexto.bloquearRepublica(usuarioId);
        contexto.exigirAdministrador(republica, usuarioId);

        NovaDespesa dadosDaDespesa = novaRecorrencia.despesa();
        regrasDespesa.validarParticipantes(republica, dadosDaDespesa);
        divisaoService.dividir(dadosDaDespesa);

        return recorrencias.save(montarModelo(republica, novaRecorrencia));
    }

    public List<Recorrencia> listarModelos(Long usuarioId) {
        Republica republica = contexto.republica(usuarioId);
        return recorrencias.findByRepublicaIdOrderById(republica.getId());
    }

    @Transactional
    public void excluirModelo(Long usuarioId, Long modeloId) {
        Republica republica = contexto.bloquearRepublica(usuarioId);
        contexto.exigirAdministrador(republica, usuarioId);

        Recorrencia recorrencia = recorrencias.findByIdAndRepublicaId(modeloId, republica.getId())
            .orElseThrow(RegraException::ausente);
        recorrencias.delete(recorrencia);
    }

    // A transação engloba o lote: se um modelo falhar, nenhuma despesa do lote é confirmada.
    @Transactional
    public List<Despesa> gerar(Long usuarioId, String competencia) {
        Republica republica = contexto.bloquearRepublica(usuarioId);
        contexto.exigirAdministrador(republica, usuarioId);
        YearMonth mes = Competencias.converter(competencia);
        fechamentoService.exigirMesAberto(republica.getId(), mes);

        List<Despesa> despesasGeradas = new ArrayList<>();
        for (Recorrencia modelo : recorrencias.findByRepublicaIdOrderById(republica.getId())) {
            if (jaFoiGerada(modelo, mes)) {
                continue;
            }

            Despesa despesa = criarDespesaDoModelo(modelo, mes, republica, usuarioId);
            despesasGeradas.add(despesas.save(despesa));
        }

        return despesasGeradas;
    }

    private Recorrencia montarModelo(Republica republica, NovaRecorrencia novaRecorrencia) {
        NovaDespesa dadosDaDespesa = novaRecorrencia.despesa();

        Recorrencia recorrencia = new Recorrencia();
        recorrencia.setRepublicaId(republica.getId());
        recorrencia.setDescricao(dadosDaDespesa.descricao());
        recorrencia.setCategoria(dadosDaDespesa.categoria());
        recorrencia.setValor(dadosDaDespesa.valor());
        recorrencia.setPagadorId(dadosDaDespesa.pagadorId());
        recorrencia.setDivisao(dadosDaDespesa.divisao());
        recorrencia.setDia(novaRecorrencia.dia());

        for (Participante participante : dadosDaDespesa.participantes()) {
            BigDecimal parte = participante.parte() == null ? BigDecimal.ZERO : participante.parte();
            recorrencia.adicionarParticipante(participante.moradorId(), parte);
        }

        return recorrencia;
    }

    private boolean jaFoiGerada(Recorrencia modelo, YearMonth mes) {
        return despesas.existsByModeloIdAndCompetencia(modelo.getId(), mes.toString());
    }

    private Despesa criarDespesaDoModelo(
            Recorrencia modelo, YearMonth mes, Republica republica, Long usuarioId) {
        Despesa despesa = new Despesa();
        regrasDespesa.preencherDespesa(despesa, montarDadosDoModelo(modelo, mes), republica);
        despesa.setAutorId(usuarioId);
        despesa.setModeloId(modelo.getId());
        despesa.setCompetencia(mes.toString());

        return despesa;
    }

    private NovaDespesa montarDadosDoModelo(Recorrencia modelo, YearMonth mes) {
        List<Participante> participantes = modelo.getParticipantes().entrySet().stream()
            .map(parte -> new Participante(parte.getKey(), parte.getValue()))
            .toList();

        // Um modelo do dia 31 usa o último dia quando o mês é mais curto.
        int diaDaDespesa = Math.min(modelo.getDia(), mes.lengthOfMonth());
        LocalDate data = mes.atDay(diaDaDespesa);

        return new NovaDespesa(
            modelo.getDescricao(),
            modelo.getValor(),
            data,
            modelo.getCategoria(),
            modelo.getPagadorId(),
            modelo.getDivisao(),
            participantes);
    }
}
