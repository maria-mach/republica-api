package br.ifsp.republica.service;

import br.ifsp.republica.dto.Contratos.Evento;
import br.ifsp.republica.model.Despesa;
import br.ifsp.republica.model.Pagamento;
import br.ifsp.republica.model.Republica;
import br.ifsp.republica.model.TipoEvento;
import br.ifsp.republica.repository.DespesaRepository;
import br.ifsp.republica.repository.PagamentoRepository;
import br.ifsp.republica.util.Competencias;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RelatorioService {

    private final DespesaRepository despesas;
    private final PagamentoRepository pagamentos;
    private final DespesaService despesaService;
    private final ContextoRepublica contexto;

    public RelatorioService(
            DespesaRepository despesas,
            PagamentoRepository pagamentos,
            DespesaService despesaService,
            ContextoRepublica contexto) {
        this.despesas = despesas;
        this.pagamentos = pagamentos;
        this.despesaService = despesaService;
        this.contexto = contexto;
    }

    public Map<String, BigDecimal> categorias(Long usuarioId, String competencia) {
        YearMonth mes = Competencias.converter(competencia);
        Map<String, BigDecimal> totaisPorCategoria = new TreeMap<>();

        for (Despesa despesa : despesaService.listarDespesas(usuarioId, mes.toString())) {
            totaisPorCategoria.merge(despesa.getCategoria(), despesa.getValor(), BigDecimal::add);
        }

        return totaisPorCategoria;
    }

    public Map<String, BigDecimal> mensal(Long usuarioId) {
        Map<String, BigDecimal> totaisPorMes = new TreeMap<>();

        for (Despesa despesa : despesaService.listarTodasAsDespesas(usuarioId)) {
            totaisPorMes.merge(despesa.getMesDaData().toString(), despesa.getValor(), BigDecimal::add);
        }

        return totaisPorMes;
    }

    public List<Evento> extrato(Long usuarioId) {
        Republica republica = contexto.republica(usuarioId);
        List<Evento> eventos = new ArrayList<>();
        eventos.addAll(criarEventosDeDespesas(republica.getId(), usuarioId));
        eventos.addAll(criarEventosDePagamentos(republica.getId(), usuarioId));

        eventos.sort(Comparator.comparing(Evento::data)
            .thenComparing(Evento::referenciaId)
            .thenComparing(Evento::tipo));

        return eventos;
    }

    private List<Evento> criarEventosDeDespesas(Long republicaId, Long usuarioId) {
        List<Evento> eventos = new ArrayList<>();

        for (Despesa despesa : despesas.findByRepublicaIdOrderByDataAscIdAsc(republicaId)) {
            if (despesa.foiPagaPor(usuarioId)) {
                eventos.add(criarEvento(
                    despesa.getData(), TipoEvento.DESPESA_PAGA, despesa.getId(), despesa.getValor()));
            }
            if (despesa.temParcelaDe(usuarioId)) {
                BigDecimal parcela = despesa.parcelaDe(usuarioId).negate();
                eventos.add(criarEvento(despesa.getData(), TipoEvento.PARCELA, despesa.getId(), parcela));
            }
        }

        return eventos;
    }

    private List<Evento> criarEventosDePagamentos(Long republicaId, Long usuarioId) {
        List<Evento> eventos = new ArrayList<>();

        for (Pagamento pagamento : pagamentos.findByRepublicaIdOrderByDataAscIdAsc(republicaId)) {
            if (!pagamento.envolve(usuarioId)) {
                continue;
            }

            BigDecimal valor = pagamento.foiFeitoPor(usuarioId)
                ? pagamento.getValor()
                : pagamento.getValor().negate();
            TipoEvento tipo = pagamento.isConfirmado()
                ? TipoEvento.PAGAMENTO_CONFIRMADO
                : TipoEvento.PAGAMENTO_PENDENTE;
            eventos.add(criarEvento(pagamento.getData(), tipo, pagamento.getId(), valor));
        }

        return eventos;
    }

    // O contrato da API expõe o tipo como texto, que também define a ordenação do extrato.
    private Evento criarEvento(LocalDate data, TipoEvento tipo, Long referenciaId, BigDecimal valor) {
        return new Evento(data, tipo.name(), referenciaId, valor);
    }
}
