package br.ifsp.republica.service;

import br.ifsp.republica.dto.Contratos.Saldo;
import br.ifsp.republica.dto.Contratos.SaldoOutro;
import br.ifsp.republica.dto.Contratos.Transferencia;
import br.ifsp.republica.model.Despesa;
import br.ifsp.republica.model.Morador;
import br.ifsp.republica.model.Pagamento;
import br.ifsp.republica.model.Republica;
import br.ifsp.republica.repository.DespesaRepository;
import br.ifsp.republica.repository.MoradorRepository;
import br.ifsp.republica.repository.PagamentoRepository;
import br.ifsp.republica.util.Dinheiro;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SaldoService {

    private final MoradorRepository moradores;
    private final DespesaRepository despesas;
    private final PagamentoRepository pagamentos;
    private final ContextoRepublica contexto;

    public SaldoService(
            MoradorRepository moradores,
            DespesaRepository despesas,
            PagamentoRepository pagamentos,
            ContextoRepublica contexto) {
        this.moradores = moradores;
        this.despesas = despesas;
        this.pagamentos = pagamentos;
        this.contexto = contexto;
    }

    public Saldo saldo(Long usuarioId) {
        Republica republica = contexto.republica(usuarioId);
        Map<Long, BigDecimal> saldoComCadaMorador = calcularSaldosEntreUsuarioEOutros(republica.getId(), usuarioId);
        BigDecimal saldoTotal = calcularSaldos(republica.getId()).getOrDefault(usuarioId, BigDecimal.ZERO);

        List<SaldoOutro> detalhamento = saldoComCadaMorador.entrySet().stream()
            .map(saldo -> new SaldoOutro(saldo.getKey(), saldo.getValue()))
            .toList();

        return new Saldo(saldoTotal, detalhamento);
    }

    public List<Transferencia> simplificar(Long usuarioId) {
        Republica republica = contexto.republica(usuarioId);

        return SimplificadorDeDividas.simplificar(calcularSaldos(republica.getId()));
    }

    // Positivo = a receber; negativo = a pagar. O saldo é sempre reconstruído do histórico.
    public Map<Long, BigDecimal> calcularSaldos(Long republicaId) {
        Map<Long, BigDecimal> saldos = new TreeMap<>();
        for (Morador morador : moradores.findByRepublicaIdOrderById(republicaId)) {
            saldos.put(morador.getId(), Dinheiro.zero());
        }

        for (Despesa despesa : despesas.findByRepublicaIdOrderByDataAscIdAsc(republicaId)) {
            aplicarDespesaAosSaldos(saldos, despesa);
        }
        for (Pagamento pagamento : pagamentos.findByRepublicaIdOrderByDataAscIdAsc(republicaId)) {
            if (pagamento.isConfirmado()) {
                aplicarPagamentoAosSaldos(saldos, pagamento);
            }
        }

        return saldos;
    }

    private void aplicarDespesaAosSaldos(Map<Long, BigDecimal> saldos, Despesa despesa) {
        saldos.merge(despesa.getPagadorId(), despesa.getValor(), BigDecimal::add);
        despesa.getParcelas().forEach(
            (moradorId, parcela) -> saldos.merge(moradorId, parcela.negate(), BigDecimal::add));
    }

    private void aplicarPagamentoAosSaldos(Map<Long, BigDecimal> saldos, Pagamento pagamento) {
        saldos.merge(pagamento.getPagadorId(), pagamento.getValor(), BigDecimal::add);
        saldos.merge(pagamento.getRecebedorId(), pagamento.getValor().negate(), BigDecimal::add);
    }

    private Map<Long, BigDecimal> calcularSaldosEntreUsuarioEOutros(Long republicaId, Long usuarioId) {
        Map<Long, BigDecimal> saldosPorMorador = new TreeMap<>();

        for (Despesa despesa : despesas.findByRepublicaIdOrderByDataAscIdAsc(republicaId)) {
            aplicarDespesaAoDetalhamento(saldosPorMorador, despesa, usuarioId);
        }
        for (Pagamento pagamento : pagamentos.findByRepublicaIdOrderByDataAscIdAsc(republicaId)) {
            if (pagamento.isConfirmado()) {
                aplicarPagamentoAoDetalhamento(saldosPorMorador, pagamento, usuarioId);
            }
        }

        return saldosPorMorador;
    }

    private void aplicarDespesaAoDetalhamento(
            Map<Long, BigDecimal> saldosPorMorador, Despesa despesa, Long usuarioId) {
        if (despesa.foiPagaPor(usuarioId)) {
            despesa.getParcelas().forEach((moradorId, parcela) -> {
                if (!moradorId.equals(usuarioId)) {
                    saldosPorMorador.merge(moradorId, parcela, BigDecimal::add);
                }
            });
        } else if (despesa.temParcelaDe(usuarioId)) {
            BigDecimal valorDevido = despesa.parcelaDe(usuarioId).negate();
            saldosPorMorador.merge(despesa.getPagadorId(), valorDevido, BigDecimal::add);
        }
    }

    private void aplicarPagamentoAoDetalhamento(
            Map<Long, BigDecimal> saldosPorMorador, Pagamento pagamento, Long usuarioId) {
        if (pagamento.foiFeitoPor(usuarioId)) {
            saldosPorMorador.merge(pagamento.getRecebedorId(), pagamento.getValor(), BigDecimal::add);
        }
        if (pagamento.foiRecebidoPor(usuarioId)) {
            saldosPorMorador.merge(pagamento.getPagadorId(), pagamento.getValor().negate(), BigDecimal::add);
        }
    }
}
