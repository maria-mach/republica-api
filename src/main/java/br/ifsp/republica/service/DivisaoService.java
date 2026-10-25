package br.ifsp.republica.service;

import br.ifsp.republica.dto.Contratos.NovaDespesa;
import br.ifsp.republica.exception.RegraException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DivisaoService {
    public Map<Long, BigDecimal> dividir(NovaDespesa despesa) {
        Map<Long, BigDecimal> parcelas = new LinkedHashMap<>();
        for (var participante : despesa.participantes()) {
            if (parcelas.containsKey(participante.moradorId())) {
                throw RegraException.invalido("Participante repetido");
            }
            parcelas.put(participante.moradorId(), BigDecimal.ZERO.setScale(2));
        }

        BigDecimal soma = BigDecimal.ZERO;
        if ("IGUAL".equals(despesa.divisao())) {
            BigDecimal parte = despesa.valor().divide(
                BigDecimal.valueOf(parcelas.size()), 2, RoundingMode.DOWN);
            parcelas.replaceAll((id, valor) -> parte);
            soma = parte.multiply(BigDecimal.valueOf(parcelas.size()));
        } else {
            BigDecimal somaDasPartes = BigDecimal.ZERO;
            boolean percentual = "PERCENTUAL".equals(despesa.divisao());
            for (var participante : despesa.participantes()) {
                BigDecimal parte = participante.parte();
                if (parte == null || parte.signum() < 0) {
                    throw RegraException.invalido("Informe a parte não negativa de cada participante");
                }
                somaDasPartes = somaDasPartes.add(parte);
                BigDecimal valor = percentual
                    ? despesa.valor().multiply(parte).divide(new BigDecimal("100"), 2, RoundingMode.DOWN)
                    : parte.setScale(2, RoundingMode.UNNECESSARY);
                parcelas.put(participante.moradorId(), valor);
                soma = soma.add(valor);
            }
            BigDecimal esperado = percentual ? new BigDecimal("100") : despesa.valor();
            if (somaDasPartes.compareTo(esperado) != 0) {
                throw RegraException.invalido("A soma deve ser " + esperado);
            }
        }

        // O pagador pode pagar pelos outros sem participar da divisão.
        // Se sobram centavos, ele assume o resíduo, conforme a regra do tema.
        BigDecimal resto = despesa.valor().subtract(soma);
        if (resto.signum() != 0) {
            parcelas.merge(despesa.pagadorId(), resto, BigDecimal::add);
        }
        return parcelas;
    }
}
