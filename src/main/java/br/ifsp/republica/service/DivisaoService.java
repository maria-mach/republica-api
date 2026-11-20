package br.ifsp.republica.service;

import br.ifsp.republica.dto.Contratos.NovaDespesa;
import br.ifsp.republica.dto.Contratos.Participante;
import br.ifsp.republica.exception.RegraException;
import br.ifsp.republica.model.TipoDivisao;
import br.ifsp.republica.util.Dinheiro;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class DivisaoService {

    private static final BigDecimal TOTAL_PERCENTUAL = new BigDecimal("100");

    public Map<Long, BigDecimal> dividir(NovaDespesa despesa) {
        exigirParticipantesSemRepeticao(despesa.participantes());

        Map<Long, BigDecimal> parcelas = calcularParcelas(despesa);

        return atribuirResiduoAoPagador(despesa, parcelas);
    }

    private void exigirParticipantesSemRepeticao(List<Participante> participantes) {
        Set<Long> moradoresVistos = new HashSet<>();
        for (Participante participante : participantes) {
            if (!moradoresVistos.add(participante.moradorId())) {
                throw RegraException.invalido("Participante repetido");
            }
        }
    }

    private Map<Long, BigDecimal> calcularParcelas(NovaDespesa despesa) {
        TipoDivisao tipo = TipoDivisao.valueOf(despesa.divisao());
        if (tipo == TipoDivisao.IGUAL) {
            return dividirIgualmente(despesa);
        }

        return dividirPorPartesInformadas(despesa, tipo);
    }

    private Map<Long, BigDecimal> dividirIgualmente(NovaDespesa despesa) {
        BigDecimal quantidadeParticipantes = BigDecimal.valueOf(despesa.participantes().size());
        BigDecimal valorParcela = despesa.valor().divide(
            quantidadeParticipantes, Dinheiro.CASAS_DECIMAIS, RoundingMode.DOWN);

        Map<Long, BigDecimal> parcelas = new LinkedHashMap<>();
        for (Participante participante : despesa.participantes()) {
            parcelas.put(participante.moradorId(), valorParcela);
        }

        return parcelas;
    }

    private Map<Long, BigDecimal> dividirPorPartesInformadas(NovaDespesa despesa, TipoDivisao tipo) {
        Map<Long, BigDecimal> parcelas = new LinkedHashMap<>();
        BigDecimal somaDasPartes = BigDecimal.ZERO;

        for (Participante participante : despesa.participantes()) {
            BigDecimal parteInformada = exigirParteNaoNegativa(participante);
            somaDasPartes = somaDasPartes.add(parteInformada);
            parcelas.put(participante.moradorId(), converterParteEmValor(despesa, tipo, parteInformada));
        }

        exigirSomaEsperada(despesa, tipo, somaDasPartes);

        return parcelas;
    }

    private BigDecimal exigirParteNaoNegativa(Participante participante) {
        BigDecimal parte = participante.parte();
        if (parte == null || parte.signum() < 0) {
            throw RegraException.invalido("Informe a parte não negativa de cada participante");
        }

        return parte;
    }

    private BigDecimal converterParteEmValor(NovaDespesa despesa, TipoDivisao tipo, BigDecimal parte) {
        if (tipo == TipoDivisao.PERCENTUAL) {
            return despesa.valor()
                .multiply(parte)
                .divide(TOTAL_PERCENTUAL, Dinheiro.CASAS_DECIMAIS, RoundingMode.DOWN);
        }

        return parte.setScale(Dinheiro.CASAS_DECIMAIS, RoundingMode.UNNECESSARY);
    }

    private void exigirSomaEsperada(NovaDespesa despesa, TipoDivisao tipo, BigDecimal somaDasPartes) {
        BigDecimal somaEsperada = tipo == TipoDivisao.PERCENTUAL ? TOTAL_PERCENTUAL : despesa.valor();
        if (somaDasPartes.compareTo(somaEsperada) != 0) {
            throw RegraException.invalido("A soma deve ser " + somaEsperada);
        }
    }

    private Map<Long, BigDecimal> atribuirResiduoAoPagador(
            NovaDespesa despesa, Map<Long, BigDecimal> parcelas) {
        BigDecimal totalDistribuido = parcelas.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal residuo = despesa.valor().subtract(totalDistribuido);

        Map<Long, BigDecimal> parcelasComResiduo = new LinkedHashMap<>(parcelas);
        if (residuo.signum() != 0) {
            // O pagador assume os centavos restantes, mesmo se não participar da divisão.
            parcelasComResiduo.merge(despesa.pagadorId(), residuo, BigDecimal::add);
        }

        return parcelasComResiduo;
    }
}
